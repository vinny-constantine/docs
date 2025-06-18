
@Slf4j
@RestControllerAdvice
public class ExceptionHandleAspect {

    @Resource
    private MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    CommonResultBean exceptionHandler(MethodArgumentNotValidException exception) {
        //获取http请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Optional<HttpServletRequest> request = Optional.empty();
        if (requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            request = Optional.of(servletRequestAttributes.getRequest());
        }
        Locale locale = request.map(x -> Locale.forLanguageTag(x.getHeader(I18nConfig.LANGUAGE_HEADER))).orElse(Locale.getDefault());
        return Optional.of(exception.getBindingResult())
            .map(Errors::getFieldError)
            .filter(fieldError -> StringUtils.isNotBlank(fieldError.getDefaultMessage()))
            .map(fieldError -> {
                //springmvc validator 校验入参DTO时, 错误码放在defaultMessage中了
                Optional<String> errorCodeOpt = Optional.of(fieldError.getDefaultMessage())
                    .filter(code -> code.startsWith(I18nConfig.ERROR_CODE_PREFIX));
                return CommonResultBean.fail(errorCodeOpt.orElse(CommonResultBean.ERROR_MSG), errorCodeOpt
                    .map(code -> messageSource.getMessage(code, fieldError.getArguments(), locale))
                    .orElse(fieldError.getDefaultMessage()));
            })
            .orElse(CommonResultBean.fail());
    }

    
    @ExceptionHandler(Exception.class)
    public CommonResultBean ExceptionHandler(Exception e) {
        if (e instanceof JsonParseException) {
            log.warn("请求体json转化出错", e);
            return CommonResultBean.fail(MesErrorMsgEnum.MES1000013);
        }
        if (e instanceof HttpMediaTypeNotSupportedException) {
            log.warn("http媒体类型不支持异常", e);
            return CommonResultBean.fail(MesErrorMsgEnum.MES1000014);
        }
        if (e instanceof MissingServletRequestParameterException) {
            log.warn("缺少参数", e.getLocalizedMessage());
            return CommonResultBean.fail(MesErrorMsgEnum.MES1000015);
        }
        log.error("发生异常", e);
        return CommonResultBean.fail(MesErrorMsgEnum.MES1000016);
    }
}