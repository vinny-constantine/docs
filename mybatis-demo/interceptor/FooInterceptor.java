package com.dover.demo.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.Properties;

/**
 * @author wangwei
 * @date 2024-06-06
 */
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class FooInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        //tricks：给mapper方法添加参数
        if (args != null && args.length > 1) {
            Object parameter = args[1];
            if (parameter instanceof Map) {
                ((Map) parameter).put("dover", "abc");
            } else if (ClassUtils.isPrimitiveOrWrapper(parameter.getClass()) || parameter instanceof String) {
                JSONObject params = new JSONObject();
                params.put("dover", "abc");
                params.put(((MappedStatement) args[0]).getBoundSql(params).getParameterMappings().get(0).getProperty(), parameter);
                args[1] = params;
            } else {
                JSONObject params = (JSONObject) JSON.toJSON(parameter);
                params.put("dover", "abc");
                args[1] = params;
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
