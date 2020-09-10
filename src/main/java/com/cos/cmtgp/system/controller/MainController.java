package com.cos.cmtgp.system.controller;



import com.cos.cmtgp.common.vo.User;
import com.jfinal.core.Controller;

public class MainController extends Controller {
	
	public void index() {
		setAttr("userName", ((User)getSession().getAttribute("sysUser")).getName());
		render("main.html");
	}

	public void codePrint() {
		setAttr("userName", ((User)getSession().getAttribute("sysUser")).getName());
		render("print.html");
	}
	
	public void btns() {
	}
	
	public void rules() {
	}
	
	public void menus() {
		//renderJson(authService.getAuthMenuForTree(SysUser.dao.findById(getParaToInt("id"))));
	}
}
