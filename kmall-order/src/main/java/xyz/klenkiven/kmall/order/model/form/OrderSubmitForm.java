package xyz.klenkiven.kmall.order.model.form;

import lombok.Data;

/**
 * Submit Order Form
 * @author klenkiven
 */
@Data
public class OrderSubmitForm {

    /** Address Id */
    private Long addrId;

    /** Payment Type */
    private String payType;

    /** Idempotent Token */
    private String orderToken;

    /** Price in Page in order to Verify Price Validity */
    private String payPrice;

    /** Order Note */
    private String note;

    /* Reduce, Bill etc. */

}
