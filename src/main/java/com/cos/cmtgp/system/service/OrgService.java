package com.cos.cmtgp.system.service;

import java.util.HashMap;
import java.util.Map;

import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class OrgService {
	
	public Map<String, Object> findPage(int pageSize, int pageNum, Map<String, String[]> paraMap, String orderBy, String orgId) {
		boolean b = true;
		StringBuffer buf = new StringBuffer("from t_org_structure u where 1=1 ");
		for (String paraName : paraMap.keySet()) {
			String prefix = "queryParams[";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				//处理范围参数
				if(!StringUtil.isEmpty(value)) {
					if(field.startsWith("org_name")) {
						buf.append(" and u.org_name like '%"+value+"%'");
					}else if(field.startsWith("org_pid")){
						buf.append(" and u.org_id in("+getOrgs(value)+")");
						b = false;
					}
				}
			}
			String prefixc = "queryParams[0][";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefixc.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					//处理范围参数
					if(field.startsWith("org_name")) {
						buf.append(" and u.org_name like '%"+value+"%'");
					}else if(field.startsWith("org_pid")){
						buf.append(" and u.org_id in("+getOrgs(value)+")");
						b = false;
					}
				}
			}
		}
		if(b) {
			buf.append(" and u.org_id in("+getOrgs(orgId)+") ");
		}
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = " ORDER BY u.org_id asc";
		}else {
			buf.append(" ORDER BY "+orderBy);
		}
		Page<Record>  pages = Db.use("tcms_base").paginate(pageNum, pageSize,"select `org_id`, `org_name`, `org_pid`, CONCAT(org_level,'') org_level ",buf.toString());
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", pages.getList());
		datagrid.put("total", pages.getTotalRow());
	return datagrid;
	}
	
	public String getOrgs(String orgId) {
		StringBuffer buf = new StringBuffer();
		Record rec = Db.use("tcms_base").findFirst("select getOrgChildLst("+orgId+") as orgList");
		if(null!=rec) {
			buf.append(rec.get("orgList"));
		}
		return buf.toString();
	}

	public boolean save(String orgPid, String orgName) {
		Record  record = Db.use("tcms_base").findFirst("select CONCAT(org_level,'') org_level from t_org_structure where org_id="+orgPid);
		if(null!=record) {
			record.set("org_pid", orgPid);
			record.set("org_name", orgName);
			record.set("org_level", Integer.parseInt(record.get("org_level").toString())+1);
			record.set("dr", 0);
			Db.use("tcms_base").save("t_org_structure", record);
			return true;
		}
		return false;
	}
}
