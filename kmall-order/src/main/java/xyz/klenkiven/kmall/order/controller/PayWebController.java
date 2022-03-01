package xyz.klenkiven.kmall.order.controller;

import com.alipay.api.AlipayApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.klenkiven.kmall.order.config.AlipayTemplate;
import xyz.klenkiven.kmall.order.model.vo.PayVO;
import xyz.klenkiven.kmall.order.service.OrderService;

/**
 * Pay Related Operation Controller
 * @author klenkiven
 */
@Controller
public class PayWebController {

    private static final Logger log = LoggerFactory.getLogger(PayWebController.class);
    private final AlipayTemplate alipayTemplate;
    private final OrderService orderService;

    /**
     * Return a html page for redirect
     */
    @GetMapping(value = "payOrder", produces = "text/html")
    @ResponseBody
    public String parOrder(@RequestParam String orderSn) throws AlipayApiException {
        PayVO payVO = orderService.getOrderPay(orderSn);
        // It will return a html page (For short default success)
        return alipayTemplate.pay(payVO);
    }

    public PayWebController(AlipayTemplate alipayTemplate,
                            OrderService orderService) {
        this.alipayTemplate = alipayTemplate;
        this.orderService = orderService;
    }

}
