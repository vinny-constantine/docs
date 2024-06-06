
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * 操作人用户信息注入相关拦截器
 *
 * @author wangwei
 * @date 2024-05-28
 */
@Slf4j
public class InjectOprInfoInterceptor implements HandlerInterceptor {

    /**
     * tl：解析并注入操作人信息的标识，由注解、方法签名、参数类信息构成
     */
    private static final ThreadLocal<InjectOprInfoAnnoHolder> INJECT_OPR_INFO_FLAG = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //初始化，清空tl
        INJECT_OPR_INFO_FLAG.remove();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //处理完成，清空tl
        INJECT_OPR_INFO_FLAG.remove();
    }

    /**
     * 参数解析器中设置tl，并由@RequestBody注解的处理器解析参数
     */
    public static class InjectOprInfoHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver, ApplicationContextAware {

        private RequestResponseBodyMethodProcessor processor;

        /**
         * 注入@ReqeustBody注解处理器
         */
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            RequestMappingHandlerAdapter adapter = applicationContext.getBean(RequestMappingHandlerAdapter.class);
            this.processor = (RequestResponseBodyMethodProcessor) adapter.getArgumentResolvers().stream().filter(p -> p instanceof RequestResponseBodyMethodProcessor).findFirst().orElse(null);
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return processor != null && parameter.hasParameterAnnotation(InjectOprInfo.class);
        }

        /**
         * 解析注解，设置tl，并使用@ReqeustBody注解处理器解析参数
         */
        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            Parameter param = parameter.getParameter();
            InjectOprInfo annotation = parameter.getParameterAnnotation(InjectOprInfo.class);
            INJECT_OPR_INFO_FLAG.set(new InjectOprInfoAnnoHolder(annotation, parameter.getMethod().getName(), param.getType()));
            return processor.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        }
    }
    
    /**
     * tl中注解信息包装类
     */
    public static final class InjectOprInfoAnnoHolder {
        private InjectOprInfo annotation;
        private String targetMethodName;
        private Class<?> targetParamType;

        public InjectOprInfoAnnoHolder(InjectOprInfo annotation, String targetMethodName, Class<?> targetParamType) {
            this.annotation = annotation;
            this.targetMethodName = targetMethodName;
            this.targetParamType = targetParamType;
        }
    }

    /**
     * 是否匹配
     *
     * @param methodName 方法名称
     * @param paramType  参数类型
     * @return 是否匹配
     */
    public static boolean match(String methodName, Class<?> paramType) {
        InjectOprInfoAnnoHolder holder = INJECT_OPR_INFO_FLAG.get();
        if (holder == null) return false;
        return holder.targetMethodName.equals(methodName) && holder.targetParamType.equals(paramType);
    }

    /**
     * 注入操作人信息
     *  aspect 中处理替换
	 *	if (InjectOprInfoInterceptor.match(joinPoint.getSignature().getName(), obj.getClass())) {
	 *		InjectOprInfoInterceptor.injectOprInfo(user, obj);
	 *	}
     * @param user 操作人信息
     * @param args 参数对象
     */
    public static void injectOprInfo(LoginUserInfo user, Object args) {
        try {
            InjectOprInfoAnnoHolder holder = INJECT_OPR_INFO_FLAG.get();
            if (holder == null || user == null || args == null) return;
            InjectOprInfo annotation = holder.annotation;
            Class<?> clazz = args.getClass();
            //获取操作人信息字段
            Field oprUserIdField = ReflectUtil.getField(clazz, annotation.oprUserId());
            if (oprUserIdField != null) {
                ReflectUtil.setFieldValue(args, oprUserIdField, user.getUserId());
            }
            //获取操作人用户名字段
            Field oprUserNameField = ReflectUtil.getField(clazz, annotation.oprUserName());
            if (oprUserNameField != null) {
                ReflectUtil.setFieldValue(args, oprUserNameField, user.getUsername());
            }
            //获取操作人真实姓名字段
            Field oprRealNameField = ReflectUtil.getField(clazz, annotation.oprRealName());
            if (oprRealNameField != null) {
                ReflectUtil.setFieldValue(args, oprRealNameField, user.getRealName());
            }
        } catch (Exception e) {
            log.error("注入操作人信息失败", e);
        }
    }

}