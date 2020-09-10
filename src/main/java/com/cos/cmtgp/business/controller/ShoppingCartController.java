package com.cos.cmtgp.business.controller;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.cos.cmtgp.business.model.ShoppingInfo;
import com.cos.cmtgp.business.service.ShoppingCartService;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.system.service.LoginService;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 *	  购物车接口
 */
public class ShoppingCartController extends BaseController {
	LoginService loginService = enhance(LoginService.class);
	ShoppingCartService shoppingCartService = enhance(ShoppingCartService.class);

	/**
	 * APP
	 * 添加到购物车
	 */
	public void addShoppingCart(){
		String json = "";
		try{
			json = HttpKit.readData(getRequest());
			ShoppingInfo shoppingInfo =  FastJson.getJson().parse(json, ShoppingInfo.class);
			shoppingInfo.setDateTime(new Date());
			shoppingInfo.setIsCheck(1);
			shoppingCartService.addShopingCart(shoppingInfo);
			renderSuccess();
		}catch(Exception ex){
			addOpLog("addShoppingCart ====> "+json);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 查询购物车
	 * @return
	 */
	public void queryShoppingCartList(){
		Integer userId = getParaToInt("userId");
		try{
			renderSuccess("",shoppingCartService.queryShoppingCart(userId));
		}catch (Exception ex){
			addOpLog("queryShoppingCartList ====> userId="+userId);
			ex.printStackTrace();
			renderFailed();
		}

	}

	/**
	 * APP
	 * 购物车微调
	 */
	public void fineTuning(){
		Integer id = getParaToInt("id");
		Integer number = getParaToInt("number");
		Integer sId = getParaToInt("sId");
		try{
			shoppingCartService.fineTuning( id, number, sId);
			renderSuccess();
		}catch(Exception ex){
			addOpLog("fineTuning ====> id="+id+"，number="+number+",sId="+sId);
			ex.printStackTrace();
			renderFailed();
		}

	}


	/**
	 * APP
	 * 勾选商品
	 */
	public void checkCommodity(){
		String id = getPara("id");
		Integer isCheck = getParaToInt("isCheck");
		try{
			shoppingCartService.checkCommodity(id,isCheck);
			renderSuccess();
		}catch (Exception ex){
			addOpLog("checkCommodity ====> id="+id+"，isCheck="+isCheck);
			ex.printStackTrace();
			renderFailed();
		}

	}

	/**
	 * APP
	 * 删除购物车
	 */
	public void delteShoppingCart(){
		String ids = getPara("ids");
		try{
			shoppingCartService.deleteShoppingCart(ids);
			renderSuccess();
		}catch (Exception ex){
			addOpLog("delteShoppingCart ====> ids="+ids);
			ex.printStackTrace();
			renderFailed();
		}

	}
	
	public void getAlarmValue() {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String user_account = getPara("user_account");
    		Record  record = Db.use("tcms_base").findFirst("select org_id from t_user where user_account = '"+user_account+"'");
    	
    		String tmepId = "";
    		if(null!=record) {
    			String orgId = record.getStr("org_id");
            	tmepId = orgId;
            	if(Integer.parseInt(orgId)!=1) {
            		for(int i=0;i<5;i++) {
                		Record  r = Db.use("tcms_base").findFirst("select org_pid from t_org_structure where org_id="+tmepId);
                		if(Integer.parseInt(r.get("org_pid").toString())==1) {
                    		orgId = tmepId;
                    		break;
                    	}else {
                    		tmepId = r.get("org_pid").toString();
                    	}
                	}
            	}
    		}else {
    			map.put("msg", "数据请求失败");
				map.put("result", "failed");
				map.put("data", null);
				renderJson(map); 
				return;
    		}
    		Record  rc = Db.use("tcms_base").findFirst("select * from t_alarm_value where org_id="+tmepId);
    		if(null!=rc) {
				 map.put("msg", "成功请求");
				 map.put("result", "success");
				 map.put("data", rc);
			}else {
				Record r = new Record();
				r.set("upper_limit", 37);
				r.set("lower_limit", 35);
				map.put("msg", "系统默认参数");
				map.put("result", "success");
				map.put("data", r);
			}
		} catch (Exception e) {
			map.put("msg", "服务异常");
			map.put("result", "failed");
			e.printStackTrace();
		}
		renderJson(map); 
	}
	
}
