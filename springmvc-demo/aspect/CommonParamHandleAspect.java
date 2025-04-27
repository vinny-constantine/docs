
@Aspect
@Component
@Order(10)
@Slf4j
public class CommonParamHandleAspect {

    @Pointcut("execution(* com.dover..controller..*.*(..))")
    public void targetAspect() {
    }


    @Around("targetAspect()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        //获取目标对象方法参数
        Object[] args = joinPoint.getArgs();
        Signature signature = joinPoint.getSignature();
        MethodSignature ms = (MethodSignature)signature;
        //切入点方法
        Method method = ms.getMethod();
        Class<?>[] parameterTypes = ms.getParameterTypes();

        try {
            UserSession userSession = LoginUserContext.getUserSession();
            log.info("登录信息UserSession={}", userSession);
            if (userSession != null) {
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (CommonParamDto.class.isAssignableFrom(parameterTypes[i])) {
                        if (args == null) {
                            log.info("参数为空不设置值 第{}个参数， method={}", i, method.getName());
                            continue;
                        }
                        setFieldNewValue(args[i], userSession);
                        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                        if(requestAttributes instanceof ServletRequestAttributes) {
                            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
                            HttpServletRequest request = servletRequestAttributes.getRequest();
                            CommonParamDto commonParamDto = (CommonParamDto) args[i];
                            commonParamDto.setUcSsoTokenId(request.getHeader("ucSsoTokenId"));
                            CommonParamDto.CommonParamContext.set(commonParamDto);
                        }
                    }
                }
            } else {// 若未登录，但传了tenantId、tenantCode，则将tenantId、tenantCode设置到ttl中
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (CommonParamDto.class.isAssignableFrom(parameterTypes[i])) {
                        CommonParamDto.CommonParamContext.set((CommonParamDto) args[i]);
                    }
                }
            }
            //执行请求,获取请求结果
            Object proceedResult = joinPoint.proceed(args);
            String proceedResultStr = getString(proceedResult);
            //超过长度截取, 并提示（已截断）
            proceedResultStr = proceedResultStr.length() > Constants.MAX_RETURN_VALUE_LOG_LENGTH ? proceedResultStr.substring(0, Constants.MAX_RETURN_VALUE_LOG_LENGTH) + "...（已截断）" : proceedResultStr;
            log.info("方法 {} 返回值 {}", ((MethodSignature) joinPoint.getSignature()).getMethod().getName(), proceedResultStr);
            return proceedResult;
        } catch (Exception e) {
            log.error("公共参数处理出现异常", e);
            return methodReturn(method, "ERR001", Optional.ofNullable(CommonParamDto.CommonParamContext.get()).map(CommonParamDto::getMsg).filter(StringUtils::isNotBlank).orElse("系统繁忙，请稍后再试！"));
        } finally {
            CommonParamDto.CommonParamContext.clear();
            LoginUserContext.clear();
        }
    }

    
    private Object methodReturn(Method method, String code, String msg) {
        // 返回类型
        Class<?> methodReturnType = method.getReturnType();
        if(MesResultBean.class.isAssignableFrom(methodReturnType)) {
            return MesResultBean.fail(code, msg);
        } else if(MesResultPage.class.isAssignableFrom(methodReturnType)) {
            return MesResultPage.fail(code, msg);
        } else {
            return JSONObject.parseObject(JSON.toJSONString(MesResultBean.fail(code, msg)), methodReturnType);
        }
    }

    private String getString(Object rvt) {
        if (rvt == null) {
            return "";
        }
        if (rvt instanceof byte[]) {
            return "（已忽略, 返回结果为字节数据）";
        }
        if (rvt instanceof String) {
            return rvt.toString();
        }
        return JSON.toJSONString(rvt);
    }
}