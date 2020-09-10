package com.cos.cmtgp.system.service;


import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cos.cmtgp.common.util.DateUtil;
import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class CollectionService {

	public List<Record> findAll(String orgId) {
		return Db.use("tcms_base").find("select e.`name`,e.IDCard,e.device_code,o.org_id,o.org_name,CONCAT(o.org_level,'') org_level,e.sex from t_employee e,t_org_structure o where e.org_id = o.org_id and e.org_id in("+getOrgs(orgId)+")");
	}

	public Record findUser(String user_account, String user_password) {
		return Db.use("tcms_base").findFirst("select * from t_user where user_account = '"+user_account+"' and user_password ='"+user_password+"'");
	}

	public boolean updateEmpl(String iDCard, String device_code) {
		return Db.use("tcms_base").update("update t_employee set device_code ='"+device_code+"' where IDCard ='"+iDCard+"'")>0;
	}


	public Page<Record> selectCollects(String findDate, String orgId, int pageNum, int pageSize,String IDCard, String user_account,String name,String status) {
		StringBuffer buf = new StringBuffer("select z.org_name,z.`name`,z.IDCard,z.sex,(select count(1) from t_collection where IDCard = z.IDCard and c_status=0 ");
		StringBuffer temp = new StringBuffer();
		Page<Record> page=null;
		StringBuffer bf;
		try {
			if(!StringUtil.isEmpty(findDate)) {
				temp.append(" and c_addTime like '"+findDate+"%'");
			}else {
				temp.append(" and c_addTime like '"+DateUtil.getDay(new Date())+"%'");
			}
			
			if(!StringUtil.isEmpty(status)) {
				StringBuffer b = new StringBuffer("select GROUP_CONCAT('''',t.IDCard,'''') IDCards from (select IDCard from t_collection where c_status="+status);
				if(!StringUtil.isEmpty(findDate)) {
					b.append(" and c_addTime like '"+findDate+"%' ");
				}else {
					b.append(" and c_addTime like '"+DateUtil.getDay(new Date())+"%' ");
				}
				Record rec = Db.use("tcms_base").findFirst(b.toString()+" GROUP BY IDCard) t ");
				bf = new StringBuffer(" from (select a.`name`,a.IDCard,b.org_name,a.sex,b.org_id,b.org_pid from t_employee a,t_org_structure b where a.org_id = b.org_id and a.IDCard in("+rec.getStr("IDCards")+")) z where 1=1 ");
			}else {
				bf = new StringBuffer(" from (select a.`name`,a.IDCard,b.org_name,a.sex,b.org_id,b.org_pid from t_employee a,t_org_structure b where a.org_id = b.org_id) z where 1=1 ");
			}
			buf.append(temp);
			buf.append(") normalNum,(select count(1) from t_collection where IDCard = z.IDCard and c_status=1 ");
			buf.append(temp);
			buf.append(" ) abnormalNum  ");
			if(!StringUtil.isEmpty(orgId)) {
				bf.append(" and z.org_id in ("+getOrgs(orgId)+")");
			}else {
				Record  rec = Db.use("tcms_base").findFirst("select org_id t_user where user_account='"+user_account+"'");
				bf.append(" and z.org_id in ("+getOrgs(rec.get("org_id").toString())+")" );
			}
			if(!StringUtil.isEmpty(IDCard)) {
				bf.append(" and z.IDCard ='"+IDCard+"'");
			}
			if(!StringUtil.isEmpty(name)) {
				bf.append(" and z.name like '%"+name+"%' ");
			}
			bf.append(" ORDER BY z.org_id ");
			//buf.append(" GROUP BY c.IDCard ORDER BY c.IDCard limit "+(Integer.parseInt(pageNum)-1)*Integer.parseInt(pageSize)+","+Integer.parseInt(pageNum)*Integer.parseInt(pageSize));
			 page = Db.use("tcms_base").paginate(pageNum, pageSize,buf.toString(),bf.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<Record> list = page.getList();
		for(Record record :list){
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("select * from t_collection a LEFT JOIN (select b.IDCard,b.`name`,b.phone,c.org_name from t_employee b,t_org_structure c where b.org_id = c.org_id) t on a.IDCard = t.IDCard  ");
			stringBuffer.append("where 1 = 1").append(" and a.IDCard ='"+record.get("IDCard")+"'").append("  ORDER BY  a.c_addTime  DESC  ").append("LIMIT 1");
			Record tcms_base = Db.use("tcms_base").findFirst(stringBuffer.toString());
			if(tcms_base !=null ){
				record.set("createTime",tcms_base.get("c_addTime"));
				record.set("out",tcms_base.get("c_value"));
			}else {
				record.set("createTime","");
				record.set("out","");
			}
		}
		return page;//Db.use("tcms_base").find(buf.toString());
	}
	
	public String getOrgs(String orgId) {
		StringBuffer buf = new StringBuffer();
		Record rec = Db.use("tcms_base").findFirst("select getOrgChildLst("+orgId+") as orgList");
		if(null!=rec) {
			buf.append(rec.get("orgList"));
		}
		return buf.toString();
	}

	public Map<String, Object> findPage(int pageSize, int pageNum,String findDate, String orgId,String IDCard,String name,String status) {
		Page<Record> record = this.selectCollects(findDate, orgId, pageNum, pageSize,IDCard,null,name,status);
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", record.getList());
		datagrid.put("total", record.getTotalRow());
	return datagrid;
	}

	public Page<Record> selectCollectiondetail(String findDate, String iDCard, String pageNum, String pageSize) {
		StringBuffer buf = new StringBuffer();
		buf.append(" from t_collection a LEFT JOIN (select b.IDCard,b.`name`,b.phone,c.org_name from t_employee b,t_org_structure c where b.org_id = c.org_id) t on a.IDCard = t.IDCard where 1=1 ");
		if(!StringUtil.isEmpty(findDate)) {			
			buf.append(" and a.c_addTime like '"+findDate+"%'");
		}
		if(!StringUtil.isEmpty(iDCard)) {			
			buf.append(" and a.iDCard ='"+iDCard+"' ORDER BY a.c_addTime desc ");
		}
		return Db.use("tcms_base").paginate(Integer.parseInt(pageNum), Integer.parseInt(pageSize),"select t.`name`,t.IDCard,t.org_name,t.phone,a.c_addTime,a.c_value,a.c_status ",buf.toString());
	}

	public List<Record> getOrgList(String user_account) {
		Record r = Db.use("tcms_base").findFirst("select org_id from t_user where user_account ='"+user_account+"'");
		return Db.use("tcms_base").find("select org_id,org_name,org_pid,CONCAT(org_level,'') org_level from t_org_structure where org_id in("+getOrgs(r.get("org_id").toString())+")");
	}

	public Map<String, Object> finddetailPage(int pageSize, int pageNum, String findDate, String iDCard) {
		Page<Record> record = this.selectCollectiondetail(findDate, iDCard, String.valueOf(pageNum), String.valueOf(pageSize));
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", record.getList());
		datagrid.put("total", record.getTotalRow());
	return datagrid;
	}

	public Record getStatistics(String orgId, String findDate, String iDCard, String name) {
		Record r = new Record();
		//检测正常人数
		Record normalNum =Db.use("tcms_base").findFirst("select count(1) normalNum from (select a.IDCard from t_collection a,t_org_structure b,t_employee e where a.IDCard = e.IDCard and b.org_id = e.org_id and c_status = 0 and a.c_addTime like '"+findDate+"%' and b.org_id in("+getOrgs(orgId)+") GROUP BY IDCard) t ");
		//检测异常人数
		Record abnormalNum =Db.use("tcms_base").findFirst("select count(1) abnormalNum from (select a.IDCard from t_collection a,t_org_structure b,t_employee e where a.IDCard = e.IDCard and b.org_id = e.org_id and c_status = 1 and a.c_addTime like '"+findDate+"%' and b.org_id in("+getOrgs(orgId)+") GROUP BY IDCard) t ");
		//检测次数
		Record sNum =Db.use("tcms_base").findFirst("select count(1) sNum from t_collection a,t_org_structure b,t_employee e where a.IDCard = e.IDCard and b.org_id = e.org_id and a.c_addTime like '"+findDate+"%' and b.org_id in("+getOrgs(orgId)+")");
		//已检测人数
		Record checkNum =Db.use("tcms_base").findFirst("select count(1) checkNum from (select count(1) sNum from t_collection a,t_org_structure b,t_employee e where a.IDCard = e.IDCard and b.org_id = e.org_id and a.c_addTime like '"+findDate+"%' and b.org_id in("+getOrgs(orgId)+") GROUP BY a.IDCard) t");
		//总人数
		Record restNum =Db.use("tcms_base").findFirst("select count(1) restNum from t_employee where org_id in("+getOrgs(orgId)+")  ");
		Integer normal = normalNum.getInt("normalNum")==null?0:normalNum.getInt("normalNum");
		Integer abnormal = abnormalNum.getInt("abnormalNum")==null?0:abnormalNum.getInt("abnormalNum");
		Integer rest = restNum.getInt("restNum")==null?0:restNum.getInt("restNum");
		Integer sn = sNum.getInt("sNum")==null?0:sNum.getInt("sNum");
		Integer check = checkNum.getInt("checkNum")==null?0:checkNum.getInt("checkNum");
		r.set("normalNum", normal);
		r.set("abnormalNum", abnormal);
		r.set("TotalNum", rest);
		r.set("CheckedNum", sn);
		r.set("CheckedsNum", check);
		r.set("UndetectedNum", rest-check);
		return r;
	}

}
