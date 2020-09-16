package com.cos.cmtgp.common;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cos.cmtgp.common.vo.SysMenu;

public class CmtgpConst {
	
	/** 后台session user key **/
	public final static String ADMIN_SESSIOIN_USER_KEY = "sysUser";
	
	/** 搜索类型 or2In (多选搜索，用or代替in，防止SQL注入) **/
	public final static String SEARCH_TYPE_OR2IN = "or2In";
	
	/** 默认删除标识 **/
	public final static String DEF_SYS_DELETE_FLAG = "sys_delete_flag";
	
	/** 默认菜单 **/
	public final static List<SysMenu> syslist;
	
	/** 供应商账号编号对应关联 **/
	public final static Map<String,String> supplierList;

	static {
		syslist = new ArrayList<SysMenu>();
	/*	syslist.add(new SysMenu(1,"系统管理",null,0,"glyphicon-user",1,"2016-01-07 03:32:08"));
		syslist.add(new SysMenu(2,"系统用户","/sysUser/listPage",1,"glyphicon-king",2,"2016-02-16 03:59:22"));*/
		syslist.add(new SysMenu(3,"数据管理",null,0,"glyphicon-cog",0,"2016-01-06 19:37:31"));
		syslist.add(new SysMenu(4,"商品维护","/commodity/listPage",3,"glyphicon-cloud",1,"2016-01-07 21:41:21"));
		syslist.add(new SysMenu(5,"商品类型维护","/commodity/listTypePage",3,"glyphicon-cloud",1,"2016-01-07 21:41:21"));
		syslist.add(new SysMenu(6,"用户查询","/user/listPage",3,"glyphicon-book",6,"2016-02-29 11:44:07"));		
		syslist.add(new SysMenu(7,"订单查询","/order/listPage",3,"glyphicon-search",2,"2016-09-12 22:10:51"));
		syslist.add(new SysMenu(8,"接口配置","/interfaceProxy/listPage",0,"glyphicon-king",2,"2016-09-12 22:10:51"));
		syslist.add(new SysMenu(9,"数据统计","/realtime/listPage",0,"glyphicon-search",2,"2016-09-12 22:10:51"));

		supplierList = new HashMap<String, String>();
		supplierList.put("tyxg", "1");
		supplierList.put("tyxg01", "1");
		supplierList.put("tyxg02", "1");
		supplierList.put("sjjksp", "2");
		supplierList.put("sjjksp01", "2");
		supplierList.put("sjjksp02", "2");
		supplierList.put("sjjksp03", "2");
	}

}
