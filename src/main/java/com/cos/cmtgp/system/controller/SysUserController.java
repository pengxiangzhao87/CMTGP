package com.cos.cmtgp.system.controller;



import java.util.List;
import java.util.Map;

import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.PasswordUtil;
import com.cos.cmtgp.common.util.StringUtil;
import com.cos.cmtgp.common.vo.User;
import com.cos.cmtgp.system.service.SysUserService;
import com.jfinal.plugin.activerecord.Record;

/**
 * 用户管理
 */
public class SysUserController extends BaseController {
	SysUserService sysUservice = enhance(SysUserService.class);
	//跳转修改密码页面
	public void updatePasswordPage() {
		setAttr("model", getSessionAttr("sysUser"));
		render("updatePassword.html");
	}
	
	//修改密码
	public void updatePassword() {
		String user_account = getPara("model.user_name");
		String old_password = getPara("model.old_password");
		String password = getPara("model.password");
		List<Record> record = sysUservice.find(user_account,PasswordUtil.encodePassword(old_password));
		if(record.size()==0) {
			renderFailed("密码输入错误！");
			return;
		}else {
			if(sysUservice.update(user_account,PasswordUtil.encodePassword(password))) {
				renderSuccess();
			}else {
				renderFailed("密码修改失败");
			}
		}
	}
	
	//密码重置
	public void resetPassword() {
		renderSuccess();
	}
	
	/**
	 * 系统用户查询页面
	 */
	public void listPage() {
		//setAttr("dictDatatarget_type", TaskBase.me.getDictDatatarget_type());
		//setAttr("dictDatastatus", TaskBase.me.getDictDatastatus());
		render("list.html");
	}
	
	/**
	 * 系统用户查询数据列表
	 */
	public void listData() {
		Map<String, String[]> paraMap = getParaMap();
		String orderBy = getOrderBy();
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = "u.user_id asc";
		}
		int pageNum = getPager().getPage();
		int pageSize = getPager().getRows();
		User user = getSessionAttr("sysUser"); 
		renderJson(sysUservice.findPage(pageSize,pageNum,paraMap,orderBy,"".toString()));
	}
	
	/**
	 * 系统用户查询页面
	 */
	public void addPage() {
		//setAttr("dictDatatarget_type", TaskBase.me.getDictDatatarget_type());
		//setAttr("dictDatastatus", TaskBase.me.getDictDatastatus());
		setAttr("orgName", getPara("orgName"));
		setAttr("orgId", getPara("orgId"));
		render("add.html");
	}
	
	/**
	 * 系统用户查询数据列表
	 */
	public void add() {
		Map<String, String[]> paraMap = getParaMap();
		if(sysUservice.add(paraMap)) {
			renderSuccess();
		}else {
			renderFailed("账号已存在");
		}
	}
	
	/**
	 * 系统用户删除
	 */
	public void delete() {
		String[] ids = getParaValues("id[]");
		StringBuffer buf = new StringBuffer();
		int i =0;
		for (String id : ids) {
			if(i==1) {
				buf.append(",");
			}
			buf.append("'");
			buf.append(id);
			buf.append("'");
			i++;
		}
		if(sysUservice.delete(buf.toString())>0) {
			renderSuccess();
		}else {
			renderFailed("服务异常");
		}
	}
}
