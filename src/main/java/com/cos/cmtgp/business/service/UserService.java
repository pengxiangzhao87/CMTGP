package com.cos.cmtgp.business.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cos.cmtgp.business.model.AddressInfo;
import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class UserService {

	/**
	 * APP
	 * 我的
	 * @param userId
	 * @return
	 */
	public Record getMyInfo(Integer userId){
			String sql = " select c.u_id,c.u_account,c.u_nick_name,c.u_phone,c.u_avatar_url,c.account_price,c.u_plot,c.u_concern,c.u_fans,c.u_liked,u_content ,REPLACE(group_concat(c.img),',','~') as imgUrl from (" +
				" select a.u_id,a.u_account,a.u_nick_name,a.u_phone,a.u_avatar_url,a.account_price,a.u_plot,a.u_concern,a.u_fans,a.u_liked,u_content " +
				" ,SUBSTRING_INDEX(b.h_address_img,'~',1) as img" +
				" from t_user_setting a left join t_hotissue_basic b on a.u_id=b.u_id and b.state=0 where a.u_id="+userId +
				" order by b.h_datetime desc limit 0,6 ) c";
		List<Record> records = Db.find(sql);
		return records.get(0);
	}

	/**
	 * APP
	 * 查询收货地址
	 * @param userId
	 * @param isUsed
	 * @return
	 */
	public List<AddressInfo> selectAddressList(Integer userId,Integer isUsed){
		String sql = "select * from t_address_info where u_id="+userId;
		if(isUsed!=null){
			sql += " and is_used="+isUsed;
		}
		return AddressInfo.dao.find(sql);
	}

	public Map<String, Object> getUserAll(int pageSize, int pageNum, Map<String, String[]> paraMap, String orderBy) {
		StringBuffer buf = new StringBuffer("from t_user_setting u where 1=1 ");
		for (String paraName : paraMap.keySet()) {
			String prefix = "queryParams[";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					//处理范围参数
					if(field.startsWith("u_account")) {
						buf.append(" and u.u_account like '%"+value+"%'");
					}else if(field.startsWith("u_phone")) {
						buf.append(" and u.u_phone like '%"+value+"%'");
					}
				}
			}
		}
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = " ORDER BY u.u_id asc";
		}else {
			buf.append(" ORDER BY "+orderBy);
		}
		Page<Record>  pages = Db.use("cmtgp_base").paginate(pageNum, pageSize,"select u.* ",buf.toString());
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", pages.getList());
		datagrid.put("total", pages.getTotalRow());
	return datagrid;
	}

}
