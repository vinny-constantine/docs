
@Data
public class CommonParamDto implements Serializable {
    /**
     * 租户id
     */
    private Long tenantId;
    /**
     * 租户code
     */
    private String tenantCode;
    /**
     * 当前操作人Id
     *
     * @ignore
     */
    private Long operateUserId;
    /**
     * 当前操作人名称
     *
     * @ignore
     */
    private String operateUserName;

    /**
     * 登录token
     *
     * @ignore
     */
    private String ucSsoTokenId;
    
    /**
     * 异常信息（方便在有事务的情况下，setRollBackOnly()后，能获取到业务提示错误）
     *
     * @ignore
     */
    private String msg;

    /**
     * 继承其他commonParamDto的参数
     */
    public <T extends CommonParamDto> T inherit(CommonParamDto commonParamDto) {
        this.tenantId = commonParamDto.getTenantId();
        this.tenantCode = commonParamDto.getTenantCode();
        this.operateUserId = commonParamDto.getOperateUserId();
        this.operateUserName = commonParamDto.getOperateUserName();
        this.ucSsoTokenId = commonParamDto.getUcSsoTokenId();
        this.msg = commonParamDto.getMsg();
        return (T) this;
    }

    
    public static class CommonParamContext {
        private static final TransmittableThreadLocal<CommonParamDto> context = new TransmittableThreadLocal<>();

        public static void set(CommonParamDto commonParamDto) {
            CommonParamContext.context.set(commonParamDto);
        }

        public static CommonParamDto get() {
            return context.get();
        }

        public static void clear() {
            context.remove();
        }
    }
}