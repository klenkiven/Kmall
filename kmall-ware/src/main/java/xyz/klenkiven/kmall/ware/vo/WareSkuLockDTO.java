package xyz.klenkiven.kmall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * Lock SKU Quantity for Order
 * @author klenkiven
 */
@Data
public class WareSkuLockDTO {

    /** Lock for Which Order */
    private String orderSn;

    /** Need to Lock */
    private List<OrderItemDTO> locks;

}
