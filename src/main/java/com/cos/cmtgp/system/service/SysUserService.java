package com.cos.cmtgp.system.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cos.cmtgp.common.util.PasswordUtil;
import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class SysUserService {

	public Map<String, Object> findPage(int pageSize,int pageNumber,Map<String, String[]> paraMap, String orderBy,String orgId) {
		boolean b = true;
		StringBuffer buf = new StringBuffer("from t_user u,t_org_structure o where u.org_id = o.org_id ");
		for (String paraName : paraMap.keySet()) {

			String prefix = "queryParams[";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					//处理范围参数
					if(field.startsWith("user_name")) {
						buf.append(" and u.user_account like '%"+value+"%'");
					}else if(field.startsWith("org_id")) {
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
					if(field.startsWith("user_name")) {
						buf.append(" and u.user_account like '%"+value+"%'");
					}else if(field.startsWith("org_id")) {
						b = false;
						buf.append(" and u.org_id in("+getOrgs(value)+") ");
					}
				}
			}
		
		}
		if(b) {
			buf.append(" and u.org_id in("+getOrgs(orgId)+") ");
		}
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = " ORDER BY u.user_id asc";
		}else {
			buf.append(" ORDER BY "+orderBy);
		}
		Page<Record>  pages = Db.use("tcms_base").paginate(pageNumber, pageSize,"select u.user_account,u.user_password,o.org_name,u.create_time,u.user_id,CASE u.role_id  WHEN 0 THEN '管理人员'  WHEN 1 THEN '普通人员'  end role_id  ",buf.toString());
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
	
	public boolean add(Map<String, String[]> paraMap) {
		Record record = new Record();
		for (String paraName : paraMap.keySet()) {
			if(paraName.startsWith("model.user_account")) {
				record.set("user_account", paraMap.get(paraName)[0]);
				String rep = "select * from t_user where user_account ='"+paraMap.get(paraName)[0]+"'";
				if(Db.use("tcms_base").find(rep).size()>0) {
					return false;
				}
			}else if(paraName.startsWith("model.user_password")) {
				record.set("user_password", PasswordUtil.encodePassword(paraMap.get(paraName)[0]));
			}else if(paraName.startsWith("model.org_id")) {
				record.set("org_id", paraMap.get(paraName)[0]);
			}else {
				record.set("role_id", paraMap.get(paraName)[0]);
			}
		}
		record.set("create_time", new Date());
		return Db.use("tcms_base").save("t_user", record);
	}

	public int delete(String string) {
		return Db.use("tcms_base").delete("delete from t_user where user_account in("+string+")");
	}

	public List<Record> find(String user_account, String old_password) {
		return Db.use("tcms_base").find("select * from t_user where user_account = '"+user_account+"' and user_password = '"+old_password+"'");
	}


	public boolean update(String user_account, String encodePassword) {
		return Db.use("tcms_base").update("update t_user set user_password ='"+encodePassword+"' where user_account ='"+user_account+"'")>0;
	}
}
