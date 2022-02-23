package xyz.klenkiven.kmall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * [FEIGN] Cart Item
 * @author klenkiven
 */
@Data
public class OrderItemDTO {

    private Long skuId;             // skuId
    private String title;           // 标题
    private String image;           // 图片
    private List<String> skuAttrValues;// 商品销售属性
    private BigDecimal price;       // 单价
    private Integer count;          // 当前商品数量
    private BigDecimal totalPrice;  // 总价
    private Boolean hasStock = false;// Has Stock
    private BigDecimal weight;      // Item weight

}
