package xyz.klenkiven.kmall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Member Order List Web Page
 * @author klenkiven
 */
@Controller
public class MemberWebController {

    /**
     * Member Order Page
     */
    @GetMapping("memberOrder.html")
    public String memberOrderPage() {
        return "list";
    }

}
