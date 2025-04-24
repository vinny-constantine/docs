
/**
 * 公共分页查询返回值
 */
@Data
@NoArgsConstructor
public class CommonPage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private int pageNum;

    /**
     * 每页条数
     */
    private int pageSize;

    /**
     * 总条数
     */
    private int total;

    /**
     * 总页数
     */
    private int pages;

    /**
     * 数据集合
     */
    private List<T> list;

    /**
     * 公共方法:设置总页数
     */
    public void setTotalPages(CommonPage omsPage) {
        int total = omsPage.getTotal();
        int pageSize = omsPage.getPageSize();

        if (total == 0 || pageSize == 0) {
            omsPage.setPages(0);
        } else {
            // totalPage 总页数
            int totalPage = 0;
            if (total > 0) {
                totalPage = total / pageSize;
                if (total % pageSize != 0) {
                    totalPage++;
                }
            }
            omsPage.setPages(totalPage);
        }
    }

    /**
     * 获取总页数时计算（勿删）
     */
    public int getPages() {
        if (this.pageSize == 0) {
            return 0;
        }
        int pages = this.total / this.pageSize;
        if (this.total % this.pageSize != 0) {
            pages++;
        }
        return pages;
    }
}
