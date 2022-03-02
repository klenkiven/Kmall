package xyz.klenkiven.kmall.order.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Alipay Return Callback VO
 * @author klenkiven
 */
@Data
public class PayReturnVO {

    /** Alipay Trade No */
    private String trade_no;

    /** Order SN */
    private String out_trade_no;

    /** Total Amount */
    private BigDecimal total_amount;

    /** Subject */
    private String subject = "KMall Payment";

    /** Callback Time */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;
}
