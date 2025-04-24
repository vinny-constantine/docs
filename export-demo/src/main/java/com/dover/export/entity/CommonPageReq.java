
/**
 * @Description: 公共参数
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CommonPageReq extends CommonParamReq implements Serializable {
    /**
     * 当前页
     */
    @NotNull(message = "当前页不能为空")
    @Min(value = 1, message = "当前页不能小于1")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 500, message = "每页大小不能大于500")
    private Integer pageSize;

    /**
     * 分页起始行
     * @ignore
     */
    private Integer startIndex;
}