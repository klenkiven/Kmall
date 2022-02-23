package xyz.klenkiven.kmall.order.controller;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.klenkiven.kmall.common.exception.NoStockException;
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
    public String toTrade(@RequestParam(required = false) Integer errorCode, Model model) {
        OrderConfirmVO confirmVO = orderService.confirmOrder();
        log.info("orderConfirmData = {}", JSON.toJSONString(confirmVO));
        model.addAttribute("orderConfirmData", confirmVO);
        String errorMsg = null;
        if (errorCode != null) {
            switch (errorCode) {
                case 1:
                    errorMsg = "Submit too Frequently";
                    break;
                case 2:
                    errorMsg = "Price has changed";
                    break;
                case 3:
                    errorMsg = "There is no stock in order list";
                    break;
            }
        }
        model.addAttribute("errorMsg", errorMsg);
        return "confirm";
    }

    /**
     * Submit Order
     */
    @PostMapping("/orderSubmit")
    public String orderSubmit(OrderSubmitForm form, Model model, RedirectAttributes redirectAttributes) {
        log.info("Order Submit: {}", form);
        SubmitResultVO result;

        try {
            result = orderService.submitOrder(form);
        } catch (NoStockException e) {
            // NoStockException
            log.info("Order Submit Fail");
            redirectAttributes.addAttribute("errorCode", 3);
            return "redirect:http://order.kmall.com/toTrade";
        }

        // Fail to Oder Confirm Page
        if (result.getCode() != 0) {
            log.info("Order Submit Fail");
            redirectAttributes.addAttribute("errorCode", result.getCode());
            return "redirect:http://order.kmall.com/toTrade";
        }

        // Success to Payment Page
        log.info("Order Submit Success: {}", result);
        model.addAttribute("submitResult", result);
        return "pay";
    }


    public OrderWebController(OrderService orderService) {
        this.orderService = orderService;
    }

}
