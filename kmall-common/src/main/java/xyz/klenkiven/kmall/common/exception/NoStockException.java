package xyz.klenkiven.kmall.common.exception;

/**
 * No Stock Exception
 * @author klenkiven
 */
public class NoStockException extends RuntimeException {
    private Long skuId;
    public NoStockException() {
        super();
    }
    public NoStockException(Long skuId) {
        super("There is no more stock for sku " + skuId);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
