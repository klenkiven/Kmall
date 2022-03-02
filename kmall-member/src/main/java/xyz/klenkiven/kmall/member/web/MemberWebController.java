package xyz.klenkiven.kmall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.member.feign.OrderFeignService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Member Order List Web Page
 * @author klenkiven
 */
@Controller
public class MemberWebController {

    private final OrderFeignService orderFeignService;

    /**
     * Member Order Page
     */
    @GetMapping("memberOrder.html")
    public String memberOrderPage(@RequestParam(defaultValue = "1") String pageNum, Model model) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageNum);
        params.put("limit", "5");
        List<?> orders = orderFeignService.listOrderItem(params).getData().getList();
        model.addAttribute("orders", orders);
        return "list";
    }

    public MemberWebController(OrderFeignService orderFeignService) {
        this.orderFeignService = orderFeignService;
    }

}
