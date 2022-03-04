package xyz.klenkiven.kmall.seckill.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import xyz.klenkiven.kmall.common.to.UserLoginTO;
import xyz.klenkiven.kmall.common.utils.Result;
import xyz.klenkiven.kmall.common.to.SeckillSkuRedisTO;
import xyz.klenkiven.kmall.seckill.interceptor.UserLoginInterceptor;
import xyz.klenkiven.kmall.seckill.service.SeckillService;

import java.util.List;

/**
 * Seckill Controller
 * @author klenkiven
 */
@Controller
public class SeckillController {

    private static final Logger log = LoggerFactory.getLogger(SeckillController.class);
    private final SeckillService seckillService;

    /**
     * [FEIGN] Get All Current Seckill SKU
     */
    @GetMapping("/getCurrentSeckillSkus")
    @ResponseBody
    public Result<List<SeckillSkuRedisTO>> getCurrentSeckillSkus() {
        List<SeckillSkuRedisTO> result = seckillService.getCurrentSeckillSkus();
        return Result.ok(result);
    }

    /**
     * [FEIGN] Get Seckill SKU in Redis
     */
    @GetMapping("/sku/seckill/{skuID}")
    @ResponseBody
    public Result<SeckillSkuRedisTO> getSkuSeckill(@PathVariable Long skuID) {
        SeckillSkuRedisTO result = seckillService.getSkuSeckill(skuID);
        return Result.ok(result);
    }

    /**
     * [Intercepted]
     * Seckill Request
     */
    @GetMapping("/kill")
    public String seckill(@RequestParam String killId,
                          @RequestParam String key,
                          @RequestParam Integer num,
                          Model model) {
        UserLoginTO userInfo = UserLoginInterceptor.loginUser.get();
        log.info("User {} Request: killId={}, key={}, num={}", userInfo.getId(), killId, key, num);
        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

}
