
/**
 * 公共分页查询返回值
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonPageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String SUCCESS_CODE = "200";
    public static final String SUCCESS_MSG = "操作成功";
    public static final String ERROR_CODE = "400";
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
     * 数据
     */
    private CommonPage<T> data;

    public boolean isSuccess() {
        return "200".equals(this.code);
    }

    /**
     * 判断是否失败
     */
    public boolean isNg() {
        return !isSuccess();
    }

    public String getMessage() {
        return this.msg;
    }
    public static <T> CommonPageResult<T> build(String code, String msg, CommonPage data) {
        return builder().code(code).msg(msg).data(data).build();
    }

    public static <T> CommonPageResult<T> success() {
        return build("200", "操作成功", null);
    }

    public static <T> CommonPageResult<T> success(String msg) {
        return build("200", msg, null);
    }

    public static <T> CommonPageResult<T> success(CommonPage<T> data) {
        return build("200", "操作成功", data);
    }

    public static <T> CommonPageResult<T> success(String msg, CommonPage<T> data) {
        return build("200", msg, data);
    }

    public static <T> CommonPageResult<T> error() {
        return build("400", "操作失败", null);
    }

    public static <T> CommonPageResult<T> error(String msg) {
        return build("400", msg, null);
    }

    public static <T> CommonPageResult<T> error(CommonPage<T> data) {
        return build("400", "操作失败", data);
    }

    public static <T> CommonPageResult<T> error(String msg, CommonPage<T> data) {
        return build("400", msg, data);
    }

    public static <T extends CommonPageReq,V> CommonPageResult<V> okPage(T dto) {
        return okPage(dto, 0, new ArrayList<V>());
    }

    public static <T extends CommonPageReq,V> CommonPageResult<V> okPage(T dto, Integer totalSize, List<V> list) {
        CommonPage<V> page = new CommonPage<>();
        page.setPageNum(dto.getPageNum());
        page.setPageSize(dto.getPageSize());
        page.setTotal(totalSize);
        page.setList(list);

        return success(page);
    }
}
