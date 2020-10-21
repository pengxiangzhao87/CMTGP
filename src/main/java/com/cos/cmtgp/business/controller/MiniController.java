package com.cos.cmtgp.business.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cos.cmtgp.business.dto.MiniTempDataDTO;
import com.cos.cmtgp.business.model.GlobalConf;
import com.cos.cmtgp.business.model.OrderDetail;
import com.cos.cmtgp.business.model.UserSetting;
import com.cos.cmtgp.business.service.MiniService;
import com.cos.cmtgp.business.service.OrderService;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.mini.AES;
import com.cos.cmtgp.common.mini.SubscribeMessage;
import com.cos.cmtgp.common.mini.WXPayConstants;
import com.cos.cmtgp.common.mini.WXPayUtil;
import com.cos.cmtgp.common.util.*;
import com.cos.cmtgp.common.vo.User;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

public class MiniController extends BaseController {
    Logger logger = LogManager.getLogger(getClass());
    MiniService miniService = enhance(MiniService.class);
    OrderService orderService = enhance(OrderService.class);


    /**
     * 二次支付
     */
    public void extraPayment(){
        Integer oId = getParaToInt("oId");
        String totalPrice = getPara("totalPrice");
        String token = getPara("token");
        String openid = CacheKit.get("miniProgram", token);
        try{
            String rdSeesion = FreemarkUtil.get3rdSeesion(32);
            String totalFee = new BigDecimal(totalPrice).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
            Map<String, String> weixinPay = miniService.getWeixinPay(oId,openid,rdSeesion, Integer.valueOf(totalFee));
            if(weixinPay!=null){
                Db.update("update t_order_basic set extra_payment="+totalPrice+",extra_status=2,extra_channel=1,extra_out_trade_no='"+rdSeesion+"' where o_id="+oId);
                Db.update("update t_order_detail set extra_pay_status=2 where is_extra=2 and o_id="+oId);
            }
            renderSuccess("",weixinPay);
        }catch(Exception ex){
            addOpLog("extraPayment ===> oId="+oId+",openid="+openid);
            ex.printStackTrace();
            renderFailed();
        }

    }


    /**
     * APP
     * 继续支付
     */
    public void continuePayment(){
        Integer oId = getParaToInt("oId");
        String token = getPara("token");
        String openid = CacheKit.get("miniProgram", token);
        try{
            String tradeNo = FreemarkUtil.get3rdSeesion(32);
            List<Record> records = Db.find("select out_trade_no,convert(total_price*100,SIGNED) as total_price from t_order_basic where o_id=" + oId);
            if(records.size()==1){
                Record record = records.get(0);
                Integer totalPrice = Integer.valueOf(record.get("total_price").toString());
                //重新统一支付
                Map<String, String> weixinPay = miniService.getWeixinPay(oId,openid,tradeNo,totalPrice);
                if(weixinPay!=null){
                    Db.update("update t_order_basic set out_trade_no='"+tradeNo+"' where o_id="+oId);
                }
                renderSuccess("",weixinPay);
            }else{
                renderFailed();
            }
        }catch(Exception ex){
            addOpLog("continuePayment ===> oId="+oId+",openid="+openid);
            ex.printStackTrace();
            renderFailed();
        }
    }

    /**
     * 关闭订单
     */
    public void closeOrder(){
        String outRradeNo = getPara("outRradeNo");
        Integer oId = getParaToInt("oId");
        try {
            String result = miniService.closedOrder(outRradeNo);
            miniService.addWXLog(oId,null,null,"用户取消订单",result,new Date());
            orderService.closeOrder(oId);
            renderSuccess();
        } catch (Exception e) {
            addOpLog("closeOrder ===> oId="+oId+",outRradeNo="+outRradeNo);
            e.printStackTrace();
            renderFailed();
        }
    }

    /**
     * 查询订单状态
     */
    public void queryPayOrder(){
        Integer oId = getParaToInt("oId");
        //1:下单查询，继续支付查询，2：二次支付查询
        Integer type = getParaToInt("type");
        String token = getPara("token");
        String openid = CacheKit.get("miniProgram", token);
        String resultStr = "支付中";
        try {
            //等微信支付结果回调
            Thread.sleep(1000);
            StringBuffer sb = new StringBuffer(" select a.order_time,a.consignee_name,a.consignee_phone,a.payment_status,a.order_status,a.out_trade_no,a.extra_status,a.extra_out_trade_no,convert(a.total_price*100,SIGNED) as allPrice ");
            sb.append(" ,sum(b.payment_price) as total_price,convert(sum(b.payment_price)*100,SIGNED) as totalPrice ");
            sb.append(" ,a.extra_payment,convert(a.extra_payment*100,SIGNED) as extraPayment,a.extra_time ,d.s_openid,d.s_id,d.s_phone ");
            sb.append(" from t_order_basic a,t_order_detail b,t_commodity_info c,t_supplier_setting d ");
            sb.append(" where a.o_id=b.o_id and b.s_id=c.s_id and c.p_id=d.s_id and a.o_id= "+oId);
            sb.append(" GROUP BY d.s_id ");
            List<Record> records = Db.find(sb.toString());
            if(records.size()>0){
                Record record = records.get(0);
                //发送短信
                PhoneVerificationCode.sendMini(record.getStr("s_phone"),oId.toString(),1);
                Integer paymentStatus = record.getInt("payment_status");
                Integer orderStatus = record.getInt("order_status");
                Integer extraStatus = record.getInt("extra_status");
                if(type==1 && paymentStatus==1 && orderStatus==1 || type==2 && extraStatus==1){
                    resultStr = "支付成功";
                }else{
                    //主动查询
                    resultStr = this.checkOrderStatus(record, type, oId, openid);
                    if("支付成功".equals(resultStr)){
                        //发送短信
                        PhoneVerificationCode.sendMini(record.getStr("s_phone"),oId.toString(),1);
                        //发送订阅
                        //this.afterPaySendMessage(records,oId,type);
                    }
                }
            }else{
                resultStr = "没有找到订单";
            }
        } catch (Exception e) {
            addOpLog("queryPayOrder ===> oId="+oId+",openid="+openid);
            e.printStackTrace();
        }
        renderSuccess(resultStr);

    }


    /**
     * 支付成功发送订阅消息
     * @param records
     * @param oId
     * @param type
     */
    private void afterPaySendMessage(List<Record> records,Integer oId,Integer type){
        for(Record item : records){
            //发送订阅
            Map<String, Object> idMap = new HashMap<String, Object>();
            idMap.put("value", oId);
            Map<String, Object> priceMap = new HashMap<String, Object>();
            priceMap.put("value", "￥" + (type==1?item.get("total_price"):item.get("extra_payment")));
            Map<String, Object> nameMap = new HashMap<String, Object>();
            nameMap.put("value", item.get("consignee_name"));
            MiniTempDataDTO dataDTO = new MiniTempDataDTO();
            if(type==1){
                Map<String, Object> phoneMap = new HashMap<String, Object>();
                phoneMap.put("value", item.get("consignee_phone"));
                Map<String, Object> markMap = new HashMap<String, Object>();
                try {
                    String orderTime = DateUtil.getDayToString(DateUtil.getStringToDate(item.getStr("order_time")));
                    markMap.put("value", orderTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                dataDTO.setName5(nameMap);
                dataDTO.setPhone_number7(phoneMap);
                dataDTO.setAmount3(priceMap);
                dataDTO.setCharacter_string1(idMap);
                dataDTO.setDate4(markMap);
                new SubscribeMessage(oId, dataDTO,MiniUtil.NEW_ORDER,2,item.getStr("s_openid")).start();
            }else if(item.getInt("s_id")==1){
                Map<String, Object> statusMap = new HashMap<String, Object>();
                statusMap.put("value", "已支付");
                Map<String, Object> timeMap = new HashMap<String, Object>();
                try {
                    String extraTime = DateUtil.getDayToString(DateUtil.getStringToDate(item.getStr("extra_time")));
                    timeMap.put("value", extraTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                dataDTO.setAmount3(priceMap);
                dataDTO.setPhrase5(statusMap);
                dataDTO.setName1(nameMap);
                dataDTO.setTime11(timeMap);
                dataDTO.setCharacter_string2(idMap);
                new SubscribeMessage(oId, dataDTO,MiniUtil.EXTRA_PAY_SUCCESS_TEMP,2,item.getStr("s_openid")).start();
            }
        }
    }

    /**
     * 主动查询订单状态
     * @param record
     * @param type
     * @param oId
     * @param openid
     * @return
     * @throws Exception
     */
    private String checkOrderStatus(Record record,Integer type,Integer oId,String openid) throws Exception{
        int totalFee = 0;
        String tradeNo = "";
        if(type==1){
            tradeNo = record.get("out_trade_no").toString();
            totalFee = record.getInt("allPrice");
        }else{
            tradeNo = record.get("extra_out_trade_no").toString();
            totalFee = record.getInt("extraPayment");
        }
        String resultCheck = miniService.queryOrder(tradeNo);
        miniService.addWXLog(oId,null,Integer.valueOf(totalFee),"查询订单状态",resultCheck,new Date());
        Map<String, String> respData = WXPayUtil.xmlToMap(resultCheck);
        if(respData.get("return_code").equals(WXPayConstants.SUCCESS) ){
            if(respData.get("result_code").equals(WXPayConstants.SUCCESS) && respData.get("trade_state").equals(WXPayConstants.SUCCESS)){
                if(respData.get("openid").equals(openid) && totalFee==Integer.valueOf(respData.get("total_fee")).intValue()){
                    String tradeState = respData.get("trade_state");
                    String result = tradeState.equals("SUCCESS") ? "支付成功" : (tradeState.equals("REFUND") ? "转入退款" : (tradeState.equals("NOTPAY") ? "未支付" : (tradeState.equals("CLOSED") ? "已关闭" : (tradeState.equals("USERPAYING") ? "支付中" : "支付失败"))));
                    //支付状态(1:已支付，2:未支付，3支付中，4转入退款，5支付失败,6支付关闭)
                    Integer status = tradeState.equals("SUCCESS") ? 1 : (tradeState.equals("REFUND") ? 4 : (tradeState.equals("NOTPAY") ? 2 : (tradeState.equals("CLOSED") ? 6 : (tradeState.equals("USERPAYING") ? 3 : 5))));
                    StringBuffer updateSql = new StringBuffer("update t_order_basic set ");
                    if(type==1){
                        Integer paymentStatus =  status==1?1:5;
                        updateSql.append(" payment_status="+paymentStatus+",order_status="+status+",transaction_id='"+respData.get("transaction_id")+"' ");
                    }else{
                        updateSql.append(" extra_time=now(),extra_status="+status+",extra_transaction_id='"+respData.get("transaction_id")+"' ");
                        Db.update("update t_order_detail set extra_pay_status="+status+" where is_extra=2 and o_id="+oId);
                    }
                    updateSql.append(" where o_id="+oId);
                    Db.update(updateSql.toString());
                    return result;
                }else{
                    return "支付金额不符!";
                }
            }else {
                return "支付中!";
            }
        }
        return "支付中!";
    }


    /**
     *  微信支付回调结果通知
     */
    public void wxNotify(){
        // 获取微信POST过来反馈信息
        String resultStr = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        HttpServletRequest request = getRequest();
        String inputLine;
        String notityXml = "";
        try {
            while ((inputLine = request.getReader().readLine()) != null) {
                notityXml += inputLine;
            }
            Date now = new Date();
            request.getReader().close();
            if(StringUtil.isNotEmpty(notityXml)){
                Map<String, String> respData = WXPayUtil.xmlToMap(notityXml);
                List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type=4 ");
                String apiSecuret = confList.get(0).getApiSecuret();
                if(respData.get("return_code").equals(WXPayConstants.SUCCESS) && WXPayUtil.isSignatureValid(respData,apiSecuret) && respData.get("result_code").equals(WXPayConstants.SUCCESS)){
                    int totalFee = Integer.valueOf(respData.get("total_fee")).intValue();
                    String transactionId = respData.get("transaction_id");
                    String outTradeNo = respData.get("out_trade_no");
                    List<Record> recordList = Db.find(" select a.consignee_name,a.consignee_phone,a.o_id,a.payment_status,a.order_status,a.total_price as paymentPrice,a.extra_status,a.extra_payment,a.out_trade_no,a.extra_out_trade_no,d.s_openid " +
                            " ,a.order_time,sum(b.payment_price) as total_price,convert(sum(b.payment_price)*100,SIGNED) as totalPrice " +
                            " ,convert(a.extra_payment*100,SIGNED) as extraPayment,a.extra_time,d.s_phone " +
                            " from t_order_basic a,t_order_detail b,t_commodity_info c,t_supplier_setting d " +
                            " where a.o_id=b.o_id and b.s_id=c.s_id and c.p_id=d.s_id and a.out_trade_no='" + outTradeNo + "' or a.extra_out_trade_no='"+ outTradeNo +"' GROUP BY d.s_id");
                    if(recordList.size()>0){
                        Record record = recordList.get(0);
                        if(record.getStr("out_trade_no").equals(outTradeNo)){
                            int totalPrice = record.getBigDecimal("paymentPrice").multiply(new BigDecimal("100")).stripTrailingZeros().intValue();
                            if(totalPrice == totalFee){
                                Integer paymentStatus = recordList.get(0).getInt("payment_status");
                                Integer orderStatus = recordList.get(0).getInt("order_status");
                                if((paymentStatus==2 || paymentStatus==3) && orderStatus==5){
                                    Db.update("update t_order_basic set payment_status=1,order_status=1,transaction_id='"+transactionId+"' where out_trade_no='" + outTradeNo + "'");
                                    //发送订阅
                                    //this.afterPaySendMessage(recordList,record.getInt("o_id"),1);
                                    //发送短信
                                    PhoneVerificationCode.sendMini(record.getStr("s_phone"),record.getStr("o_id"),1);
                                }
                                miniService.addWXLog(null,null,null,"微信支付回调",notityXml,now);
                            }else{
                                miniService.addWXLog(null,null,null,"微信支付回调 - 金额不符",notityXml,now);
                                resultStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[金额不符]]></return_msg></xml>";
                            }
                        }else if(record.getStr("extra_out_trade_no").equals(outTradeNo)){
                            int extraPayment = record.getBigDecimal("extra_payment").multiply(new BigDecimal("100")).stripTrailingZeros().intValue();
                            if(extraPayment == totalFee){
                                Integer extraStatus = recordList.get(0).getInt("extra_status");
                                if(extraStatus==2 || extraStatus==3){
                                    Db.update("update t_order_basic set extra_status=1,extra_time=now(),extra_transaction_id='"+transactionId+"' where extra_out_trade_no='" + outTradeNo + "'");
                                    Db.update("update t_order_detail set extra_pay_status=1 where is_extra=2 and o_id="+record.getInt("o_id"));
                                    //发送订阅
                                    //this.afterPaySendMessage(recordList,record.getInt("o_id"),2);
                                }
                                miniService.addWXLog(null,null,null,"微信二次支付回调",notityXml,now);
                            }else{
                                miniService.addWXLog(null,null,null,"微信二次支付回调 - 金额不符",notityXml,now);
                                resultStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[金额不符]]></return_msg></xml>";
                            }
                        }
                    }else{
                        logger.error("微信支付回调 - 没找到订单 outTradeNo="+outTradeNo);
                        miniService.addWXLog(null,null,null,"微信支付回调 - 没找到订单",notityXml,now);
                        resultStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[没有找到订单]]></return_msg></xml>";
                    }
                }else{
                    String errDes = respData.get("err_code_des");
                    miniService.addWXLog(null,null,null,"微信支付回调 - 失败",notityXml,now);
                    resultStr ="<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA["+errDes+"]]></return_msg></xml>";
                }
            }else{
                miniService.addWXLog(null,null,null,"微信支付回调 - 微信没有返回数据","",now);
                resultStr ="<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[没有获取到数据]]></return_msg></xml>";
            }
        } catch (Exception e) {e.printStackTrace(); }
        renderSuccess(resultStr);
    }



    /**
     * 查询退款
     */
    public void queryRefundOrder(){
        Integer oId = getParaToInt("oId");
        Integer id = getParaToInt("id");
        //1：退款，2：返还差价
        Integer type = getParaToInt("type");
        String resultStr = "退款中";
        try {
            Thread.sleep(1000);
            Date now = new Date();
            if(type==1){
                List<Record> recordList = Db.find(" select b.o_id,b.payment_price,b.chargeback_status,b.refund_id,b.is_extra,b.extra_pay_back_status,b.extra_refund_id,c.u_openid,b.extra_price,b.extra_back_status,a.total_back_price " +
                        " from t_order_basic a,t_order_detail b,t_user_setting c where a.o_id=b.o_id and a.u_id=c.u_id and b.id=" + id);
                Record record = recordList.get(0);
                if(record.getInt("chargeback_status")==2){
                    resultStr = "退款成功";
                }else{
                    //查询退款
                    String refundCheckStr = miniService.queryRefundOrder(record.getStr("refund_id"));
                    miniService.addWXLog(oId, id,null, "退款 - 查询结果", refundCheckStr, now);
					Map<String, String> refundMap = WXPayUtil.xmlToMap(refundCheckStr);
					if (refundMap.get("return_code").equals(WXPayConstants.SUCCESS)) {
						if (refundMap.get("result_code").equals(WXPayConstants.SUCCESS)) {
							String refundStatus = refundMap.get("refund_status_0");
							Integer chargebackStatus = refundStatus.equals(WXPayConstants.SUCCESS) ? 2 : (refundStatus.equalsIgnoreCase("CHANGE") ? 4 : 3);
                            resultStr = refundStatus.equals(WXPayConstants.SUCCESS) ? "退款成功" : (refundStatus.equalsIgnoreCase("CHANGE") ? "退款异常" : "退款处理中");
                            Db.update("update t_order_detail set chargeback_status="+chargebackStatus+" where id="+id);
                            if(record.getInt("is_extra")!=2 || record.getInt("is_extra")==2 && record.getInt("extra_pay_back_status")!=null && record.getInt("extra_pay_back_status")==2){
                                this.updateOrderClose(oId,id);
                            }
                            if(chargebackStatus==2){
                                this.afterRefundSendMessage(record,type);
                            }
						}
					}
                }
                //二次支付退款
                if(record.get("is_extra")!=null && record.getInt("is_extra")==2 && record.getInt("extra_pay_back_status")!=null ){
                    if(record.getInt("extra_pay_back_status")!=2){
                        String extraRefundCheckStr = miniService.queryRefundOrder(record.getStr("extra_refund_id"));
                        miniService.addWXLog(oId, id,null, "二次支付退单 - 查询结果", extraRefundCheckStr, now);
                        Map<String, String> extraRefundMap = WXPayUtil.xmlToMap(extraRefundCheckStr);
                        if (extraRefundMap.get("return_code").equals(WXPayConstants.SUCCESS)) {
                            if (extraRefundMap.get("result_code").equals(WXPayConstants.SUCCESS)) {
                                String extraRefundStatus = extraRefundMap.get("refund_status_0");
                                Integer extraStatus = extraRefundStatus.equals(WXPayConstants.SUCCESS) ? 2 : (extraRefundStatus.equalsIgnoreCase("CHANGE") ? 4 : 3);
                                resultStr = extraRefundStatus.equals(WXPayConstants.SUCCESS) ? "退款成功" : (extraRefundStatus.equalsIgnoreCase("CHANGE") ? "退款异常" : "退款处理中");
                                Db.update("update t_order_detail set extra_pay_back_status="+extraStatus+" where id="+id);
                                if(record.getInt("chargeback_status")==2){
                                    this.updateOrderClose(oId,id);
                                }
//                                if(extraStatus==2){
//                                    this.afterRefundSendMessage(record,2);
//                                }
                            }
                        }
                    }
                }
            }else{
                List<Record> recordList = Db.find("select a.o_id,a.back_price_status,a.back_refund_id,a.total_back_price ,b.u_openid from t_order_basic a,t_user_setting b where a.u_id=b.u_id and a.o_id=" + oId);
                Record record = recordList.get(0);
                if(record.getInt("back_price_status")==2){
                    resultStr = "退款成功";
                }else{
                    String refundCheckStr = miniService.queryRefundOrder(record.getStr("back_refund_id"));
                    miniService.addWXLog(oId, id,null, "商户返还差价 - 查询结果", refundCheckStr, now);
                    Map<String, String> refundMap = WXPayUtil.xmlToMap(refundCheckStr);
                    if (refundMap.get("return_code").equals(WXPayConstants.SUCCESS)) {
                        if (refundMap.get("result_code").equals(WXPayConstants.SUCCESS)) {
                            String refundStatus = refundMap.get("refund_status_0");
                            Integer chargebackStatus = refundStatus.equals(WXPayConstants.SUCCESS) ? 2 : (refundStatus.equalsIgnoreCase("CHANGE") ? 4 : 3);
                            resultStr = refundStatus.equals(WXPayConstants.SUCCESS) ? "退款成功" : (refundStatus.equalsIgnoreCase("CHANGE") ? "退款异常" : "退款处理中");
                            Db.update("update t_order_basic set back_price_status="+chargebackStatus+" where o_id="+oId);
                            Db.update("update t_order_detail set extra_back_status="+chargebackStatus+" where id="+id);
                            resultStr = "退款成功";
//                            this.afterRefundSendMessage(record,type);
                        }
                    }
                }
            }
        }catch (Exception ex){ex.printStackTrace();}
        renderSuccess(resultStr);
    }

    /**
     * 退款成功发送订阅消息
     * @param record
     * @param type
     */
    private void afterRefundSendMessage(Record record,Integer type){
        //1：退单，2：返还差价

        Map<String, Object> idMap = new HashMap<String, Object>();
        idMap.put("value", record.getInt("o_id"));
        Map<String, Object> amountMap = new HashMap<String, Object>();
        Map<String, Object> thingMap = new HashMap<String, Object>();
        if(type==1){
            BigDecimal paymentPrice = record.getBigDecimal("payment_price");
            thingMap.put("value", "商品退款");
            if(record.getInt("is_extra")==1 && record.getInt("extra_back_status")!=null && record.getInt("extra_back_status")==2){
                BigDecimal extraPrice = record.getBigDecimal("extra_price");
                paymentPrice = paymentPrice.subtract(extraPrice);
                thingMap.put("value", "商品退款，扣除商家支付的￥"+extraPrice+"差价");
            }
            amountMap.put("value", "￥"+paymentPrice);
        }else{
            amountMap.put("value", "￥"+record.getBigDecimal("total_back_price"));
            thingMap.put("value", "重量不足，退回差价");
        }
        Map<String, Object> dateMap = new HashMap<String, Object>();
        try {
            dateMap.put("value", DateUtil.getDayToString(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Map<String, Object> phraseMap = new HashMap<String, Object>();
        phraseMap.put("value", "直接退款");
        MiniTempDataDTO dataDTO = new MiniTempDataDTO();
        dataDTO.setCharacter_string1(idMap);
        dataDTO.setAmount3(amountMap);
        dataDTO.setDate2(dateMap);
        dataDTO.setPhrase4(phraseMap);
        dataDTO.setThing5(thingMap);
        new SubscribeMessage(record.getInt("o_id"), dataDTO,MiniUtil.REFUND_SUCCESS_TEMP,3,record.getStr("u_openid")).start();
    }


    /**
     * 微信退款回调
     */
    public void wxRefundNotify(){
        // 获取微信POST过来反馈信息
        String returnStr = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        HttpServletRequest request = getRequest();
        String inputLine;
        String notityXml = "";
        try {
            while ((inputLine = request.getReader().readLine()) != null) {
                notityXml += inputLine;
            }
            Date now = new Date();
            request.getReader().close();
            if (StringUtil.isNotEmpty(notityXml)) {
                Map<String, String> respData = WXPayUtil.xmlToMap(notityXml);
                List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type=4 ");
                String apiSecuret = confList.get(0).getApiSecuret();
                /**
                 * 解密方式
                 * 解密步骤如下：
                 * （1）对加密串A做base64解码，得到加密串B
                 * （2）对商户key做md5，得到32位小写key* ( key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置 )
                 * （3）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）
                 */
                String req_info = respData.get("req_info");
                String decode = AES.decode(req_info, apiSecuret);
                if(!"".equals(decode)){
                    Map<String, String> decodeData = WXPayUtil.xmlToMap(decode);
                    String transactionId = decodeData.get("transaction_id");
                    String outTradeNo = decodeData.get("out_trade_no");
                    String refundId = decodeData.get("refund_id");
                    String outRefundNo = decodeData.get("out_refund_no");
                    int totalFee = Integer.valueOf(decodeData.get("total_fee")).intValue();
                    int refundFee = Integer.valueOf(decodeData.get("refund_fee")).intValue();
                    int refundStatus = decodeData.get("refund_status").equals("SUCCESS")?2:(decodeData.get("refund_status").equals("CHANGE")?4:5);
                    StringBuffer sb = new StringBuffer("select a.o_id,b.id,a.total_price,a.total_back_price,b.payment_price,b.is_extra,b.extra_price,b.extra_back_status,c.u_openid ");
                    sb.append(" ,a.transaction_id,a.out_trade_no ");
                    sb.append(" ,a.back_refund_id,a.back_price_status,a.back_out_refund_no ");
                    sb.append(" ,b.refund_id,b.chargeback_status,b.out_refund_no ");
                    sb.append(" ,b.extra_refund_id,b.extra_pay_back_status,b.extra_out_refund_no ");
                    sb.append(" ,a.extra_transaction_id,a.extra_out_trade_no ");
                    sb.append(" from t_order_basic a,t_order_detail b,t_user_setting c ");
                    sb.append(" where  a.o_id=b.o_id and a.u_id=c.u_id and ( ");
                    sb.append(" (a.back_refund_id = '"+refundId+"' and a.back_out_refund_no='"+outRefundNo+"') ");
                    sb.append(" or (b.refund_id='"+refundId+"' and b.out_refund_no='"+outRefundNo+"') ");
                    sb.append(" or (b.extra_refund_id='"+refundId+"' and b.extra_out_refund_no='"+outRefundNo+"')) ");
                    List<Record> recordList = Db.find(sb.toString());
                    if(recordList.size()>0){
                        Record record = recordList.get(0);
                        int oId = record.getInt("o_id");
                        String backRefundId = record.getStr("back_refund_id");
                        String backOutRefundNo = record.getStr("back_out_refund_no");
                        String refund = record.getStr("refund_id");
                        String refundNo = record.getStr("out_refund_no");
                        String extraRefundId = record.getStr("extra_refund_id");
                        String extraOutRefundNo = record.getStr("extra_out_refund_no");
                        if(record.getStr("transaction_id").equals(transactionId) && record.getStr("out_trade_no").equals(outTradeNo)
                            || record.getStr("extra_transaction_id").equals(transactionId) && record.getStr("extra_out_trade_no").equals(outTradeNo)){
                            /*************分量不足，退款差价**************/
                            if(backRefundId!=null && backOutRefundNo!=null && backRefundId.equals(refundId) && backOutRefundNo.equals(outRefundNo)){
                                int backPriceStatus = record.getInt("back_price_status");
                                int totalBackPrice = record.getBigDecimal("total_back_price").multiply(new BigDecimal("100")).stripTrailingZeros().intValue();
                                if(totalBackPrice==refundFee){
                                    if(backPriceStatus!=2 && backPriceStatus!=4 && backPriceStatus!=5){
                                        Db.update("update t_order_basic set back_price_status=" + refundStatus + " where o_id="+oId);
                                        Db.update("update t_order_detail set extra_back_status=" + refundStatus + " where is_extra=1 and o_id="+oId);
//                                        this.afterRefundSendMessage(record,2);
                                    }
                                    miniService.addWXLog(oId,null,refundFee,"微信差价退款回调",decode,now);
                                }else{
                                    miniService.addWXLog(oId,null,refundFee,"微信差价退款回调 - 价格不符",decode,now);
                                    returnStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[价格不符]]></return_msg></xml>";
                                }
                            /************退款***************/
                            }else if(refund.equals(refundId) && refundNo.equals(outRefundNo)){
                                Integer isExtra = record.getInt("is_extra");
                                Integer extraBackStatus = record.getInt("extra_back_status");
                                BigDecimal paymentPrice = record.getBigDecimal("payment_price");
                                if(isExtra==1 && (extraBackStatus==2 || extraBackStatus==3) ){
                                    BigDecimal extraPrice = record.getBigDecimal("extra_price");
                                    paymentPrice = paymentPrice.subtract(extraPrice);
                                }
                                Integer chargebackStatus = record.getInt("chargeback_status");
                                if(paymentPrice.multiply(new BigDecimal("100")).stripTrailingZeros().intValue()==refundFee){
                                    if(chargebackStatus!=2 && chargebackStatus!=4 && chargebackStatus!=5){
                                        Db.update("update t_order_detail set chargeback_status=" + refundStatus + " where id="+record.getInt("id"));
                                        Integer extraPayBackStatus = record.getInt("extra_pay_back_status");
                                        if(isExtra==2 ){
                                            if( extraPayBackStatus==2 ){
                                                //所有商品都退款，关闭订单
                                                this.updateOrderClose(oId,record.getInt("id"));
                                                this.afterRefundSendMessage(record,1);
                                            }
                                        }else{
                                            //所有商品都退款，关闭订单
                                            this.updateOrderClose(oId,record.getInt("id"));
                                            this.afterRefundSendMessage(record,1);
                                        }
                                    }
                                    miniService.addWXLog(oId,record.getInt("id"),refundFee,"微信退款回调",decode,now);
                                }else{
                                    miniService.addWXLog(oId,record.getInt("id"),refundFee,"微信退款回调 - 价格不符",decode,now);
                                    returnStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[价格不符]]></return_msg></xml>";
                                }
                            /**************二次支付退款****************/
                            }else if(extraRefundId!=null && extraOutRefundNo!=null && extraRefundId.equals(refundId) && extraOutRefundNo.equals(outRefundNo)){
                                Integer extraPayBackStatus = record.getInt("extra_pay_back_status");
                                int paymentPrice = record.getBigDecimal("extra_price").multiply(new BigDecimal("100")).stripTrailingZeros().intValue();
                                if(paymentPrice==refundFee){
                                    if(extraPayBackStatus!=2 && extraPayBackStatus!=4 && extraPayBackStatus!=5){
                                        Db.update("update t_order_detail set extra_pay_back_status=" + refundStatus + " where is_extra=2 and id="+record.getInt("id"));
                                        Integer chargebackStatus = record.getInt("chargeback_status");
                                        if(chargebackStatus==2){
                                            //所有商品都退款，关闭订单
                                            this.updateOrderClose(oId,record.getInt("id"));
//                                            this.afterRefundSendMessage(record,1);
                                        }
                                    }
                                    miniService.addWXLog(oId,record.getInt("id"),refundFee,"微信二次支付退款回调",decode,now);
                                }else{
                                    miniService.addWXLog(oId,record.getInt("id"),refundFee,"微信二次支付退款回调 - 价格不符",decode,now);
                                    returnStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[价格不符]]></return_msg></xml>";
                                }
                            }else{
                                miniService.addWXLog(null,null,null,"微信退款回调 - 订单不匹配",decode,now);
                            }
                        }else{
                            miniService.addWXLog(null,null,null,"微信退款回调 - 订单不符",decode,now);
                        }
                    }else{
                        miniService.addWXLog(null,null,null,"微信退款回调 - 没找到订单",decode,now);
                    }
                }else{
                    miniService.addWXLog(null,null,null,"微信退款回调 - 解密失败",decode,now);
                    returnStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>";
                }
            }else{
                miniService.addWXLog(null,null,null,"微信退款回调 - 微信没有返回数据","",now);
                returnStr = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[没有获取到数据]]></return_msg></xml>";
            }
        }catch (Exception ex){ex.printStackTrace();}
        renderSuccess(returnStr);
    }

    /**
     * 是否关闭订单、送达订单
     * @param oId
     * @param id
     */
    private void updateOrderClose(Integer oId,Integer id){
        //查询没有退单完成的
        List<Record> recordList = Db.find("select count(1) as totalSum,sum(case is_send when 1 then 0 else 1 end) as arriveSum from t_order_detail where o_id = " + oId + " and (chargeback_status<>2 or chargeback_status is null) ");
        if(recordList.size()>0) {
            Record Record = recordList.get(0);
            Integer totalSum = Record.getInt("totalSum");
            BigDecimal arriveSum = Record.getBigDecimal("arriveSum");
            if(totalSum==0 ){
                Db.update("update t_order_basic set order_status=4 where o_id = " + oId);
            }else if(totalSum.intValue()==arriveSum.intValue()){
                Db.update("update t_order_basic set order_status=3 where o_id = " + oId);
            }
        }
    }

    /**
     * 用户获取openid
     */
    public void getOpendId(){
        String code = getPara("code");
        Integer type = getParaToInt("type");
        List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type="+type);
        String cAppid = confList.get(0).getCAppid();
        String cSecret = confList.get(0).getCSecret();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+cAppid+"&secret="+cSecret+"&js_code="+code+"&grant_type=authorization_code";
        String result = UrlUtil.getAsText(url);
        if(!"".equals(result)){
            Map<String,Object> parse = (Map<String,Object>) JSON.parse(result);
            String openid = (String)parse.get("openid");
            String sessionKey = (String)parse.get("session_key");
            List<Record> recordList = Db.find("select * from t_user_setting where u_openid='"+openid+"'");
            String rdSeesion = FreemarkUtil.get3rdSeesion(128);
            CacheKit.put("miniProgram",rdSeesion,openid);
            CacheKit.put("miniProgram",openid,sessionKey);
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("token",rdSeesion);
            String areaFlag = "0,1,2,3";//0:即时配送，1：北京，2：京津冀，3: 无限制
            if(recordList.size()>0){
                map.put("uId",recordList.get(0).get("u_id"));
                if(recordList.get(0).get("u_phone")!=null && !"".equals(recordList.get(0).getStr("u_phone"))){
                    map.put("isPhone",0);
                }else{
                    map.put("isPhone",1);
                }
                List<Record> records = Db.find("select * from t_address_info where is_used=1 and u_id=" + recordList.get(0).get("u_id"));
                if(records.size()>0){
                    Record record = records.get(0);
                    String aCity = record.get("a_city").toString();
                    Integer distance = Integer.valueOf(record.get("distance").toString());
                    if(!aCity.contains("北京") && !aCity.contains("天津") && !aCity.contains("河北")){
                        areaFlag = "3";
                    }else{
                        if(aCity.contains("北京") && distance>3000){
                            areaFlag = "1,2,3";
                        }else if(distance<=3000){
                            areaFlag = "0,1,2,3";
                        }else{
                            areaFlag = "2,3";
                        }
                    }
                }
            }else{
                UserSetting userSetting = new UserSetting();
                userSetting.setUOpenid(openid);
                userSetting.save();
                map.put("uId",userSetting.getUId());
                map.put("isPhone",1);
            }
            map.put("areaFlag",areaFlag);
            renderSuccess("",map);
        }else{
            renderFailed();
        }
    }

    /**
     * mini
     * 授权用户
     */
    public void getUserInfo(){
        String encryptedData = getPara("encryptedData");
        String iv = getPara("iv");
        String token = getPara("token");
        String openid = CacheKit.get("miniProgram", token);
        String key = CacheKit.get("miniProgram", openid);
        try {
            byte[] resultByte = AES.decrypt(Base64.decodeBase64(encryptedData),
                    Base64.decodeBase64(key),
                    Base64.decodeBase64(iv));
            if (null != resultByte && resultByte.length > 0) {
                String userInfo = new String(resultByte, "UTF-8");
                JSONObject userJson = JSON.parseObject(userInfo);
                String nickName = userJson.getString("nickName");
                String avatarUrl = userJson.getString("avatarUrl");
                String path = (PathKit.getWebRootPath()+"/upload/").replace("\\", "/");
                URL httpurl = new URL(avatarUrl);
                String fileName = System.currentTimeMillis()+"_avatar.png";
                File f = new File(path + fileName);
                FileUtils.copyURLToFile(httpurl, f);
                if(Db.update("update t_user_setting set u_nick_name='"+nickName+"',u_avatar_url='"+fileName+"' where u_openid='"+openid+"'")>0){
                    renderSuccess(fileName);
                }else{
                    renderFailed();
                }
            } else {
                renderFailed();
            }
        }catch (Exception ex){
            renderFailed();
        }
    }

    /**
     * mini
     * 授权手机号
     */
    public void getPhone(){
        String encryptedData = getPara("encryptedData");
        String iv = getPara("iv");
        String token = getPara("token");
        String openid = CacheKit.get("miniProgram", token);
        String key = CacheKit.get("miniProgram", openid);
        try {
            byte[] resultByte = AES.decrypt(org.apache.commons.codec.binary.Base64.decodeBase64(encryptedData),
                    org.apache.commons.codec.binary.Base64.decodeBase64(key),
                    Base64.decodeBase64(iv));
            if (null != resultByte && resultByte.length > 0) {
                String userInfo = new String(resultByte, "UTF-8");
                JSONObject userJson = JSON.parseObject(userInfo);
                String phone = userJson.getString("phoneNumber");
                List<Record> recordList = Db.find("select * from t_user_setting where u_openid='" + openid + "'");
                Map<String,Object> map = new HashMap<String, Object>();
                if(recordList.size()>0){
                    Integer uId = recordList.get(0).getInt("u_id");
                    if(Db.update("update t_user_setting set u_phone='"+phone+"' where u_id="+uId)>0){
                        map.put("uId",uId);
                        map.put("isPhone",0);
                        renderSuccess("",map);
                    }else{
                        renderFailed();
                    }
                }else{
                    renderFailed();
                }
            } else {
                renderFailed();
            }
        }catch (Exception ex){
            renderFailed();
        }
    }

    public void checkToken(){
        String token = getPara("token");
        String openid = CacheKit.get("miniProgram", token);
        if(openid==null){
            renderFailed();
        }else{
            renderSuccess();
        }
    }

}
