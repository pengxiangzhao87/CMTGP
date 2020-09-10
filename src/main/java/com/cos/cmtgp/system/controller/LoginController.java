package com.cos.cmtgp.system.controller;


import java.util.ArrayList;
import java.util.List;

import com.cos.cmtgp.common.CmtgpConst;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.PasswordUtil;
import com.cos.cmtgp.common.util.StringUtil;
import com.cos.cmtgp.common.vo.SysMenu;
import com.cos.cmtgp.common.vo.User;
import com.cos.cmtgp.system.service.LoginService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;


/**
 * 登陆
 */
public class LoginController extends BaseController {
	LoginService loginService = enhance(LoginService.class);
	
	/**
	 * 登陆页面
	 */
	public void index() {
		render("login.html");
	}
	
	/**
	 * 登陆
	 */
	public void login() {
		String userName = getPara("user_name");
		String password = getPara("password");
		String remark = "登陆成功";
		
		if(StringUtil.isEmpty(getPara("randomCode")) || !validateCaptcha("randomCode")) {
			remark = "验证码错误！";
			renderFailed(remark,null);
			return;
		}
		//String password = PasswordUtil.encodePassword(getPara("password"));
		if(!StringUtil.isEmpty(userName)){			
			Record  record  = Db.use("cmtgp_base").findFirst("select `u_account`, `u_pwd`, `u_phone` from t_user_setting  where u_account = ? ",new Object[] {userName});
			if(null != record) {
				User user = new User(userName, record.get("u_pwd").toString(),CmtgpConst.supplierList.get(userName));
				if(record.get("u_pwd").equals(PasswordUtil.encodePassword(password))) {
					setSessionAttr("sysUser", user);

					renderSuccess();
				}else {
					remark = "账号密码输入错误！";
					renderFailed(remark,null);
					return;
				}
				
			}else {
				remark = "账号不存在！";
				renderFailed(remark,null);
				return;
			}
		}else {
			remark = "输入账号无效！";
			renderFailed(remark,null);
			return;
		}
	}
	
	/**
	 * 获得用户菜单
	 */
	public void getMenu() {
		List<SysMenu> syslist = new ArrayList<SysMenu>();
		String supplierId = ((User) getSession().getAttribute("sysUser")).getSupplierId();
		if(null!=supplierId) {
			syslist.add(new SysMenu(3,"数据管理",null,0,"glyphicon-cog",0,"2016-01-06 19:37:31"));
			syslist.add(new SysMenu(4,"商品维护","/commodity/listPage",3,"glyphicon-cloud",1,"2016-01-07 21:41:21"));
			syslist.add(new SysMenu(5,"商品类型维护","/commodity/listTypePage",3,"glyphicon-cloud",1,"2016-01-07 21:41:21"));
			syslist.add(new SysMenu(6,"用户查询","/user/listPage",3,"glyphicon-book",6,"2016-02-29 11:44:07"));		
			syslist.add(new SysMenu(7,"订单查询","/order/listPage",3,"glyphicon-search",2,"2016-09-12 22:10:51"));
		}else {
			syslist.add(new SysMenu(3,"数据管理",null,0,"glyphicon-cog",0,"2016-01-06 19:37:31"));
			syslist.add(new SysMenu(4,"商品维护","/commodity/listPage",3,"glyphicon-cloud",1,"2016-01-07 21:41:21"));
			syslist.add(new SysMenu(5,"商品类型维护","/commodity/listTypePage",3,"glyphicon-cloud",1,"2016-01-07 21:41:21"));
			syslist.add(new SysMenu(6,"用户查询","/user/listPage",3,"glyphicon-book",6,"2016-02-29 11:44:07"));		
			syslist.add(new SysMenu(7,"订单查询","/order/listPage",3,"glyphicon-search",2,"2016-09-12 22:10:51"));
			syslist.add(new SysMenu(8,"接口配置","/interfaceProxy/listPage",0,"glyphicon-king",2,"2016-09-12 22:10:51"));
			syslist.add(new SysMenu(9,"数据统计","/realtime/listPage",0,"glyphicon-search",2,"2016-09-12 22:10:51"));
		}
		renderJson(syslist);
	}
	
	/**
	 * 图形验证码
	 */
	public void randomCode() {
		renderCaptcha();
	}
	
	/**
	 * 退出登陆
	 */
	public void logout() {
		removeSessionAttr("sysUser");
		redirect("/login");
	}
	
	//修改密码页面
	public void updatePasswordPage() {
		render("updatePassword.html");
	}
	
	//修改密码
	public void updatePassword() {
		String userId = ((User)getSession().getAttribute("sysUser")).getName();
		String password = getPara("password");
		password = PasswordUtil.encodePassword(password);
		int row = Db.use("cmtgp_base").update("update t_userinfo set password = ? where userId = ? ",new Object[] {password,userId});
		if(row>0) {			
			renderSuccess("密码修改成功");
		}else {
			renderFailed("密码修改异常");
		}
	}
}
