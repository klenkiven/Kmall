package xyz.klenkiven.kmall.order.controller;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import xyz.klenkiven.kmall.order.model.form.OrderSubmitForm;
import xyz.klenkiven.kmall.order.model.vo.OrderConfirmVO;
import xyz.klenkiven.kmall.order.model.vo.SubmitResultVO;
import xyz.klenkiven.kmall.order.service.OrderService;

@Controller
public class OrderWebController {

    private static final Logger log = LoggerFactory.getLogger(OrderWebController.class);
    private final OrderService orderService;

    /**
     * From Cart to Trade
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVO confirmVO = orderService.confirmOrder();
        log.info("orderConfirmData = {}", JSON.toJSONString(confirmVO));
        model.addAttribute("orderConfirmData", confirmVO);
        return "confirm";
    }

    /**
     * Submit Order
     */
    @PostMapping("/orderSubmit")
    public String orderSubmit(OrderSubmitForm form) {
        SubmitResultVO result = orderService.submitOrder(form);

        // Fail to Oder Confirm Page
        if (result.getCode() != 0) {
            return "redirect:http://order.kmall.com/toTrade";
        }
        // Success to Payment Page
        return "pay";
    }


    public OrderWebController(OrderService orderService) {
        this.orderService = orderService;
    }

}
