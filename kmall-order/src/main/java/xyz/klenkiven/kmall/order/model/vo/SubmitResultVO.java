package xyz.klenkiven.kmall.order.model.vo;

import lombok.Data;
import xyz.klenkiven.kmall.order.entity.OrderEntity;

/**
 * Submit Service Result
 * @author klenkiven
 */
@Data
public class SubmitResultVO {

    /** Order Entity Info */
    private OrderEntity order;

    /** Status Code */
    private Integer code;

}
