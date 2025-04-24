
import cn.hutool.core.util.NumberUtil;
import java.util.Optional;

/**
 * 分页工具类
 */
public final class PageUtils {

    /**
     * 计算起始条数，获取分页数据
     */
    public static <T extends CommonPageReq> void computePage(Integer totalSize, T t) {
        // 当前页
        int pageNum = Optional.ofNullable(t.getPageNum()).orElse(1);
        // 每页条数
        int pageSize = Optional.ofNullable(t.getPageSize()).orElse(20);
        // 总页数
        int totalPage = NumberUtil.ceilDiv(totalSize, pageSize);
        if (pageNum < 1 || pageNum > totalPage) {
            pageNum = 1;
        }
        // 当前页
        t.setPageNum(pageNum);
        // 每页条数
        t.setPageSize(pageSize);
        // 起始条数
        t.setStartIndex((pageNum - 1) * pageSize);
    }

}