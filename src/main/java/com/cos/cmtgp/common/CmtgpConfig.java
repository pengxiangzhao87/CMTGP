package com.cos.cmtgp.common;


import com.cos.cmtgp.business.common.OrderInterceptor;
import com.cos.cmtgp.business.controller.*;
import com.cos.cmtgp.business.model._MappingKit;
import com.cos.cmtgp.system.controller.LoginController;
import com.cos.cmtgp.system.controller.MainController;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.Const;
import com.jfinal.core.JFinal;
import com.jfinal.json.FastJsonFactory;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.FreeMarkerRender;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;

import freemarker.template.TemplateModelException;

/**
 * API引导式配置
 */
public class CmtgpConfig extends JFinalConfig {
	private static Constants jfinalConstants;
	
	/**
	 * 配置常量
	 */
	@Override
	public void configConstant(Constants me) {
		// 加载少量必要配置，随后可用PropKit.get(...)获取值
		PropKit.use("db_config.txt");
		me.setDevMode(true);
		me.setViewType(ViewType.FREE_MARKER);
		me.setMaxPostSize(100*Const.DEFAULT_MAX_POST_SIZE);
		me.setJsonFactory(new FastJsonFactory());
	}
	
	/**
	 * 配置路由
	 */
	@Override
	public void configRoute(Routes me) {
        me.add("/commodity", CommodityController.class,"/cmtgp/commodity");
		me.add("/community", CommunityController.class,"/cmtgp/community");
		me.add("/menu", MenuController.class,"/cmtgp/menu");
		me.add("/order", OrderController.class,"/cmtgp/order");
		me.add("/shoppingCart", ShoppingCartController.class,"/cmtgp/shoppingcart");
		me.add("/supplier", SupplierController.class,"/cmtgp/supplier");
		me.add("/realtime", RealtimeController.class,"/cmtgp/realtime");
		me.add("/user", UserController.class,"/cmtgp/user");
		me.add("/login", LoginController.class,"/cmtgp/login");
		me.add("/main", MainController.class,"/cmtgp");
		me.add("/interfaceProxy", InterfaceProxy.class,"/cmtgp/interfaceProxy");
		me.add("/mini", MiniController.class,"/cmtgp/mini");
	}
	
	/**
	 * 配置插件
	 */
	@Override
	public void configPlugin(Plugins me) {
		// 配置C3p0数据库连接池插件
		C3p0Plugin c3p0Plugin = new C3p0Plugin(PropKit.get("base_jdbcUrl"), PropKit.get("base_user"), PropKit.get("base_password").trim(),"com.mysql.cj.jdbc.Driver");
		me.add(c3p0Plugin);
		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin("cmtgp_base", c3p0Plugin);
		arp.setShowSql(true);
		_MappingKit.mapping(arp);
		me.add(arp);
		//配置缓存插件
		me.add(new EhCachePlugin());
		//Cron4jPlugin cron4jPlugin = new Cron4jPlugin(PropKit.use("task.properties"));
		//me.add(cron4jPlugin);
	}
	
	/**
	 * 配置全局拦截器
	 */
	@Override
	public void configInterceptor(Interceptors me) {
		me.add(new OrderInterceptor());
	}
	
	/**
	 * 配置处理器
	 */
	@Override
	public void configHandler(Handlers me) {

	}
	
	@Override
	public void afterJFinalStart() {
		try {
			FreeMarkerRender.getConfiguration().setSharedVariable("basePath", JFinal.me().getContextPath());
		} catch (TemplateModelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 建议使用 JFinal 手册推荐的方式启动项目
	 * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		JFinal.start("src/main/webapp", 9000, "/CMTGP");
	}

	@Override
	public void configEngine(Engine me) {
	}
	
	public static boolean getDevModel() {
		return jfinalConstants.getDevMode();
	}
}
