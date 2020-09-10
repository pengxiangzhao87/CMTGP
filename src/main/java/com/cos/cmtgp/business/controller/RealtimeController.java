package com.cos.cmtgp.business.controller;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cos.cmtgp.business.service.CommunityService;
import com.cos.cmtgp.business.service.RealtimeService;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.system.service.LoginService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 *	  实时数据接口
 */
public class RealtimeController extends BaseController {
	RealtimeService realtimeService = enhance(RealtimeService.class);


	/**
	 * 实时数据统计
	 */
	public void listPage() {
		render("list.html");
	}
	
	/**
	 * 热用户数据查询
	 */
	public void userStatistics() {
		String statDate = getPara("statDate");
		String endDate = getPara("endDate");
		renderJson(realtimeService.userStatistics(statDate,endDate));
	}

	private List<Map<String,Object>> recordMap(Record r) {
		List list = new ArrayList<Map<String,Object>>();
		String [] key = r.getColumnNames();
		Map<String,Object> map = r.getColumns();
		for(int i =0;i<map.size();i++) {
			Map m = new HashMap<String,Object>();
			m.put("value",map.get(key[i]));
			m.put("name",key[i]);
			list.add(m);
		}
		return list;
	}

	/**
	 * 成交量数据查询
	 */
	public void transactionStatistics() {
		String statDate = getPara("statDate");
		String endDate = getPara("endDate");
		addOpLog("数据统计-交易量统计");
		renderJson(realtimeService.transactionStatistics(statDate,endDate));
	}
	
	/**
	 * 成交金额查询
	 */
	public void transactionMoney() {
		String statDate = getPara("statDate");
		String endDate = getPara("endDate");
		renderJson(realtimeService.transactionMoney(statDate,endDate));
	}
	
	
	/**
	 * 趋势数据统计
	 */
	public void trendStatistics() {
		String statDate = getPara("statDate");
		String endDate = getPara("endDate");
		try {
			renderJson(realtimeService.trendStatistics(statDate,endDate));
		} catch (ParseException e) {
			e.printStackTrace();
			renderFailed("数据请求异常");
		}
	}
	
	/**
	 * 商品分布查询
	 */
	public void commodityStatistics() {
		String statDate = getPara("statDate");
		String endDate = getPara("endDate");
		renderJson(realtimeService.commodityStatistics(statDate,endDate));
	}
}
