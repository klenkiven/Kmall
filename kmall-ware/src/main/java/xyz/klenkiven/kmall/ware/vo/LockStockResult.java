package xyz.klenkiven.kmall.ware.vo;

import lombok.Data;

@Data
public class LockStockResult {

    private Long skuId;
    private Integer num;
    private Boolean locked;

}
