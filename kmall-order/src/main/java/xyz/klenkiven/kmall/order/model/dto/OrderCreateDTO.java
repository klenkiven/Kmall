package xyz.klenkiven.kmall.order.model.dto;

import lombok.Data;
import xyz.klenkiven.kmall.order.entity.OrderEntity;
import xyz.klenkiven.kmall.order.entity.OrderItemEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order Create return Data
 * @author klenkiven
 */
@Data
public class OrderCreateDTO {

    /** Created Order Entity */
    private OrderEntity order;

    /** Purchased Item */
    private List<OrderItemEntity> orderItems;

    /** Current Pay Price */
    private BigDecimal payPrice;

    /** Fare Price */
    private BigDecimal fare;

}
