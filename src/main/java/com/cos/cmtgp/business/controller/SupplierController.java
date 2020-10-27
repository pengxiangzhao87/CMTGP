package com.cos.cmtgp.business.controller;


import com.alibaba.fastjson.JSON;
import com.cos.cmtgp.business.model.CommodityInfo;
import com.cos.cmtgp.business.model.GlobalConf;
import com.cos.cmtgp.business.model.SupplierSetting;
import com.cos.cmtgp.business.service.SupplierService;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.FreemarkUtil;
import com.cos.cmtgp.common.util.PasswordUtil;
import com.cos.cmtgp.common.util.UrlUtil;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *	  供应商接口
 */
public class SupplierController extends BaseController {
	SupplierService supplierService = enhance(SupplierService.class);

	public void getSupplier(){
		Integer tId = getParaToInt("tId");
		SupplierSetting byId = SupplierSetting.dao.findById(tId);
		renderSuccess("",byId);
	}

	public void uploadSupplier(){
		String json = HttpKit.readData(getRequest());
		SupplierSetting supplier = FastJson.getJson().parse(json,SupplierSetting.class);
		supplier.update();
		renderSuccess();
	}

	/**
	 * 商户登录
	 */
	public void login(){
		String account = getPara("account");
		String password = getPara("password");
		String token = getPara("token");
		String openid = CacheKit.get("miniProgram", token);
		List<Record> recordList = Db.find("select * from t_supplier_setting where s_account='" + account + "' and s_password='" + PasswordUtil.encodePassword(password)+"'");
		if(recordList.size()>0){
			Db.update("update t_supplier_setting set s_openid='"+openid+"' where s_id="+recordList.get(0).get("s_id"));
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("sId",recordList.get(0).getInt("s_id"));
			if(recordList.get(0).getStr("s_account").equals("tyxg")){
				map.put("isHidden",0);
			}else{
				map.put("isHidden",1);
			}
			renderSuccess("",map);
		}else{
			renderFailed();
		}
	}

	/**
	 * 商户获取openid
	 */
	public void getOpendId(){
		String code = getPara("code");
		Integer type = getParaToInt("type");
		List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type="+type);
		String cAppid = confList.get(0).getCAppid();
		String cSecret = confList.get(0).getCSecret();
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+cAppid+"&secret="+cSecret+"&js_code="+code+"&grant_type=authorization_code";
		String result = UrlUtil.getAsText(url);
		Map<String,Object> parse = (Map<String,Object>)JSON.parse(result);
		String openid = (String)parse.get("openid");
		if(!"".equals(result)){
			List<Record> recordList = Db.find("select * from t_supplier_setting where s_openid='"+openid+"'");
			String rdSeesion = FreemarkUtil.get3rdSeesion(128);
			CacheKit.put("miniProgram",rdSeesion,openid);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("token",rdSeesion);
			if(recordList.size()>0){
				map.put("sId",recordList.get(0).get("s_id"));
				if(recordList.get(0).getStr("s_account").equals("tyxg")){
					map.put("isHidden",0);
				}else{
					map.put("isHidden",1);
				}
				renderSuccess("",map);
			}else{
				renderSuccess("",map);
			}
		}else{
			renderFailed();
		}
	}


	/**
	 * 添加用户信息
	 */
	public void addUser() {
		String json = HttpKit.readData(getRequest());
		SupplierSetting supplier = FastJson.getJson().parse(json, SupplierSetting.class);
		if(supplier.save()) {
			addOpLog("[供应商管理] 添加");
			renderSuccess("供应商添加成功");
		}else {
			renderFailed("供应商加失败");
		}
	}

	/**
	 * 修改用户信息
	 */
	public void updateUser() {
		if(getModel(SupplierSetting.class, "model").update()) {
			addOpLog("[供应商管理] 修改");
			renderSuccess("供应商修改成功");
		}else {
			renderFailed("用户修改失败");
		}
	}

}
