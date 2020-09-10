package com.cos.cmtgp.business.controller;


import java.util.List;
import java.util.Map;

import com.cos.cmtgp.business.model.CommodityTypeSetting;
import com.cos.cmtgp.business.model.InterfaceInfo;
import com.cos.cmtgp.business.service.CommodityService;
import com.cos.cmtgp.business.service.InterfaceProxyService;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 *	  接口代理类
 */
public class InterfaceProxy extends BaseController {
	InterfaceProxyService interfaceProxyService = enhance(InterfaceProxyService.class);
	/**
	 * 代理接口实现类
	 */
	public void Implementation() {
		try {
			String InterfaceName = getPara("InterfaceName");
			List<Record> record = null;
			InterfaceInfo interfaceInfo = getModel(InterfaceInfo.class, "model").findFirst("select `id`, `interface_name`, `interface_desc`, `execute_sql`, `return_field`, `interface_parameter` from t_interface_info where Interface_name = '"+InterfaceName+"'");
			if(null!=interfaceInfo) {			
				String execSql = interfaceInfo.getExecuteSql();
				String patams = interfaceInfo.get("interface_parameter");
				if(!StringUtil.isEmpty(patams)) {
					String[] fields = patams.split("~");
					Object [] para = new Object[fields.length];
					for(int i =0;i<fields.length;i++) {
						para[i] = getPara(fields[i]).replace("-", "%");
					}
					record = Db.use("cmtgp_base").find(execSql, para);
				}else {
					record = Db.use("cmtgp_base").find(execSql);
				}
			}
			renderSuccess("请求成功",record);
		} catch (Exception e) {
			renderFailed("服务请求异常");
			e.printStackTrace();
		}
	}	
	
	/**
	 * 接口配置添加页面
	 */
	public void addPage() {
		render("InterfaceInfo_add.html");
	}
	
	/**
	 * 接口配置添加
	 */
	public void add() {
		InterfaceInfo interfaceInfo = getModel(InterfaceInfo.class, "model");
		interfaceInfo.set("interface_name", getPara("interface_name"));
		interfaceInfo.set("interface_desc", getPara("interface_desc"));
		interfaceInfo.set("execute_sql", getPara("execute_sql"));
		interfaceInfo.set("return_field", getPara("return_field"));
		interfaceInfo.set("interface_parameter", getPara("interface_parameter"));
		if(interfaceInfo.save()) {
			renderSuccess("接口配置添加成功");
		}else {
			renderFailed("接口配置添加失败");
		}
	}
	
	/**
	 * 接口配置修改页面
	 */
	public void updatePage() {
		setAttr("model", InterfaceInfo.dao.findById(getPara("id")));
		render("InterfaceInfo_update.html");
	}
	
	/**
	 * 接口配置修改
	 */
	public void update() {
		InterfaceInfo interfaceInfo = getModel(InterfaceInfo.class, "model");
		interfaceInfo.set("id", getPara("id"));
		interfaceInfo.set("interface_name", getPara("interface_name"));
		interfaceInfo.set("interface_desc", getPara("interface_desc"));
		interfaceInfo.set("execute_sql", getPara("execute_sql"));
		interfaceInfo.set("return_field", getPara("return_field"));
		interfaceInfo.set("interface_parameter", getPara("interface_parameter"));
		if(interfaceInfo.update()) {
			renderSuccess();
		}else {
			renderFailed();
		}
	}	
	
	/**
	 * 接口配置删除
	 */
	public void delete() {
		try {
			Integer[] ids = getParaValuesToInt("id[]");
			for (Integer id : ids) {
				new InterfaceInfo().set("id", id).delete();
			}
			renderSuccess();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * 接口配置查询页面
	 */
	public void listPage() {
		render("InterfaceInfo_list.html");
	}
	
	/**
	 * 接口配置查询数据
	 */
	public void listData() {
		Map<String, String[]> paraMap = getParaMap();
		String orderBy = getOrderBy();
		int pageNum = getPager().getPage();
		int pageSize = getPager().getRows();
		renderJson(interfaceProxyService.findPage(pageSize,pageNum,paraMap,orderBy));
	}
	
	
}
