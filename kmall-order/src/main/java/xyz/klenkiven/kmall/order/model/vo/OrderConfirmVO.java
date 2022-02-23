package xyz.klenkiven.kmall.order.model.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import xyz.klenkiven.kmall.order.model.dto.MemberAddressDTO;
import xyz.klenkiven.kmall.order.model.dto.OrderItemDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Order Confirm Page View Object
 * @author klenkiven
 */
public class OrderConfirmVO {

    /** User's Address */
    @Getter @Setter
    private List<MemberAddressDTO> address;

    /** All Checked Purchase Item */
    @Getter @Setter
    private List<OrderItemDTO> items;

    /** 优惠券（会员积分） **/
    @Getter @Setter
    private Integer integration;

    /** Has Stock Map */
    @Setter @Getter
    private Map<Long, Boolean> hasStockMap;

    /** Idempotent Token */
    @Getter @Setter
    private String token;

    /** Total Item Count */
    public Integer getTotalCount() {
        if (items == null || items.size() == 0) { return 0; }
        int count = 0;
        for (OrderItemDTO i : items) {
            count += i.getCount();
        }
        return count;
    }

    /** 总商品金额 **/
    //BigDecimal total;
    //计算订单总额
    public BigDecimal getTotal() {
        BigDecimal totalNum = BigDecimal.ZERO;
        if (items != null && items.size() > 0) {
            for (OrderItemDTO item : items) {
                //计算当前商品的总价格
                BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                //再计算全部商品的总价格
                totalNum = totalNum.add(itemPrice);
            }
        }
        return totalNum;
    }

    /** 应付总额 **/
    //BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        return getTotal();
    }

}
