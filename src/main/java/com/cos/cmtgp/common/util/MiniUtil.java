package com.cos.cmtgp.common.util;

/**
 * @author pengxiangZhao
 * @Description: TODO
 * @ClassName: MiniUtil
 * @Date 2020/8/24 0024
 */
public class MiniUtil {
    //商户号
    public static final String MERCHANT_NO = "1602070933";
    //微信支付通知回调地址
    public static final String NOTIFY_URL = "https://www.sotardust.cn/CMTGP//mini/wxNotify";
    public static final String REFUND_NOTIFY_URL = "https://www.sotardust.cn/CMTGP//mini/wxRefundNotify";
    //订单查询
    public static final String CHECK_ORDER = "https://api.mch.weixin.qq.com/pay/orderquery";
    //统一下单
    public static final String UNIFIED_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    //关闭订单
    public static final String CLOSED_ORDER = "https://api.mch.weixin.qq.com/pay/closeorder";
    //申请退款
    public static final String REFUND_ORDER = "https://api.mch.weixin.qq.com/secapi/pay/refund";
    //查询退款
    public static final String CHECK_REFUND_ORDER = "https://api.mch.weixin.qq.com/pay/refundquery";

    //二次支付通知模板,走短信
    public static final String EXTRA_PAY_TEMP = "c-wwagnYAUYK0dj5QeEjvT64J_P39vNTnXiHs3EXVgA";
    //退款成功通知模板
    public static final String REFUND_SUCCESS_TEMP = "jq5UENIsQBT7dg8AwBj2MVd7GJpcEl8oQm7ztx_FPDA";
    //发货通知模板，不要
    public static final String SEND_TEMP = "xq__fUa5dSTSkOautbRcm9R9Y9ynSOeD4Ooh8roxctc";


    //二次支付成功通知模板，不要
    public static final String EXTRA_PAY_SUCCESS_TEMP = "fSY6OIzxAN8Ru7aFUNvwBUD80i561FaqwzkwIG_sNJQ";
    //申请退款模板，走短信
    public static final String REFUND_TEMP = "94fg3W3PWhDWzpZ_W5upX3megk0dxBL47w9w0SsmAKo";
    //新订单模板，走短信
    public static final String NEW_ORDER = "6Pcxa3JKbmABTMowyVr_8hACo9u3xiAm3p80Y6DIycQ";


}
