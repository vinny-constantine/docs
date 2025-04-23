
import com.alibaba.fastjson.serializer.DateCodec;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.common.collect.Lists;
import com.zczy.mes.common.utils.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.*;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Configuration
public class FastjsonConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        // 清理默认
        messageConverters.clear();
        // 添加
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new ResourceRegionHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter<>());
        messageConverters.add(new AllEncompassingFormHttpMessageConverter());
        // 配置FastJson并添加
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        fastJsonHttpMessageConverter.setSupportedMediaTypes(getSupportedMediaTypes()); //MediaType
        fastJsonHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8); 
        fastJsonHttpMessageConverter.setFastJsonConfig(getFastJsonConfig());
        messageConverters.add(fastJsonHttpMessageConverter);

    }


    /**
     * FastJson 配置
     */
    private FastJsonConfig getFastJsonConfig() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        // 配置序列化器功能
        fastJsonConfig.setSerializerFeatures(
            SerializerFeature.PrettyFormat,
            SerializerFeature.WriteNullStringAsEmpty,
            SerializerFeature.WriteNullListAsEmpty);
        //序列化配置,将Long类型转为字符串
        //不要使用全局的,会影响其他地方的JSON序列化SerializeConfig serializeConfig=SerializeConfig.globalInst
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.put(BigInteger.class, com.alibaba.fastjson.serializer.ToStringSerializer.instance);
        serializeConfig.put(Long.class, com.alibaba.fastjson.serializer.ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, com.alibaba.fastjson.serializer.ToStringSerializer.instance);
        serializeConfig.put(long.class, com.alibaba.fastjson.serializer.ToStringSerializer.instance);
        serializeConfig.put(Date.class, DateCodec.instance);
        fastJsonConfig.setSerializeConfig(serializeConfig);
        return fastJsonConfig;
    }

    /**
     * 配置支持的媒体类型
     */
    private List<MediaType> getSupportedMediaTypes() {
        List<MediaType> supportedMediaTypes = Lists.newArrayList();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(new MediaType("application", "*+json"));
        return supportedMediaTypes;
    }
}