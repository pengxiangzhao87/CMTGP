package com.cos.cmtgp.business.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cos.cmtgp.business.model.CommodityTypeSetting;
import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;


public class InterfaceProxyService {


	public Map<String, Object> findPage(int pageSize, int pageNum, Map<String, String[]> paraMap, String orderBy) {
		StringBuffer buf = new StringBuffer("from t_interface_info u where 1=1 ");
		for (String paraName : paraMap.keySet()) {
			String prefix = "queryParams[";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					//处理范围参数
					if(field.startsWith("interface_name")) {
						buf.append(" and u.interface_name like '%"+value+"%'");
					}else if(field.startsWith("interface_desc")) {
						buf.append(" and u.interface_desc like '%"+value+"%'");
					}
				}
			}
		}
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = " ORDER BY u.id asc";
		}else {
			buf.append(" ORDER BY "+orderBy);
		}
		Page<Record>  pages = Db.use("cmtgp_base").paginate(pageNum, pageSize,"select * ",buf.toString());
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", pages.getList());
		datagrid.put("total", pages.getTotalRow());
	return datagrid;
	}
}
