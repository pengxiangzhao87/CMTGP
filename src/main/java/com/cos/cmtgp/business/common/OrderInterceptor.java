package com.cos.cmtgp.business.common;

import java.util.Map;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

public class OrderInterceptor implements Interceptor {

		public void intercept(Invocation inv) {
			String ActionKey = inv.getActionKey();
			if(ActionKey.contains("order")) {
				Map<String, String[]> map = inv.getController().getRequest().getParameterMap();
				System.out.println("订单拦截器："+map.toString());
			}
			inv.invoke();
		}

		
}
