
@Data
@Builder
@Slf4j
public class DoverResultBean<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SUCCESS_CODE = "200";

    public static final String SUCCESS_MSG = "操作成功";

    public static final String ERROR_CODE = "100";

    public static final String ERROR_CODE_DATABASE = "500";

    public static final String ERROR_MSG = "操作失败";

    /**
     * 响应结果状态码：200、成功  非200、失败
     */
    private String code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * traceId：每次请求的唯一标识，在TraceIdFilter中生成
     */
    private String traceId;
    
    /**
     * 数据
     */
    private T data;

    public MesResultBean() {
        this.traceId = TraceIdUtil.getTraceId();
    }

    public MesResultBean(String code, String msg, String traceId, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.traceId = Optional.ofNullable(traceId).orElse(TraceIdUtil.getTraceId());
    }

    
    /**
     * 判断是否成功
     */
    public boolean isOk() {
        return SUCCESS_CODE.equals(code);
    }
    /**
     * 判断是否失败
     */
    public boolean isNg() {
        return !isOk();
    }

    @SuppressWarnings("unchecked")
    public <R> MesResultBean<R> cast() {
        return (MesResultBean<R>) this;
    }

    
    public static <T> MesResultBean<T> build(String code, String msg, T data) {
        //在异常情况下，且有通用上下文时，尝试记录异常信息，方便setRollbackOnly()后，返回业务提示
        if (ERROR_CODE.equals(code) && CommonParamDto.CommonParamContext.get() != null) {
            CommonParamDto.CommonParamContext.get().setMsg(msg);
        }
        return MesResultBean.<T>builder().code(code).msg(msg).data(data).traceId(TraceIdUtil.getTraceId()).build();
    }

    public static <T> MesResultBean<T> build(JSONObject jsonObject) {
        String code = Optional.ofNullable(jsonObject.getString("code")).orElse(ERROR_CODE);
        String msg = Optional.ofNullable(jsonObject.getString("msg")).orElse(ERROR_MSG);
        //在异常情况下，且有通用上下文时，尝试记录异常信息，方便setRollbackOnly()后，返回业务提示
        if (ERROR_CODE.equals(code) && CommonParamDto.CommonParamContext.get() != null) {
            CommonParamDto.CommonParamContext.get().setMsg(msg);
        }
        TypeReference<T> clazz = null;
        return MesResultBean.<T>builder()
            .code(code)
            .msg(msg)
            .data(jsonObject.getObject("data", clazz))
            .traceId(TraceIdUtil.getTraceId())
            .build();
    }

    public static <T> MesResultBean<T> ok() {
        return build(SUCCESS_CODE, SUCCESS_MSG, null);
    }

    public static <T> MesResultBean<T> ok(String msg) {
        return build(SUCCESS_CODE, msg, null);
    }

    public static <T> MesResultBean<T> okAndLog(String msg) {
        log.info(msg);
        return build(SUCCESS_CODE, msg, null);
    }

    public static <T> MesResultBean<T> ok(T data) {
        return build(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static <T> MesResultBean<T> ok(String msg, T data) {
        return build(SUCCESS_CODE, msg, data);
    }

    public static <T> MesResultBean<T> fail() {
        return build(ERROR_CODE, ERROR_MSG, null);
    }


    public static <T> MesResultBean<T> fail(String msg) {
        return build(ERROR_CODE, msg, null);
    }

    public static <T> MesResultBean<T> fail(T data) {
        return build(ERROR_CODE, ERROR_MSG, data);
    }

    public static <T> MesResultBean<T> fail(String msg, T data) {
        return build(ERROR_CODE, msg, data);
    }
    public static <T> MesResultBean<T> fail(String code, String msg) {
        return build(code, msg, null);
    }

    public static <T> MesResultBean<T> failAndLog(String msg) {
        log.error(msg);
        return build(ERROR_CODE, msg, null);
    }
}