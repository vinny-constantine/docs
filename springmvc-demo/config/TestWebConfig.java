package com.dover.order.config;

import com.dover.db.routing.datasource.ReadWriteRoutingDataSource;
import com.dover.helper.spring.SpringBeanFactoryHelper;
import com.dover.properties.loader.DoverInsightConfig;
import com.dover.properties.utils.DoverInsightUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.beans.factory.support.AbstractBeanDefinition.DEPENDENCY_CHECK_NONE;

/**
 * 为测试Mapper准备的spring-config
 *
 * @author dover
 * @date 2019-06-18
 * @description
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.dover.order.core"})
@PropertySource("classpath:/datasource.properties")
@ImportResource(locations = {"classpath*:applicationInsightConfig.xml", "classpath:spring/datasource-config.xml", "classpath:spring/sharding-config.xml"})
public class AppConfiguration {


    private static PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private static MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

    private static DefaultListableBeanFactory beanFactory;

    private static final String SCAN_PACKAGE = "com.dover.order.core.dao.biz";


    //    @Bean // 需要 insight 时注释掉
    @SneakyThrows
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        Resource[] resources = resolver.getResources("classpath*:*.properties");
        propertySourcesPlaceholderConfigurer.setLocations(resources);
        return propertySourcesPlaceholderConfigurer;
    }


    @Bean
    public ReadWriteRoutingDataSource dataSource(DataSource writeDataSource, DataSource readDataSource) {
        ReadWriteRoutingDataSource dataSource = new ReadWriteRoutingDataSource();
        dataSource.setDefaultWriteDataSource(writeDataSource);
        HashMap<String, DataSource> readDataSourceMap = new HashMap<>();
        readDataSourceMap.put("read", readDataSource);
        dataSource.setReadDataSourceMap(readDataSourceMap);
        return dataSource;
    }


    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setConfigLocation(resolver.getResource("spring/mybatis-config.xml"));
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("com/dover/order/core/**/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }


    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    @Bean
    public SpringBeanFactoryHelper springBeanFactoryHelper() {
        SpringBeanFactoryHelper bean = beanFactory.createBean(SpringBeanFactoryHelper.class);
        beanFactory.autowireBeanProperties(bean, AbstractAutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        beanFactory.initializeBean(bean, "springBeanFactoryHelper");
        return bean;
    }


    @Bean
    public DoverInsightConfig doverInsightConfig() {
        DoverInsightConfig bean = beanFactory.createBean(DoverInsightConfig.class);
        beanFactory.autowireBean(bean);
        beanFactory.initializeBean(bean, "doverInsightConfig");
        return bean;
    }


    @Component
    public static class TestPrepareBeanPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

        private AtomicBoolean once = new AtomicBoolean(true);


        @Override
        @SneakyThrows
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(SCAN_PACKAGE) +
                                       File.separator + "**/*.class";
            Resource[] resources = resolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                AnnotationMetadata metadata = sbd.getMetadata();
                if (metadata.hasAnnotation(Component.class.getName()) //
                    || metadata.hasAnnotation(Service.class.getName()) //
                    || metadata.hasAnnotation(Controller.class.getName()) //
                    || metadata.hasAnnotation(Repository.class.getName()) //
                    || metadata.hasAnnotation(Configuration.class.getName())) {
                    sbd.setResource(resource);
                    sbd.setSource(resource);
                    sbd.setDependencyCheck(DEPENDENCY_CHECK_NONE);
                    registry.registerBeanDefinition(sbd.getBeanClassName(), sbd);
                }
            }
        }


        @Override
        @SneakyThrows
        public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
            beanFactory = (DefaultListableBeanFactory) factory;
            for (BeanPostProcessor beanPostProcessor : beanFactory.getBeanPostProcessors()) {
                Class<? extends BeanPostProcessor> processorClass = beanPostProcessor.getClass();
                if (processorClass.getSimpleName().equals("ApplicationContextAwareProcessor") && once.get() && once.getAndSet(false)) {
                    Field applicationContext = processorClass.getDeclaredField("applicationContext");
                    applicationContext.setAccessible(true);
                    ApplicationContext context = (ApplicationContext) applicationContext.get(beanPostProcessor);
                    context.publishEvent(new TestPrepareEvent("yes"));
                }
            }
        }


        @Override
        public void setEnvironment(Environment environment) {
            DoverInsightUtils.setInsightProperties((ConfigurableEnvironment) environment, "order");
        }
    }


    @Component
    public static class TestPrepareEventListener implements ApplicationListener<TestPrepareEvent> {

        @Override
        public void onApplicationEvent(TestPrepareEvent event) {
            if (beanFactory != null) {
                List<BeanPostProcessor> beanPostProcessors = beanFactory.getBeanPostProcessors();
                for (Iterator<BeanPostProcessor> iterator = beanPostProcessors.listIterator(); iterator.hasNext(); ) {
                    BeanPostProcessor beanPostProcessor = iterator.next();
                    if (beanPostProcessor instanceof AutowiredAnnotationBeanPostProcessor) {
                        AutowiredAnnotationBeanPostProcessor target = (AutowiredAnnotationBeanPostProcessor) beanPostProcessor;
                        BeanPostProcessor processor = (BeanPostProcessor) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                            ArrayUtils.add(target.getClass().getInterfaces(), InstantiationAwareBeanPostProcessor.class), (proxy, method, args) -> {
                                if (method.getName().equals("postProcessPropertyValues")) {
                                    try {
                                        method.invoke(target, args);
                                    } catch (Exception ignored) {
                                    }
                                    return args[0];
                                } else {
                                    return method.invoke(target, args);
                                }
                            });
                        iterator.remove();
                        beanPostProcessors.add(processor);
                        break;
                    }
                }
            }
        }
    }


    static class TestPrepareEvent extends ApplicationEvent {

        private static final long serialVersionUID = -6294938642057273336L;


        /**
         * Create a new ApplicationEvent.
         *
         * @param source the object on which the event initially occurred (never {@code null})
         */
        TestPrepareEvent(Object source) {
            super(source);
        }
    }
}

