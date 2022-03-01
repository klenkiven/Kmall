package xyz.klenkiven.kmall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import xyz.klenkiven.kmall.order.model.vo.PayVO;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000119627206";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDQ/7f608Is0n/Prlz7WGZj/yAw+qHMlCM7h80qGuwx7SXDt2CV7EXgvOcK8gP6F05KMjOFMZdvCDjNPCOkh8Kf/JffDMvD1gok7KbYu2NQSesGlyQUPtWAZDwOIwphF0MEN4v12iYRIeg93TDQCgU95D4V8wEUgtPTkXy6c1D8AkrXF/iN9hDHpwFRazdDXEiAt8nM1Q72Ikq+kP9p5pLwfFHSw2/CE7KpEHCcmi4dmcd+6Hk/XDX2jQ+bA0uy51u+BENMRDOMuKZCjm0aecHVr0ov5I6drPgNISSTSBipUCaqs2scNIuOxLL7WfOU8MX2KSkGhEKeCkXXHatgFjFHAgMBAAECggEBAMUBRxtYoKA/i9roMR17poh75IxGtF1zBgMHb41Q83PcZsyl/pVTskq1xdO7baRAc7vctlPG+0feKCzNk4tzrTHBmCbYmPpwt/82U8/YUzVLeA8uSQQMdpQf57wrq4z5sGI5OeT1TQy5OYGXDnwL9gTxMNTfyxTafXHgogHe0We5zUlvJiRuOxAe2pLfkZ9g2Gb7T12WziwQ3lN/il7GI9rP2e4/9SA8XRqVjTcAOBLieUOzojM9Nsy+dCcLhLgA13SAy0CHRW7YKGB4aKT2hfHUcoG81FY4+g0v18F0Nr+/ZrOFBKQ+GZM3kNcw+GgOb3+YpYROHnca2J4wpxOLt4ECgYEA69543Gr5yq/vPO8VkgQ0TLuI0/uAMIbff+1QTAVUw+YuRgrGcgCkRCutKR2mn0HnmgFL11DDROcQl5bxt78zlOl8VxC0o20zjbsz0NFq0V2aVPknYnJENE0OQ1MV+jn0EGCU2+KV864GAJmOmNvmutShgBGSq1L+ymSrPL/NYaMCgYEA4tYofmJrmbV6/QApmdjFwUHzymr/r4u1lnxCf/KoNWjnyRZRlMuj0qV9os+WeJExXWclyoxFW172h+f1BLh4POVFja118eVykSqXX6DINsh3M9h1EykIjmqRGjNZeDG83UGEgFWnnqKt+KtiZY8sSpeVbhdoIZTvb85wSeBSlA0CgYEA6IPGRxsZpSosP7g8XlXQrLfOGZJ6+ttqJbTujBJRTlBx21Ax8h4Z0K3xpG97WtFgG1wdxGL0K89Qdu8w138fvTMyBK4ONHCo+pXJbZeTolKQmiWHIrAb9ibj4ZBdQO+yXhanh99lDPPHLP4zKRLV7+U+U35/RG6rhx8YAunc79sCgYEAsHEGpHAn5DL+zIbw1QOBpzaGzD94TPLuN3TGQUHdx1WnXYnnwUa5UiN520d/cp71eALAqZF8bXsrZkFuG9Lla/CmrnJrmNHx0KsBfdfRhJNf7UsEnfe/+Q85eNjSUiTWFIBe8Xgu3r0Jd7DP49F/ETJhgHXigKSZri00uLItaY0CgYBHzPg6B8QCwAxjNHFMrO4VMVSqzMY5erWeCXTfDGrDfrN9VvvLUgIzk2WzLDi3iG8mtXJXZQIqWXegl5Rjf1H39xt8e2+FWqKo/0MbA1irCAGalyvqT8GgrDJJYj5fTWNjMCXJWNVxahcs/uKkj/gMCYlCJ7cKtvIZFzjuaS7AgA==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiTVvm3aNO0ZhCGcrwhVcrRqhDk6W+xW2n9vB9V7LHf7O2tTZXl07mCMw3jHUg1lXu+UINuHZ7fMK4scQQ0Q/4/2rlCcwIKWTMV5sKV3rumNn29I81/5cDRvjelbUeoUB2JS+S5s2i0luQQNjVBiPGvoonNWpTFCWoGRUEakHKWi43xucjBzAHecTNyHOexZjTks+OTsXiEaF9JmscSCI3R3SeufsaKVrcVIXYavHZpJQlNIGWxAeeS3rvZ4sqv8gcjEDbiHtLy5CzErPzuWnjUaZzY+sPRA1EtIezPB0jSBLbqbZb6HBRgeS02heqBBMvki2OvWcpr4tocePpFB6cQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url = "http://order.kmall.com/notify/paySuccess";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.kmall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVO vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
