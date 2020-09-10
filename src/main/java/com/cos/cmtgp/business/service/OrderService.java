package com.cos.cmtgp.business.service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import cn.hutool.core.util.ArrayUtil;
import com.cos.cmtgp.business.dto.*;
import com.cos.cmtgp.business.model.OrderBasic;
import com.cos.cmtgp.business.model.OrderDetail;
import com.cos.cmtgp.common.mini.SubscribeMessage;
import com.cos.cmtgp.common.mini.WXPayConstants;
import com.cos.cmtgp.common.mini.WXPayUtil;
import com.cos.cmtgp.common.util.DateUtil;
import com.cos.cmtgp.common.util.FreemarkUtil;
import com.cos.cmtgp.common.util.MiniUtil;
import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;



public class OrderService {

	private MiniService miniService = new MiniService();

	public OrderBasic selectOrder(String oId) {
		return OrderBasic.dao.findById(oId);
	}

	public boolean generateOrder(OrderBasic orderInfo) {
		orderInfo.set("order_time", new Date());
		return orderInfo.save();
	}

	public boolean updateOrderstatus(String oId, String orderStatus) {
		OrderBasic orderInfo = OrderBasic.dao.findById(oId);
		orderInfo.set("", orderStatus);
		return orderInfo.update();
	}

	public Map<String, Object> findPage(int pageSize, int pageNum, Map<String, String[]> paraMap, String orderBy) {
		StringBuffer buf = new StringBuffer("from t_order_basic a,t_user_setting u where a.u_id = u.u_id ");
		for (String paraName : paraMap.keySet()) {
			String prefix = "queryParams[";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					//处理范围参数
					if(field.startsWith("o_id")) {
						buf.append(" and a.o_id like '%"+value+"%'");
					}else if(field.startsWith("statDate")) {
						buf.append(" and DATE_FORMAT(a.order_time ,'%Y-%m-%d') >='"+value+"'");
					}else if(field.startsWith("endDate")) {
						buf.append(" and DATE_FORMAT(a.order_time ,'%Y-%m-%d') <='"+value+"'");
					}
				}
			}
		}
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = " ORDER BY a.order_time asc";
		}else {
			buf.append(" ORDER BY "+orderBy);
		}
		Page<Record>  pages = Db.use("cmtgp_base").paginate(pageNum, pageSize,"select a.`o_id`, a.`u_id`, CONCAT(a.`order_time`,'') order_time, a.`total_price`, a.`consignee_name`, a.`consignee_phone`, a.`consignee_address`, a.`payment_status`, a.`payment_channel`, a.`extra_payment`, a.`extra_time`, a.`extra_status`, a.`extra_channel`,a. `order_status`,u.u_nick_name uName ",buf.toString());
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", pages.getList());
		datagrid.put("total", pages.getTotalRow());
	return datagrid;
	}

	public Record logisticsFind(String oId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Object> findDetailPage(int pageSize, int pageNum, Map<String, String[]> paraMap, String orderBy) {
		StringBuffer buf = new StringBuffer("from t_order_detail u where 1=1 ");
		for (String paraName : paraMap.keySet()) {
			String prefix = "queryParams[";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					//处理范围参数
					if(field.startsWith("id")) {
						buf.append(" and u.o_id like '%"+value+"%'");
					}else if(field.startsWith("IDCard")) {
						buf.append(" and u.IDCard like '%"+value+"%'");
					}
				}
			}
		}
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = " ORDER BY u.id asc";
		}else {
			buf.append(" ORDER BY "+orderBy);
		}
		Page<Record>  pages = Db.use("cmtgp_base").paginate(pageNum, pageSize,"select u.* ",buf.toString());
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", pages.getList());
		datagrid.put("total", pages.getTotalRow());
	return datagrid;
	}

	/**
	 * APP
	 * 新增订单
	 * @param orderDTO
	 */
	@Before(Tx.class)
	public Map<String, Object> addOrder(OrderDTO orderDTO){
		Date now = new Date();
		String rdSeesion = FreemarkUtil.get3rdSeesion(32);
		OrderBasic basic = new OrderBasic()
				.set("u_id",orderDTO.getuId())
				.set("order_time",now)
				.set("total_price",orderDTO.getTotalPrice())
				.set("consignee_range_time",orderDTO.getRangeTime())
				.set("consignee_name",orderDTO.getName())
				.set("consignee_phone",orderDTO.getPhone())
				.set("consignee_address",orderDTO.getAddress())
				.set("payment_status",2)
				.set("out_trade_no",rdSeesion)
				.set("order_status",5);
        List<DetailDTO> details = orderDTO.getDetails();
        if(basic.save()){
            List<OrderDetail> detailList = new ArrayList<OrderDetail>();
            String ids = "";
            for(DetailDTO re : details){
                OrderDetail detail = new OrderDetail()
						.set("s_id",re.getsId())
						.set("o_id",basic.getOId())
						.set("payment_price",re.getPaymentPrice())
						.set("order_num",re.getOrderNum())
						.set("is_send",1)
                		.set("is_extra",0);
                detailList.add(detail);
                ids+=re.getsId()+",";
            }
            if(Db.batchSave(detailList,detailList.size()).length>0){
				Db.delete("delete from t_shopping_info where u_id="+orderDTO.getuId()+" and s_id in ("+ids.substring(0,ids.length()-1)+")");
				Map<String, Object> result = new HashMap<String, Object>();
				String openid = CacheKit.get("miniProgram", orderDTO.getToken());
				String totalFeed = orderDTO.getTotalPrice().multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
				Map<String, String> weixinPay = miniService.getWeixinPay(basic.getOId(),openid,rdSeesion, Integer.valueOf(totalFeed));
				result.put("oId",basic.getOId());
				result.put("data",weixinPay);
				return result;
			}
		}
		return null;
	}

	/**
	 * 商户
	 * APP
	 * 发送订单
	 */
	@Before(Tx.class)
	public boolean  sendOrder(Integer oId,Record record){
		if(Db.update("update t_order_detail set is_send=2 where id in ("+record.getStr("ids")+")")>0){
			//发送订阅
			Map<String, Object> one = new HashMap<String, Object>();
			one.put("value", oId);

			Map<String, Object> two = new HashMap<String, Object>();
			two.put("value", "￥" + record.get("total_price"));

			Map<String, Object> three = new HashMap<String, Object>();
			three.put("value", record.get("consignee_name"));

			Map<String, Object> four = new HashMap<String, Object>();
			four.put("value", record.get("consignee_phone"));

			Map<String, Object> five = new HashMap<String, Object>();
			five.put("value", "商品配送中，注意查收");

			MiniTempDataDTO dataDTO = new MiniTempDataDTO();
			dataDTO.setCharacter_string6(one);
			dataDTO.setAmount12(two);
			dataDTO.setName2(three);
			dataDTO.setPhone_number3(four);
			dataDTO.setThing5(five);
			List<Record> recordList = Db.find("select b.u_openid from t_order_basic a,t_user_setting b where a.u_id=b.u_id and a.o_id="+oId);
			new SubscribeMessage(oId, dataDTO,MiniUtil.SEND_TEMP,3,recordList.get(0).getStr("u_openid")).start();
			if(record.getInt("sum").intValue()==record.getInt("finish").intValue()){
				if(Db.update("update t_order_basic set order_status=2 where o_id="+oId)>0){
					return true;
				}
			}else{
				return true;
			}
		}
		return false;
	}

	/**
	 * 商家确认已送达
	 */
	@Before(Tx.class)
	public boolean confirmSend(List<Record> recordList,String openid,Integer oId){
		String ids= "";
		boolean flag = false;
		for(Record record : recordList){
			if(record.getStr("s_openid").equals(openid)){
				ids +=","+record.getStr("id");
			}else{
				flag = record.getInt("is_send")==3;
			}
		}
		if(Db.update("update t_order_detail set is_send=3 where id in ("+ids.substring(0,ids.length()-1)+")")>0){
			if(flag){
				if(Db.update("update t_order_basic set order_status=3 where o_id="+oId)>0){
					return true;
				}
			}else{
				return true;
			}
		}
		return false;
	}

	/**
	 * APP
	 * 商户返还差价
	 */
	@Before(Tx.class)
	public Map<String,Object> toBackPrice(Integer oId,String backPrice)throws Exception{
		Map<String,Object> resultMap = new HashMap<String, Object>();
		OrderBasic orderBasic = OrderBasic.dao.findById(oId);
		Map<String, String> reqData = new HashMap<String, String>();
		reqData.put("transaction_id", orderBasic.getTransactionId());
		reqData.put("out_trade_no", orderBasic.getOutTradeNo());
		String outRefundNo = FreemarkUtil.get3rdSeesion(32);
		orderBasic.setBackOutRefundNo(outRefundNo);
		reqData.put("out_refund_no", FreemarkUtil.get3rdSeesion(32));
		String totalFee = orderBasic.getTotalPrice().multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
		reqData.put("total_fee", totalFee);
		BigDecimal backPriceDecimal = new BigDecimal(backPrice);
		orderBasic.setTotalBackPrice(backPriceDecimal);
		String refundFee = backPriceDecimal.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
		reqData.put("refund_fee", refundFee);
		//发送返还差价
		String xmlStr = miniService.refund(reqData);
		Date now = new Date();
		miniService.addWXLog(oId,null,Integer.valueOf(refundFee),"商户返还差价",xmlStr,now);
		Map<String, String> refundResult = WXPayUtil.xmlToMap(xmlStr);
		if(refundResult.get("return_code").equals(WXPayConstants.SUCCESS)) {
			if (refundResult.get("result_code").equals(WXPayConstants.SUCCESS)) {
				String refundId = refundResult.get("refund_id");
				orderBasic.setBackRefundId(refundId);
				orderBasic.setBackPriceStatus(3);
			}
		}
		orderBasic.update();
		resultMap.put("code",1);
		resultMap.put("msg","微信退款异常");
		return resultMap;
	}

	/**
	 * APP
	 * 取消订单
	 * @param oid
	 * @return
	 */
	public boolean closeOrder(Integer oid){
		return Db.update("update t_order_basic set order_status=6 where o_id="+oid)>0;
	}

	/**
	 * APP
	 * 二次支付
	 * @param orderBasic
	 * @return
	 */
	public boolean extraOrder(OrderBasic orderBasic){
		orderBasic.setExtraTime(new Date());
		orderBasic.update();
		return true;
	}


	/**
	 * APP
	 * 查询订单
	 * @param pageNo
	 * @param pageSize
	 * @param userId
	 * @return
	 */
	public Page<Record> selectOrderBasicList(Integer pageNo,Integer pageSize,Integer userId,Integer status){
		String fromSql = " from t_order_basic where u_id="+userId;
		if(status!=-1){
			fromSql+=" and order_status="+status;
		}
		fromSql+=" order by order_time desc ";
		Page<Record> paginate = Db.paginate(pageNo, pageSize, "select o_id ",fromSql);
		List<Record> recordList = new ArrayList<Record>();
 		if(paginate.getList().size()>0){
			String oIds = "";
			for(Record record:paginate.getList()){
				oIds += record.getStr("o_id") + ",";
			}
			String sql = " select a.o_id,REPLACE(group_concat(concat('https://www.sotardust.cn/CMTGP/upload/',SUBSTRING_INDEX(c.s_address_img,'~',1))),',','~') as imgUrl,a.order_status" +
					" ,case a.order_status when 1 then '待发货' when 2 then '待收货' when 3 then '已送达' when 5 then " +
					" (case a.payment_status when 2 then '未支付' when 3 then '支付中' when 4 then '转入退款' when 5 then '支付失败' else '已支付' end ) " +
					" when 6 then '已取消' else '已关闭' end as status " +
					" ,sum(case b.is_extra when 0 then 0 else b.extra_price end) as extraPrice,a.order_time,a.total_price " +
					" FROM t_order_basic a " +
					" inner join t_order_detail b on a.o_id=b.o_id " +
					" inner join t_commodity_info c on b.s_id=c.s_id " +
					" where a.u_id="+userId+" and a.o_id in ("+oIds.substring(0,oIds.length()-1)+") " ;
			if(status!=-1){
				sql += " and a.order_status="+status;
			}
			sql += "  group by a.o_id  order by a.order_time desc ";
			recordList.addAll(Db.find(sql));
			for(Record record : recordList){
				Double extraPrice = record.getDouble("extraPrice");
				if(extraPrice<0){
					record.set("msg","重量有误差，退还￥"+extraPrice+"到我的账户");
				}

			}

		}
		Page<Record> result = new Page<Record>(recordList, paginate.getPageNumber(), paginate.getPageSize(), paginate.getTotalPage(), paginate.getTotalRow());
		return result;
	}

	/**
	 * APP
	 * 订单明细
	 * @param oId
	 * @return
	 */
	public Map<String,Object> queryOrderDetail(Integer oId){
        Map<String,Object> resultMap = new HashMap<String, Object>();
			String basicSql = "select a.consignee_name,a.consignee_phone,a.consignee_address,a.o_id,a.payment_channel,DATE_FORMAT(a.order_time,'%Y-%m-%d %T') as order_time,DATE_FORMAT(a.last_time,'%Y-%m-%d %T') as last_time,DATE_FORMAT(a.extra_time,'%Y-%m-%d %T') as extra_time,a.extra_payment,a.extra_channel" +
                ",a.order_status,case when b.account_price is null then 0 else b.account_price end as accountPrice,sum(case when c.chargeback_status is null then (case c.is_extra when 2 then c.extra_price else 0 end) else 0 end ) as extraPrice,a.total_price,a.consignee_range_time,a.out_trade_no,a.extra_status  " +
					" ,a.total_back_price,a.back_price_status,sum(case when c.chargeback_status =2 then c.payment_price else 0 end) as backPrice " +
					" ,sum(case when c.chargeback_status =2 then (case c.is_extra when 1 then c.extra_price else 0 end) else 0 end) chargeback_pay " +
					" ,sum(case when c.chargeback_status =2 then (case c.is_extra when 2 then c.extra_price else 0 end) else 0 end) chargeback_back " +
                " from t_order_basic a,t_user_setting b,t_order_detail c where a.o_id=c.o_id and a.u_id=b.u_id and a.o_id="+oId;
		List<Record> info = Db.find(basicSql);
		if(info.size()>0){
			Record record = info.get(0);
            resultMap.put("info",record);
            String sql = "select b.s_id,c.s_corporate_name,a.id,concat('https://www.sotardust.cn/CMTGP/upload/',SUBSTRING_INDEX(b.s_address_img,'~',1)) as imgUrl,concat(b.s_name,'￥',b.s_price,'/',b.s_unit) as name " +
                    " ,concat('￥',a.payment_price) as price,case b.init_unit when 0 then concat(a.order_num,'g') else concat(a.order_num,'个') end as num " +
                    " ,case a.is_extra when 2 then concat('实际重量:',a.extra_weight,'g,需支付￥',a.extra_price) when 1 then concat('实际重量:',a.extra_weight,'g,已退还￥',a.extra_price) else '' end as msg " +
                    " ,a.is_extra,case when a.extra_img_url is null then '' else a.extra_img_url end as extra_img_url,a.chargeback_status " +
                    " from t_order_detail a ,t_commodity_info b,t_supplier_setting c where b.p_id=c.s_id and a.s_id=b.s_id and a.o_id="+oId;
            List<Record> records = Db.find(sql);

            Map<String,List<Record>> map = new HashMap<String, List<Record>>();
            for(Record item : records){
				String extra_img_url = item.getStr("extra_img_url");
				if(!"".equals(extra_img_url)){
					item.set("extra_img_url",new ArrayList<String>(Arrays.asList(extra_img_url.split("~"))));
				}
				String s_corporate_name = item.getStr("s_corporate_name");
				if(map.containsKey(s_corporate_name)){
					map.get(s_corporate_name).add(item);
				}else{
					List<Record> re = new ArrayList<Record>();
					re.add(item);
					map.put(s_corporate_name,re);
				}
			}
			List<Record> result = new ArrayList<Record>();
			for(String str : map.keySet()){
 				Record item = new Record();
 				item.set("supplier",str);
 				item.set("goods",map.get(str));
 				result.add(item);
			}
            resultMap.put("detailList",result);
        }
        return resultMap;
	}



    /**
	 * APP
     * 同意退款
     * @return
     */
	@Before(Tx.class)
    public String agreeRefund(Integer id,Integer oId)throws Exception{
		String resultStr = "";
		OrderBasic orderBasic = OrderBasic.dao.findById(oId);
		if(orderBasic.getPaymentStatus().intValue()!=1){
			//未支付
			return "用户未支付";
		}
		Integer backPriceStatus = orderBasic.getBackPriceStatus();
		if(backPriceStatus!=null && backPriceStatus==4){
			//差价退还未完成
			return "有金额还未到账，请稍后再操作!";
		}
		Integer extraStatus= orderBasic.getExtraStatus();
		if(extraStatus!=null && extraStatus==3){
			//差价退还未完成
			return "有金额还未到账，请稍后再操作!";
		}
		String transactionId = orderBasic.getTransactionId();
		String outTradeNo = orderBasic.getOutTradeNo();
		OrderDetail orderDetail = OrderDetail.dao.findById(id);
		BigDecimal paymentPrice = orderDetail.getPaymentPrice();
		Integer isExtra = orderDetail.getIsExtra();
		BigDecimal extraPrice = orderDetail.getExtraPrice();
		//商户退回过差价
		if(backPriceStatus!=null && backPriceStatus==1 && isExtra==1){
			paymentPrice = paymentPrice.subtract(extraPrice);
		}
		String refundFee = paymentPrice.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
		BigDecimal totalPrice = orderBasic.getTotalPrice();
		String totalFee = totalPrice.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
		Map<String, String> reqData = new HashMap<String, String>();
		reqData.put("transaction_id", transactionId);
		reqData.put("out_trade_no", outTradeNo);
		String outRefundNo = FreemarkUtil.get3rdSeesion(32);
		orderDetail.setOutRefundNo(outRefundNo);
		reqData.put("out_refund_no",outRefundNo);
		reqData.put("total_fee", totalFee);
		reqData.put("refund_fee", refundFee);
		reqData.put("notify_url",MiniUtil.REFUND_NOTIFY_URL);
		/****发送WX退款****/
		String xmlStr = miniService.refund(reqData);
		Date now = new Date();
		miniService.addWXLog(oId,id,Integer.valueOf(refundFee),"商户同意退款",xmlStr,now);
		Map<String, String> refundResult = WXPayUtil.xmlToMap(xmlStr);

		if(refundResult.get("return_code").equals(WXPayConstants.SUCCESS)){
			if(refundResult.get("result_code").equals(WXPayConstants.SUCCESS)){
				//初始化退款中
				orderDetail.setChargebackStatus(3);
				//保存微信退款单号
				String refundId = refundResult.get("refund_id");
				orderDetail.setRefundId(refundId);
				/****有二次支付****/
				Map<String, String> extraRefundResult = new HashMap<String, String>();
				if(extraStatus!=null && extraStatus.intValue()==1 && isExtra==2){
					String extraTransactionId = orderBasic.getExtraTransactionId();
					BigDecimal extraPayment = orderBasic.getExtraPayment();
					String extraTotalFee = extraPayment.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
					String extraOutTradeNo = orderBasic.getExtraOutTradeNo();
					String extraRefundFee = extraPrice.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
					Map<String, String> extraData = new HashMap<String, String>();
					extraData.put("transaction_id", extraTransactionId);
					extraData.put("out_trade_no", extraOutTradeNo);
					String extraOutRefundNo = FreemarkUtil.get3rdSeesion(32);
					orderDetail.setExtraOutRefundNo(extraOutRefundNo);
					extraData.put("out_refund_no", extraOutRefundNo);
					extraData.put("total_fee", extraTotalFee);
					extraData.put("refund_fee", extraRefundFee);
					extraData.put("notify_url",MiniUtil.REFUND_NOTIFY_URL);
					//发送二次WX退款
					String refundStr = miniService.refund(extraData);
					miniService.addWXLog(oId,id,Integer.valueOf(extraRefundFee),"商户同意退款 - 退款二次支付差价",refundStr,now);
					extraRefundResult.putAll(WXPayUtil.xmlToMap(refundStr));
					if (extraRefundResult.get("return_code").equals(WXPayConstants.SUCCESS)) {
						if (extraRefundResult.get("result_code").equals(WXPayConstants.SUCCESS)) {
							String extraRefundId = extraRefundResult.get("refund_id");
							orderDetail.setExtraRefundId(extraRefundId);
							//初始化退款中
							orderDetail.setExtraPayBackStatus(3);
						} else {
							resultStr = "差价退还异常!";
						}
					} else {
						resultStr = "差价退还异常!";
					}
				}
				orderDetail.update();
				return resultStr;
			}
		}
        return "微信退款异常";
    }

	/**
	 * APP
	 * 商户通知用户补差价
	 */
	@Before(Tx.class)
    public boolean sendPayPrice(Integer oId,String payPrice,String orderTime){
		if (Db.update("update t_order_basic set extra_status=2,extra_payment=" + payPrice + " where o_id=" + oId) > 0) {
			if (Db.update("update t_order_detail set extra_pay_status=2 where is_extra=2 and o_id=" + oId) > 0) {
				//发送订阅
				Map<String, Object> one = new HashMap<String, Object>();
				one.put("value", "重量超出，需支付差价");

				Map<String, Object> two = new HashMap<String, Object>();
				BigDecimal bigDecimal = new BigDecimal(payPrice).setScale(2, RoundingMode.HALF_UP);
				two.put("value", "￥" + bigDecimal);

				Map<String, Object> three = new HashMap<String, Object>();
				three.put("value", oId);

				Map<String, Object> four = new HashMap<String, Object>();
				four.put("value", orderTime);

				MiniTempDataDTO dataDTO = new MiniTempDataDTO();
				dataDTO.setThing4(one);
				dataDTO.setAmount3(two);
				dataDTO.setNumber2(three);
				dataDTO.setTime1(four);
				List<Record> recordList = Db.find("select b.u_openid from t_order_basic a,t_user_setting b where a.u_id=b.u_id and a.o_id="+oId);
				new SubscribeMessage(oId, dataDTO,MiniUtil.EXTRA_PAY_TEMP,3,recordList.get(0).getStr("u_openid")).start();
				return true;
			}
		}
		return false;
	}

	/**
	 * 商户删除图片
	 */
	@Before(Tx.class)
	public boolean deletePic(Integer id,Integer idx,String url){
		OrderDetail detail = OrderDetail.dao.findById(id);
		String[] split = detail.getExtraImgUrl().split("~");
		String[] remove = ArrayUtil.remove(split, idx);
		String result = StringUtil.join(remove,"~");
		detail.setExtraImgUrl(result);
		if(new File(PathKit.getWebRootPath()+"/upload/"+url).delete()){
			if(detail.update()){
				return true;
			}
		}
		return false;
	}

}
