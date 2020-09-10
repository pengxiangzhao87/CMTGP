package com.cos.cmtgp.business.service;


import com.cos.cmtgp.business.model.HotissueBasic;
import com.cos.cmtgp.common.util.DateUtil;
import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealtimeService {

    public void selectHotissue(Integer flag){
        StringBuffer sql = new StringBuffer("select * from t_hotissue_basic where 1=1");
        //推荐
        if(flag==0){
            sql.append(" order by h_appreciate_no desc,h_discuss_no desc ");
        }else{
            //所在社区
            sql.append(" ");
        }
        List<HotissueBasic> hotissueBasicList = HotissueBasic.dao.find(sql.toString());
    }

	public Record userStatistics(String statDate, String endDate) {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(1) total,(select count(DISTINCT u_id) from t_shopping_info where 1=1 ");
		if(!StringUtil.isEmpty(statDate)&&!StringUtil.isEmpty(endDate)) {
			sql.append("and DATE_FORMAT(date_time,'%Y-%m-%d') >= '"+statDate+"' and DATE_FORMAT(date_time,'%Y-%m-%d') <= '"+endDate+"'");
		}
		sql.append(") intention,(select count(DISTINCT u_id) from t_order_basic  where 1=1 ");
		if(!StringUtil.isEmpty(statDate)&&!StringUtil.isEmpty(endDate)) {
			sql.append("and DATE_FORMAT(order_time,'%Y-%m-%d') >= '"+statDate+"' and DATE_FORMAT(order_time,'%Y-%m-%d') <= '"+endDate+"'");
		}
		sql.append(") fixed from t_user_setting u");
		Record  record = Db.use("cmtgp_base").findFirst(sql.toString());
		return record;
	}

	public Record transactionStatistics(String statDate, String endDate) {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(1) total,count(CASE WHEN payment_status = 1 THEN 1 END) paidNum,count(CASE WHEN payment_status = 2 THEN 1 END) unpaidNum,");
		sql.append("count(CASE WHEN order_status = 1 THEN 1 END) consignment,count(CASE WHEN order_status = 2 THEN 1 END) distribution,count(CASE WHEN order_status = 3 THEN 1 END) delivered,count(CASE WHEN order_status = 4 THEN 1 END) closed from t_order_basic where 1=1 ");
		if(!StringUtil.isEmpty(statDate)&&!StringUtil.isEmpty(endDate)) {
			sql.append("and DATE_FORMAT(order_time,'%Y-%m-%d') >= '"+statDate+"' and DATE_FORMAT(order_time,'%Y-%m-%d') <= '"+endDate+"'");
		}
		Record  record = Db.use("cmtgp_base").findFirst(sql.toString());
		return record;
	}
	
	public Record transactionMoney(String statDate, String endDate) {
		StringBuffer sql = new StringBuffer();
		sql.append("select sum(total_price) price from t_order_basic where payment_status =1 ");
		if(!StringUtil.isEmpty(statDate)&&!StringUtil.isEmpty(endDate)) {
			sql.append("and DATE_FORMAT(order_time,'%Y-%m-%d') >= '"+statDate+"' and DATE_FORMAT(order_time,'%Y-%m-%d') <= '"+endDate+"'");
		}
		Record  record = Db.use("cmtgp_base").findFirst(sql.toString());
		return record;
	}

	public Map<String,Object> commodityStatistics(String statDate, String endDate) {
		StringBuffer sql = new StringBuffer();
		sql.append("select (select s_name from t_commodity_info where s_id = b.s_id) sName,sum(order_num) orderNum  from t_order_basic a,t_order_detail b where a.o_id = b.o_id ");
		if(!StringUtil.isEmpty(statDate)&&!StringUtil.isEmpty(endDate)) {
			sql.append(" and DATE_FORMAT(a.order_time,'%Y-%m-%d') >= '"+statDate+"' and DATE_FORMAT(a.order_time,'%Y-%m-%d') <= '"+endDate+"'");
		}
		sql.append(" GROUP BY b.s_id ORDER BY b.s_id ");
		List<Record>  record = Db.use("cmtgp_base").find(sql.toString());
		Map<String,Object> map = new HashMap<String, Object>();
		List listN = new ArrayList<String>();
		List listO = new ArrayList<String>();
		for(Record r:record) {
			listN.add(r.get("sName"));
			listO.add(r.get("orderNum"));
		}
		map.put("axxis", listN);
		map.put("values", listO);
		return map;
	}

	public Map<String,Object> trendStatistics(String statDate, String endDate) throws ParseException {
		statDate = DateUtil.getDay(DateUtil.addDay(new Date(),-17,0));
		endDate = DateUtil.getDay(new Date());
		StringBuffer sql = new StringBuffer();
		StringBuffer sql2 = new StringBuffer();
		StringBuffer sql3 = new StringBuffer();
		
		List<Record>  record = Db.use("cmtgp_base").find("select DATE_FORMAT(order_time ,'%Y-%m-%d') date,count(1) total from t_order_basic where 1=1  ");
		List<Record>  record2 = Db.use("cmtgp_base").find("select DATE_FORMAT(date_time ,'%Y-%m-%d') date,count(1) total from t_shopping_info where 1=1  ");
		if(!StringUtil.isEmpty(statDate)&&!StringUtil.isEmpty(endDate)) {
			sql.append(" and DATE_FORMAT(order_time ,'%Y-%m-%d') >='"+statDate+"' and DATE_FORMAT(order_time ,'%Y-%m-%d') <= '"+endDate+"'");
			sql2.append(" and DATE_FORMAT(date_time ,'%Y-%m-%d') >='"+statDate+"' and DATE_FORMAT(date_time ,'%Y-%m-%d') <= '"+endDate+"'");
		}
		sql.append(" GROUP BY  DATE_FORMAT(order_time ,'%Y-%m-%d') ORDER BY DATE_FORMAT(order_time ,'%Y-%m-%d')");
		sql2.append(" GROUP BY  DATE_FORMAT(date_time ,'%Y-%m-%d') ORDER BY DATE_FORMAT(date_time ,'%Y-%m-%d'),u_id");
		Map<String,Object> map = new HashMap<String, Object>();
		List listN = new ArrayList<String>();
		listN.add(DateUtil.getDay(DateUtil.addDay(new Date(),-6,0)));
		listN.add(DateUtil.getDay(DateUtil.addDay(new Date(),-5,0)));
		listN.add(DateUtil.getDay(DateUtil.addDay(new Date(),-4,0)));
		listN.add(DateUtil.getDay(DateUtil.addDay(new Date(),-3,0)));
		listN.add(DateUtil.getDay(DateUtil.addDay(new Date(),-2,0)));
		listN.add(DateUtil.getDay(DateUtil.addDay(new Date(),-1,0)));
		listN.add(DateUtil.getDay(new Date()));
		List listO = new ArrayList<String>();
		List listY = new ArrayList<String>();
		for(int i=0;i<listN.size();i++) {
			for(Record r:record) {
				if(r.get("date").equals(listN.get(i))) {
					listY.add(r.get("total"));
				}else {
					listY.add(0);
				}
			}
		}
		
		for(int i=0;i<listN.size();i++) {
			for(Record r:record2) {
				if(r.get("date").equals(listN.get(i))) {
					listO.add(r.get("total"));
				}else {
					listO.add(0);
				}
			}
		}
		map.put("date", listN);
		map.put("visitsTotal", listO);
		map.put("transactionTatal", listY);
		return map;
	}
}
