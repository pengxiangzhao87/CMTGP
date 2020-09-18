package com.cos.cmtgp.business.controller;


import java.io.File;
import java.util.*;

import com.cos.cmtgp.business.model.CommodityInfo;
import com.cos.cmtgp.business.model.CommodityTypeSetting;
import com.cos.cmtgp.business.model.SupplierSetting;
import com.cos.cmtgp.business.service.CommodityService;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.vo.User;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.upload.UploadFile;

/**
 *	  商品管理接口
 */
public class CommodityController extends BaseController {
	CommodityService commodityService = enhance(CommodityService.class);

	/**
	 * APP
	 * 查询所有类别
	 */
	public void queryCategoryList(){
		try{
			renderSuccess("",commodityService.selectCategoryList());
		}catch(Exception ex){
			addOpLog("queryCategoryList ===>");
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 热销商品分页查询/模糊查询/类别查询
	 * pageNo 页码
	 * tId 商品分类
	 * @return
	 */
	public void queryCommodityByPage(){
		Integer pageNo = getPager().getPage();
		Integer pageSize = getPager().getRows();
		Integer tId = getParaToInt("tId");
		String sName = getPara("sName");
		Integer userId = getParaToInt("userId");
		try{
			renderSuccess("",commodityService.queryCommodityList(userId,pageNo,pageSize, tId, sName));
		}catch(Exception ex){
			addOpLog("queryCommodityByPage ===> pageNo"+pageNo+",tId="+tId+",sName="+sName+",userId="+userId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 查询单个商品
	 */
	public void queryCollage(){
		Integer sId = getParaToInt("sId");
		Integer userId = getParaToInt("userId");
		try{
			renderSuccess("",commodityService.queryCommodity(sId,userId));
		}catch (Exception ex){
			addOpLog("queryCollage ====> sId"+sId+",userId="+userId);
			ex.printStackTrace();
			renderFailed();
		}

	}

	/**
	 * APP
	 * 查看时令、新商品
	 *
	 */
	public void queryActive(){
		Integer userId = getParaToInt("userId");
		Integer status = getParaToInt("status");
		try{
			String sql = " select a.s_id,a.s_name,SUBSTRING_INDEX(a.s_address_img,'~',1) as coverUrl,a.init_unit,a.init_num,a.price_unit,c.s_corporate_name, " +
					" a.s_price,concat('/',a.s_unit) as unit,case when b.id is null then 0 else 1 end as isCar,count(d.id) as sales,a.state " +
					" from t_commodity_info a " +
					" inner join t_commodity_type_setting e on a.t_id=e.t_id " +
					" left join t_shopping_info b on a.s_id=b.s_id and b.u_id="+ userId +
					" left join t_supplier_setting c on a.p_id=c.s_id " +
					" left join t_order_detail d on d.s_id=a.s_id " +
					" where p_id=1 and e.t_off=0 and is_active="+ status +
					" group by a.s_id order by sales desc,a.s_id desc  limit 20 ";
			List<Record> recordList = Db.find(sql);
			renderSuccess("",recordList);
		}catch (Exception ex){
			addOpLog("queryActive === >userId="+userId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 打折
	 *
	 */
	public void queryOnSales(){
		Integer userId = getParaToInt("userId");
		try{
			List<Record> recordList = new ArrayList<Record>();
			String sqlOne = " select a.s_id,a.s_name,SUBSTRING_INDEX(a.s_address_img,'~',1) as coverUrl,a.init_unit,a.init_num,a.price_unit,a.original_price, " +
					" a.s_price,concat('/',a.s_unit) as unit,c.s_corporate_name,case when b.id is null then 0 else 1 end as isCar,count(d.id) as sales,a.state " +
					" from t_commodity_info a " +
					" left join t_supplier_setting c on a.p_id=c.s_id " +
					" left join t_shopping_info b on a.s_id=b.s_id and b.u_id="+ userId +
					" left join t_order_detail d on d.s_id=a.s_id " +
					" where p_id=1 and is_active=3 " +
					" group by a.s_id order by sales desc,a.s_id desc  limit 15 ";
			recordList.addAll(Db.find(sqlOne));
			String sqlTwo = " select a.s_id,a.s_name,SUBSTRING_INDEX(a.s_address_img,'~',1) as coverUrl,a.init_unit,a.init_num,a.price_unit,a.original_price, " +
					" a.s_price,concat('/',a.s_unit) as unit,c.s_corporate_name,case when b.id is null then 0 else 1 end as isCar,count(d.id) as sales,a.state " +
					" from t_commodity_info a " +
					" left join t_supplier_setting c on a.p_id=c.s_id " +
					" left join t_shopping_info b on a.s_id=b.s_id and b.u_id="+ userId +
					" left join t_order_detail d on d.s_id=a.s_id " +
					" where p_id=2 and is_active=3 " +
					" group by a.s_id order by sales desc,a.s_id desc  limit 15 ";
			recordList.addAll(Db.find(sqlTwo));
			Collections.shuffle(recordList,new Random(47));
			renderSuccess("",recordList);
		}catch (Exception ex){
			addOpLog("queryNews");
			ex.printStackTrace();
			renderFailed();
		}
	}


	/**
	 * 商品类型添加页面
	 */
	public void addTypePage() {
		render("CommodityType_add.html");
	}
	
	/**
	 * 商品类型添加
	 */
	public void addCommodityType() {
		CommodityTypeSetting commodityType = getModel(CommodityTypeSetting.class, "model");
		if(commodityType.save()) {
			renderSuccess("商品类型添加成功");
		}else {
			renderFailed("商品类型加失败");
		}
	}
	
	/**
	 * 商品类型修改页面
	 */
	public void updateTypePage() {
		setAttr("model", CommodityTypeSetting.dao.findById(getPara("id")));
		render("CommodityType_update.html");
	}
	
	/**
	 * 商品类型修改
	 */
	public void updateCommodityType() {
		if(getModel(CommodityTypeSetting.class, "model").update()) {
			renderSuccess();
		}else {
			renderFailed();
		}
	}	
	
	/**
	 * 商品类型删除
	 */
	public void deleteCommodityType() {
		try {
			Integer[] ids = getParaValuesToInt("id[]");
			for (Integer id : ids) {
				new CommodityTypeSetting().set("t_id", id).delete();
			}
			renderSuccess();
		} catch (Exception e) {
			renderFailed("商品类型删除失败");
			e.printStackTrace();
		}
	}	
	
	/**
	 * 商品类型查询页面
	 */
	public void listTypePage() {
		render("CommodityType_list.html");
	}
	
	/**
	 * 商品类型查询数据
	 */
	public void listTypeData() {
		Map<String, String[]> paraMap = getParaMap();
		String orderBy = getOrderBy();
		int pageNum = getPager().getPage();
		int pageSize = getPager().getRows();
		renderJson(commodityService.findTypePage(pageSize,pageNum,paraMap,orderBy));
	}
	
	/**
	 * 商品添加页面
	 */
	public void addPage() {
		render("Commodity_add.html");
	}
	
	/**
	 * 商品添加数据
	 */
	public void addCommodity() {
		List<UploadFile> uploadFiles = getFiles();
		CommodityInfo commodity = getModel(CommodityInfo.class, "model");
		commodity.set("sales_desc","sales_desc.jpg");
		if(uploadFiles!=null&&uploadFiles.size()>0) {
			StringBuffer buf = new StringBuffer();
			for(UploadFile f:uploadFiles) {
				if(f.getFileName().contains(".jpg")||f.getFileName().contains(".gif")||f.getFileName().contains(".png")) {
					if(f.getParameterName().contains("model.s_address_img")){
						buf.append(f.getFileName()+"~");
					}else if(f.getParameterName().equals("model.s_desc")){
						commodity.set("s_desc",f.getFileName());
					}else if(f.getParameterName().equals("model.sales_desc")){
						commodity.set("sales_desc",f.getFileName());
					}
				}else if(f.getFileName().contains(".mp4")){
					commodity.set("s_address_video",f.getFileName());
				}
			}
			if(buf.length()>0){
				commodity.set("s_address_img", buf.toString().substring(0,buf.length()-1));
			}
		}
		if(commodity.save()) {
			renderSuccess("商品添加成功");
		}else {
			renderFailed("商品添加失败");
		}
	}
	
	/**
	 * 商品修改页面
	 */
	public void updatePage() {
		setAttr("model", CommodityInfo.dao.findById(getPara("id")));
		render("Commodity_update.html");
	}

	/**
	 * 商品修改
	 */
	public void updateCommodity() {
		List<UploadFile> uploadFiles = getFiles();
		CommodityInfo model = getModel(CommodityInfo.class, "model");
		if(uploadFiles!=null&&uploadFiles.size()>0) {
			StringBuffer buf = new StringBuffer();
			for(UploadFile f:uploadFiles) {
				if(f.getFileName().contains(".jpg")||f.getFileName().contains(".gif")||f.getFileName().contains(".png")) {
					if(f.getParameterName().contains("model.s_address_img")){
						buf.append(f.getFileName()+"~");
					}else if(f.getParameterName().equals("model.s_desc")){
						model.set("s_desc", f.getFileName());
					}else if(f.getParameterName().equals("model.sales_desc")){
						model.set("sales_desc", f.getFileName());
					}
				}else if(f.getFileName().contains(".mp4")){
					model.set("s_address_video",  f.getFileName());
				}
			}
			if (buf.length()>0) {
				model.set("s_address_img", buf.toString().substring(0, buf.length()-1));
			}
		}
		if(model.update()) {
			renderSuccess("商品修改成功");
		}else {
			renderFailed("商品修改失败");
		}
	}	
	
	/**
	 * 商品删除
	 */
	public void deleteCommodity() {
		try {
			Integer[] ids = getParaValuesToInt("id[]");
			for (Integer id : ids) {
				new CommodityInfo().set("s_id", id).delete();
			}
			renderSuccess("商品上传完成");
		} catch (Exception e) {
			renderFailed("商品删除失败");
			e.printStackTrace();
		}
	}	
	
	/**
	 * 商品查询页面
	 */
	public void listPage() {
		render("Commodity_list.html");
	}
	
	/**
	 * 商品查询数据
	 */
	public void listData() {
		Map<String, String[]> paraMap = getParaMap();
		String orderBy = getOrderBy();
		int pageNum = getPager().getPage();
		int pageSize = getPager().getRows();
		String supplierId = ((User) getSession().getAttribute("sysUser")).getSupplierId();
		renderJson(commodityService.findPage(pageSize,pageNum,paraMap,orderBy,supplierId));
	}
	
	/**
	 * 获取配置列表数据
	 */
	public void getCommodityList() {
		 String type = getPara("type");
		if(type.equals("commodity")) {
			renderJson(Db.find("select t_id id,t_name text from t_commodity_type_setting"));
		}else if(type.equals("commoditytype")) {
			StringBuffer buf = new StringBuffer();
			buf.append("select s_id id,s_corporate_name text from t_supplier_setting");
			String supplierId = ((User) getSession().getAttribute("sysUser")).getSupplierId();
			if(null!=supplierId) {
				buf.append(" where s_id = "+supplierId);
			}
			List<Record> recordList = Db.find(buf.toString());
			Record record = recordList.get(0);
			record.set("selected",true);
			renderJson(recordList);
		}else if(type.equals("isActive")) {
			String supplierId = ((User) getSession().getAttribute("sysUser")).getSupplierId();
			List<Record> list = new ArrayList<Record>();
			Record r = new Record();
			r.set("id", 0);
			r.set("text", "否");
			list.add(r);
			Record r2 = new Record();
			r2.set("id", 3);
			r2.set("text", "折扣");
			list.add(r2);
			if(null!=supplierId) {
				if(supplierId.equals("1")) {
					Record r3 = new Record();
					r3.set("id", 1);
					r3.set("text", "时令水果");
					list.add(r3);
				}else {
					Record r4 = new Record();
					r4.set("id", 2);
					r4.set("text", "新商品");
					list.add(r4);
				}
			}else {
				Record r3 = new Record();
				r3.set("id", 1);
				r3.set("text", "时令水果");
				list.add(r3);
				Record r4 = new Record();
				r4.set("id", 2);
				r4.set("text", "新商品");
				list.add(r4);
			}
			renderJson(list);
		}
	}
	
}
