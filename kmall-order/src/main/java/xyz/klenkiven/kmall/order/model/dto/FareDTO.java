package xyz.klenkiven.kmall.order.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Get Fare
 * @author klenkiven
 */
@Data
public class FareDTO {

    /** Address Info */
    private MemberAddressDTO address;

    /** Fare */
    private BigDecimal fare;

}
