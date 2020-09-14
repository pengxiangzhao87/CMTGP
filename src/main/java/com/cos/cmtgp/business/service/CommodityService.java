package com.cos.cmtgp.business.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cos.cmtgp.business.model.CommodityTypeSetting;
import com.cos.cmtgp.common.util.StringUtil;
import com.cos.cmtgp.common.vo.User;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;


public class CommodityService {

	public List<CommodityTypeSetting> selectCategoryList(){
		return CommodityTypeSetting.dao.find("select *from t_commodity_type_setting");
	}

	/**
	 * 热销商品分页查询/模糊查询/类别查询
	 * @param pageNo
	 * @param tId
	 * @param sName
	 * @return
	 */
	public Page<Record> queryCommodityList(Integer userId,Integer pageNo,Integer pageSize,Integer tId,String sName){
		String select = " select a.s_id,a.s_name,SUBSTRING_INDEX(a.s_address_img,'~',1) as coverUrl,a.init_unit,a.init_num,a.price_unit," +
			" a.s_price,a.original_price,a.is_active, " +
			" concat('/',a.s_unit) as unit, " +
			" case when b.id is null then 0 else 1 end as isCar,c.s_corporate_name,count(d.id) as sales,a.state ";
		String from = " from t_commodity_info a " +
					" left join t_supplier_setting c on a.p_id=c.s_id" +
					" left join t_shopping_info b on a.s_id=b.s_id "+ (userId==null?"":"and b.u_id="+userId) +
					" left join t_order_detail d on d.s_id=a.s_id " +
					" where 1=1 ";
		if(tId!=-1){
			from += " and a.t_id="+tId;
		}
		if(!"".equals(sName) && sName!=null){
			from += " and a.s_name like '%"+sName+"%'";
		}
		from += " group by a.s_id order by sales desc,a.s_id desc";
		Page<Record> paginate = Db.paginate(pageNo, pageSize, select, from);

		return paginate;
	}

	/**
	 * 查询单个商品
	 * @param sId
	 * @return
	 */
	public Record queryCommodity(Integer sId,Integer userId){
		String sqlStr = " select a.s_id,a.is_active,a.original_price,a.s_name,a.s_address_img,a.s_address_video,a.s_desc,a.sales_desc,a.price_unit, " +
				" CONCAT('￥',a.s_price) as price, " +
				" concat('/',a.s_unit) as unit, " +
				" a.init_num, a.init_unit,a.state, " +
				" (select count(*) from t_shopping_info b where b.u_id="+userId+") as carSum " +
				" from t_commodity_info a where a.s_id= " + sId ;
		List<Record> records = Db.find(sqlStr);

		return records.get(0);
	}


	public Map<String, Object> findTypePage(int pageSize, int pageNum, Map<String, String[]> paraMap, String orderBy) {
		StringBuffer buf = new StringBuffer("from t_commodity_type_setting u where 1=1 ");
		for (String paraName : paraMap.keySet()) {
			String prefix = "queryParams[";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					//处理范围参数
					if(field.startsWith("t_type")) {
						buf.append(" and u.t_type like '%"+value+"%'");
					}else if(field.startsWith("t_name")) {
						buf.append(" and u.t_name like '%"+value+"%'");
					}
				}
			}
		}
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = " ORDER BY u.t_id asc";
		}else {
			buf.append(" ORDER BY "+orderBy);
		}
		Page<Record>  pages = Db.use("cmtgp_base").paginate(pageNum, pageSize,"select u.* ",buf.toString());
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", pages.getList());
		datagrid.put("total", pages.getTotalRow());
	return datagrid;
	}

	public Map<String, Object> findPage(int pageSize, int pageNum, Map<String, String[]> paraMap, String orderBy,String supplierId) {
		StringBuffer buf = new StringBuffer("from t_commodity_info a,t_commodity_type_setting b,t_supplier_setting c where a.t_id = b.t_id and a.p_id = c.s_id ");
		for (String paraName : paraMap.keySet()) {
			String prefix = "queryParams[";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					//处理范围参数
					if(field.startsWith("pName")) {
						buf.append(" and c.s_corporate_name like '%"+value+"%'");
					}else if(field.startsWith("sName")) {
						buf.append(" and b.t_name like '%"+value+"%'");
					}else if(field.startsWith("s_name")) {
						buf.append(" and a.s_name like '%"+value+"%'");
					}
				}
			}
		}
		
		if(null!=supplierId){
			buf.append(" and p_id ="+supplierId);
		}
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = " ORDER BY u.s_id asc";
		}else {
			buf.append(" ORDER BY "+orderBy);
		}
		Page<Record>  pages = Db.use("cmtgp_base").paginate(pageNum, pageSize,"select a.*,b.t_name sName,c.s_corporate_name pName ",buf.toString());
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", pages.getList());
		datagrid.put("total", pages.getTotalRow());
	return datagrid;
	}
}
