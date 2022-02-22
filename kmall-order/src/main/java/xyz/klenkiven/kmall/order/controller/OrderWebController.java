package xyz.klenkiven.kmall.order.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.klenkiven.kmall.order.model.vo.OrderConfirmVO;
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
        log.info("orderConfirmData = {}", confirmVO);
        model.addAttribute("orderConfirmData", confirmVO);
        return "confirm";
    }

    public OrderWebController(OrderService orderService) {
        this.orderService = orderService;
    }

}
