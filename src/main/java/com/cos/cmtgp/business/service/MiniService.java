package com.cos.cmtgp.business.service;

import com.cos.cmtgp.business.model.GlobalConf;

import com.cos.cmtgp.business.model.WxCashInfo;
import com.cos.cmtgp.common.mini.WXPayConstants;
import com.cos.cmtgp.common.mini.WXPayUtil;
import com.cos.cmtgp.common.util.FreemarkUtil;
import com.cos.cmtgp.common.util.MiniUtil;
import com.cos.cmtgp.common.util.UrlUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.security.MessageDigest;
import java.util.*;


/**
 * @author pengxiangZhao
 * @Description: TODO
 * @ClassName: MiniService
 * @Date 2020/8/25 0025
 */
public class MiniService {

    /**
     * 退款
     * @param reqData
     * @return
     * @throws Exception
     */
    public String refund(Map<String, String> reqData) throws Exception{
        List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type in (3,4)");
        String cAppid="";
        String apiSecuret="";
        for(GlobalConf conf : confList){
            if(conf.getCType()==3){
                cAppid = conf.getCAppid();
            }else if(conf.getCType()==4){
                apiSecuret = conf.getApiSecuret();
            }
        }
        String rdSeesion = FreemarkUtil.get3rdSeesion(32);
        reqData.put("appid", cAppid);
        reqData.put("mch_id", MiniUtil.MERCHANT_NO);
        reqData.put("nonce_str", rdSeesion);
        reqData.put("sign_type", "MD5");
        reqData.put("sign", generateSignature(reqData, apiSecuret));
        String reqBody = WXPayUtil.mapToXml(reqData);
        return UrlUtil.sendPost(MiniUtil.REFUND_ORDER,reqBody,true);
    }


    public String generateSignature(final Map<String, String> data, String key) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals(WXPayConstants.FIELD_SIGN)) {
                continue;
            }
            // 参数值为空，则不参与签名
            if (data.get(k).trim().length() > 0){
                sb.append(k).append("=").append(data.get(k).trim()).append("&");
            }
        }
        sb.append("key=").append(key);
        return MD5(sb.toString()).toUpperCase();
    }

    public static String MD5(String data) throws Exception {
        java.security.MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }


    /**
     * 查询退款
     */
    public String queryRefundOrder(String refundId)throws Exception{
        List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type in (3,4)");
        String cAppid="";
        String apiSecuret="";
        for(GlobalConf conf : confList){
            if(conf.getCType()==3){
                cAppid = conf.getCAppid();
            }else if(conf.getCType()==4){
                apiSecuret = conf.getApiSecuret();
            }
        }
        Map<String, String> reqData = new HashMap<String, String>();
        reqData.put("appid", cAppid);
        reqData.put("mch_id", MiniUtil.MERCHANT_NO);
        reqData.put("refund_id",refundId);
        reqData.put("nonce_str",FreemarkUtil.get3rdSeesion(32));
        reqData.put("sign_type", "MD5");
        reqData.put("sign", generateSignature(reqData, apiSecuret));
        String reqBody = WXPayUtil.mapToXml(reqData);
        return UrlUtil.sendPost(MiniUtil.CHECK_REFUND_ORDER,reqBody,true);

    }


    /**
     * 关闭订单
     */
    public String closedOrder(String outRradeNo) throws Exception {
        List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type in (3,4)");
        String cAppid="";
        String apiSecuret="";
        for(GlobalConf conf : confList){
            if(conf.getCType()==3){
                cAppid = conf.getCAppid();
            }else if(conf.getCType()==4){
                apiSecuret = conf.getApiSecuret();
            }
        }
        String rdSeesion = FreemarkUtil.get3rdSeesion(32);
        Map<String, String> reqData = new HashMap<String, String>();
        reqData.put("appid", cAppid);
        reqData.put("mch_id", MiniUtil.MERCHANT_NO);
        reqData.put("out_trade_no",outRradeNo);
        reqData.put("nonce_str",rdSeesion);
        reqData.put("sign_type", "MD5");
        reqData.put("sign", generateSignature(reqData, apiSecuret));
        String reqBody = WXPayUtil.mapToXml(reqData);
        return UrlUtil.sendPost(MiniUtil.CLOSED_ORDER,reqBody,false);
    }

    /**
     * 查询订单
     * @param outRradeNo
     * @return
     * @throws Exception
     */
    public String queryOrder(String outRradeNo)throws Exception{
        List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type in (3,4)");
        String cAppid="";
        String apiSecuret="";
        for(GlobalConf conf : confList){
            if(conf.getCType()==3){
                cAppid = conf.getCAppid();
            }else if(conf.getCType()==4){
                apiSecuret = conf.getApiSecuret();
            }
        }
        Map<String, String> reqData = new HashMap<String, String>();
        reqData.put("appid", cAppid);
        reqData.put("mch_id", MiniUtil.MERCHANT_NO);
        reqData.put("out_trade_no",outRradeNo);
        reqData.put("nonce_str",FreemarkUtil.get3rdSeesion(32));
        reqData.put("sign_type", "MD5");
        reqData.put("sign", generateSignature(reqData, apiSecuret));
        String reqBody = WXPayUtil.mapToXml(reqData);
        return UrlUtil.sendPost(MiniUtil.CHECK_ORDER,reqBody,false);
    }

    /**
     * 统一下单
     */
    @Before(Tx.class)
    public Map<String,String> getWeixinPay(Integer oId,String openid,String tradeNo, Integer totalFee){
        try {
            List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type in (3,4)");
            String cAppid="";
            String apiSecuret="";
            for(GlobalConf conf : confList){
                if(conf.getCType()==3){
                    cAppid = conf.getCAppid();
                }else if(conf.getCType()==4){
                    apiSecuret = conf.getApiSecuret();
                }
            }
            Map<String, String> reqData = new HashMap<String, String>();
            reqData.put("appid", cAppid);
            reqData.put("mch_id", MiniUtil.MERCHANT_NO);
            reqData.put("nonce_str", FreemarkUtil.get3rdSeesion(32));
            reqData.put("body", "食朝夕-小程序支付");
            reqData.put("out_trade_no", tradeNo);
            reqData.put("total_fee", totalFee.toString());
            reqData.put("spbill_create_ip", "192.168.1.142");
            reqData.put("notify_url", MiniUtil.NOTIFY_URL);
            reqData.put("trade_type", "JSAPI");
            reqData.put("openid", openid);
            reqData.put("sign_type", "MD5");
            reqData.put("sign", generateSignature(reqData, apiSecuret));
            String reqBody = WXPayUtil.mapToXml(reqData);
            String xmlStr = UrlUtil.sendPost(MiniUtil.UNIFIED_ORDER,reqBody,false);
            this.addWXLog(oId,null,totalFee,"统一下单",xmlStr,new Date());
            Map<String, String> respData = WXPayUtil.xmlToMap(xmlStr);
            String RETURN_CODE = "return_code";
            String return_code;
            if (respData.containsKey(RETURN_CODE)) {
                return_code = respData.get(RETURN_CODE);
            } else {
                throw new Exception(String.format("No `return_code` in XML: %s", xmlStr));
            }
            if (return_code.equals(WXPayConstants.FAIL)) {
                return respData;
            }else if (return_code.equals(WXPayConstants.SUCCESS)) {
                //二次签名
                String time = String.valueOf(System.currentTimeMillis() / 1000);
                String rdSeesion = FreemarkUtil.get3rdSeesion(32);
                Map<String, String> signData = new HashMap<String, String>();
                signData.put("appId",cAppid);
                signData.put("timeStamp",time);
                signData.put("nonceStr",rdSeesion);
                signData.put("package","prepay_id="+respData.get("prepay_id"));
                signData.put("signType","MD5");
                Map<String, String> secondData = new HashMap<String, String>();
                secondData.put("timeStamp",time);
                secondData.put("nonceStr",rdSeesion);
                secondData.put("package","prepay_id="+respData.get("prepay_id"));
                secondData.put("signType","MD5");
                secondData.put("paySign",generateSignature(signData, apiSecuret));
                return secondData;
            }else {
                throw new Exception(String.format("return_code value %s is invalid in XML: %s", return_code, xmlStr));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 增加操作日志
     */
    public void addWXLog(Integer basicId,Integer detailId,Integer detalPrice,String desc,String result,Date now) {
        try {
            WxCashInfo cashInfo = new WxCashInfo();
            cashInfo.setBasicId(basicId);
            if(detailId!=null){
                cashInfo.setDetailId(detailId);
            }
            if(detalPrice!=null){
                cashInfo.setDealPrice(detalPrice);
            }
            cashInfo.setDesc(desc);
            cashInfo.setResult(result);
            cashInfo.setCreateDate(now);
            cashInfo.save();
        }catch(Exception ex){

        }
    }

}
