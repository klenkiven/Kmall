package xyz.klenkiven.kmall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;


/**
 * Get Fare
 * @author klenkiven
 */
@Data
public class FareResp {

    /** Address Info */
    private MemberAddressDTO address;

    /** Fare */
    private BigDecimal fare;

}
