package xyz.klenkiven.kmall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;


/**
 * For Ajax Response
 * @author klenkiven
 */
@Data
public class FareResp {

    /** Address Info */
    private MemberAddressDTO address;

    /** Fare */
    private BigDecimal fare;

}
