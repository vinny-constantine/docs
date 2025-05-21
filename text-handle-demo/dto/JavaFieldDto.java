
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author wangwei
 * @date 2025-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class JavaFieldDto {

    /**
     * 字段名,如：skuName
     */
    private String name;

    /**
     * 字段类型,如：String
     */
    private String type;

    /**
     * 字段描述,如：商品名称
     */
    private String desc;

    /**
     * 字段层级,如：1
     */
    private Integer level;

    /**
     * 子字段
     */
    private List<JavaFieldDto> children;
}
