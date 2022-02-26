package xyz.klenkiven.kmall.common.to.mq;

import lombok.Data;

/**
 * Stock Locked Transport Object
 * @author klenkiven
 */
@Data
public class StockLockedTO {

    /** Task Id */
    private Long taskId;

    /** Task Id */
    private StockDetailTO taskDetail;

}
