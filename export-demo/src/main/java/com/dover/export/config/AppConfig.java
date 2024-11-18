package com.dover.export.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Stack;
import java.util.concurrent.Callable;

/**
 * @author dover
 * @since 2020/11/19
 */
@Log4j2
@EnableAsync
@Configuration
@EnableConfigurationProperties(PropsConfig.class)
public class AppConfig implements WebMvcConfigurer {

    @Autowired
    private PropsConfig propsConfig;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(360000).setTaskExecutor(getAsyncExecutor());
        configurer.registerCallableInterceptors(callableProcessingInterceptor());
    }

    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor getAsyncExecutor() {
        System.out.println(propsConfig);
        log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("AsyncStreaming-");
        return executor;
    }

    @Bean
    public CallableProcessingInterceptor callableProcessingInterceptor() {
        return new TimeoutCallableProcessingInterceptor() {
            @Override
            public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {
                log.error("timeout!");
                return super.handleTimeout(request, task);
            }
        };
    }


    public static void main(String[] args) {
//        System.out.println(Arrays.toString(bus(new int[]{3, 1, 2})));
//        System.out.println(calc(new int[]{7, 3, 3}));
//        System.out.println(calc(new int[]{4, 0, 2, 8, 3, 0}));
        System.out.println(isValidStr("a(bc)"));
        System.out.println(isValidStr("a([bc)]"));
    }

    public static int calc(int[] height) {
        int times = 0;
        for (int i = 0; i < height.length - 1; i++) {
            int tmp = height[i] + (height[i + 1] - height[i]) / 2;
            times += Math.abs(height[i + 1] - tmp);
            height[i + 1] = tmp;
        }
        return times;
    }

    public static int[] bus(int[] height) {
        boolean flag = false;
        for (int i = height.length - 1; i > 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                if (height[j] - height[i] >= (i - j)) {
                    height[i] = j;
                    flag = true;
                    break;
                }
            }
            flag = flag ? false : (height[i] = -1) == 1;
        }
        height[0] = -1;
        return height;
    }

    public static boolean isValidStr(String arg) {
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < arg.length(); i++) {
            boolean skip = false;
            char c = arg.charAt(i);
            if (c == ')') {
                while (true) {
                    Character pop = stack.pop();
                    if (pop == '(' || stack.isEmpty()) {
                        skip = true;
                        break;
                    }
                    if (pop == '[' || pop == '{') return false;
                }
            }
            if (c == ']') {
                while (true) {
                    Character pop = stack.pop();
                    if (pop == '[' || stack.isEmpty()) {
                        skip = true;
                        break;
                    }
                    if (pop == '(' || pop == '{') return false;
                }
            }
            if (c == '}') {
                while (true) {
                    Character pop = stack.pop();
                    if (pop == '{' || stack.isEmpty()) {
                        skip = true;
                        break;
                    }
                    if (pop == '(' || pop == '[') return false;
                }
            }
            if (skip) continue;
            stack.push(c);
        }

        while (!stack.isEmpty()) {
            Character c = stack.pop();
            if (c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}') return false;
        }
        return true;
    }

}
