package com.cos.cmtgp.system.controller;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.alibaba.fastjson.JSON;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.DateUtil;
import com.cos.cmtgp.common.util.PasswordUtil;
import com.cos.cmtgp.common.util.StringUtil;
import com.cos.cmtgp.system.service.CollectionService;
import com.cos.cmtgp.system.service.LoginService;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * app接口信息
 */
public class CollectionController extends BaseController {
	LoginService loginService = enhance(LoginService.class);
	CollectionService collectionService = enhance(CollectionService.class);
	
	/**
	 * 	登录
	 */
	public void login() {
		Map<String,Object> map = new HashMap<String, Object>();
		try {
			String user_account = getPara("user_account");
			String user_password = getPara("user_password");
			Record record = collectionService.findUser(user_account,PasswordUtil.encodePassword(user_password));
			if(null!=record) {	
				map.put("msg", "登录成功");
				map.put("data", collectionService.findAll(record.getStr("org_id")));
				map.put("result", "success");
			}else {
				map.put("msg", "确认账号密码是否正确");
				map.put("data", null);
				map.put("result", "failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("msg", "服务异常");
			map.put("data", null);
			map.put("result", "failed");
		}
		renderJson(map);
	}
	
	/**
	 * 获取组织机构
	 */
	public void getOrgList() {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String user_account = getPara("user_account");
			List<Record> record = collectionService.getOrgList(user_account);
			if(null!=record&&record.size()>0) {
				map.put("msg", "登录成功");
				map.put("data", record);
				map.put("result", "success");
			}else {
				map.put("msg", "无有效数据");
				map.put("data", null);
				map.put("result", "failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("msg", "服务异常");
			map.put("data", null);
			map.put("result", "failed");
		}
		
		renderJson(map);
	}
	
	/**
	 * 人员设备绑定
	 */
	public void setEmployeeBindings() {
		try {
			String IDCard = getPara("IDCard");
			String device_code = getPara("device_code");
			Record  record = Db.use("tcms_base").findFirst("select * from t_employee where device_code ='"+device_code+"'");
			if(null!=record) {
				renderFailed("该设备编号已被绑定");
			}
			 if(collectionService.updateEmpl(IDCard,device_code)) {
				 renderSuccess("人员设备绑定成功");
			 }else {
				 renderFailed("人员设备绑定失败");
			 }
		} catch (Exception e) {
			renderFailed("服务异常");
			e.printStackTrace();
		}
	}
	
	
	/**
	 *	 app列表查询
	 */
	public void getCollections() {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String user_account = getPara("user_account");
			String findDate = getPara("findDate");
			String orgId = getPara("orgId");
			int pageNum = getParaToInt("pageNum"); 
			int pageSize = getParaToInt("pageSize");
			List<Record> record = collectionService.selectCollects(findDate,orgId,pageNum,pageSize,null,user_account,null,null).getList();
			if(null!=record&&record.size()>0) {
				 map.put("msg", "成功请求");
				 map.put("result", "success");
				 map.put("data", record);
			}else {
				map.put("msg", "请求失败");
				 map.put("result", "failed");
				 map.put("data", null);
			}
		} catch (Exception e) {
			map.put("msg", "服务异常");
			map.put("result", "failed");
			e.printStackTrace();
		}
		renderJson(map);
	}
	
	/**
	 * 	 app列表查询明细
	 */
	public void getCollectiondetail() {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String findDate = getPara("findDate");
			String IDCard = getPara("IDCard");
			String pageNum = getPara("pageNum");
			String pageSize = getPara("pageSize");
			List<Record> record = collectionService.selectCollectiondetail(findDate,IDCard,pageNum,pageSize).getList();
			if(null!=record&&record.size()>0) {
				 map.put("msg", "成功请求");
				 map.put("result", "success");
				 map.put("data", record);
			}else {
				map.put("msg", "请求失败");
				map.put("result", "failed");
				map.put("data", null);
			}
		} catch (Exception e) {
			map.put("msg", "服务异常");
			map.put("result", "failed");
			e.printStackTrace();
		}
		renderJson(map); 
	}
	
	
	/**
	 * 	采集查询页面
	 */
	public void detailPage() {
		//setAttr("dictDatatarget_type", TaskBase.me.getDictDatatarget_type());
		//setAttr("dictDatastatus", TaskBase.me.getDictDatastatus());
		String date = getPara("findDate");
		if(!StringUtil.isEmpty(date)&&!date.equals("0")) {
			setAttr("findDate", DateUtil.getDayStr(date)); 
		}else {
			setAttr("findDate","0"); 
		}
		setAttr("IDCard", getPara("IDCard"));
		render("detail.html");
	}
	
	
	/**
	 * 	 采集查询数据
	 */
	public void detailData() {
		Map<String, String[]> paraMap = getParaMap();
		String findDate = getPara("findDate");
		try {
			if(!findDate.equals("0")) {			
				Date d = new Date();
				d.setTime(Long.parseLong(findDate));
				findDate = DateUtil.getDay(d);
			}else {
				findDate = DateUtil.getDay(new Date());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String IDCard = getPara("IDCard");
		String orderBy = getOrderBy();
		int pageNum = getPager().getPage();
		int pageSize = getPager().getRows();
		renderJson(collectionService.finddetailPage(pageSize,pageNum,findDate,IDCard)); 
	}
	
	/**
	 * 	采集详情查询页面
	 */
	public void listPage() {
		//setAttr("dictDatatarget_type", TaskBase.me.getDictDatatarget_type());
		//setAttr("dictDatastatus", TaskBase.me.getDictDatastatus());
		render("list.html");
	}
	
	
	
	/**
	 * 	数据导出
	 */
	public void exportDatagrId(){
		String sdata=getPara("data");
		String sfilename=getPara("filename");
		String sheetName="";
		if(sfilename==null||"".equals(sfilename)){
			sfilename="excel.xls";
			sheetName="sheet1";
		}else{
			sheetName=sfilename;
			sfilename=sfilename+".xls";
		}
		String[][] datas=toArr(sdata);
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(sheetName);
		HSSFCellStyle contextstyle =workbook.createCellStyle();
		try {
			HSSFRow row = null;
			HSSFCell cell;
			for (int i = 0; i < datas.length; i++) {
				row = sheet.createRow(i);
				for (int j = 1; j < datas[i].length; j++) {
					cell = row.createCell(j-1);
					cell.setCellValue(datas[i][j]);
				}
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			byte[] ba = baos.toByteArray();
			ByteArrayInputStream is = new ByteArrayInputStream(ba);
			BufferedInputStream bins = new BufferedInputStream(is);// 放到缓冲流里面
			
			OutputStream outs = getResponse().getOutputStream();// 获取文件输出IO流
			BufferedOutputStream bouts = new BufferedOutputStream(outs);
			getResponse().setContentType("application/x-download");// 设置response内容的类型
			getResponse().setHeader("Content-disposition", "attachment;filename="+ URLEncoder.encode(sfilename, "utf-8"));// 设置头部信息
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			// 开始向网络传输文件流
			while ((bytesRead = bins.read(buffer, 0, 8192)) != -1) {
				bouts.write(buffer, 0, bytesRead);
			}
			bouts.flush();// 这里一定要调用flush()方法
			is.close();
			bins.close();
			baos.close();
			outs.close();
			bouts.close();
			sheet=null;
			workbook=null;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			renderNull();
		}
	}
	
	private String [][] toArr(String sdata){
		String [] rowStrs=sdata.split("LSeparation--");

		String [] cellStrs;
		String [][] arr=null;
		for(int i=0;i<rowStrs.length;i++){
			cellStrs=rowStrs[i].split("CSeparation--");
			List<String> strings = Arrays.asList(cellStrs);
			if(strings.contains(""))
			if(null==arr){
				arr=new String [rowStrs.length][cellStrs.length];
			}
			for(int j=0;j<cellStrs.length;j++){
				arr[i][j]=cellStrs[j];
			}
		}
		return arr;
	}
	
	/**
	 * 	数据导出
	 */
	public void exportDetailId(){
		String sdata=getPara("data");
		String sfilename=getPara("filename");
		String sheetName="";
		if(sfilename==null||"".equals(sfilename)){
			sfilename="excel.xls";
			sheetName="sheet1";
		}else{
			sheetName=sfilename;
			sfilename=sfilename+".xls";
		}
		String[][] datas=toArr(sdata);
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(sheetName);
		HSSFCellStyle contextstyle =workbook.createCellStyle();
		try {
			HSSFRow row = null;
			HSSFCell cell;
			for (int i = 0; i < datas.length; i++) {
				row = sheet.createRow(i);
				for (int j = 1; j < datas[i].length; j++) {
					cell = row.createCell(j-1);
					cell.setCellValue(datas[i][j]);
				}
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			byte[] ba = baos.toByteArray();
			ByteArrayInputStream is = new ByteArrayInputStream(ba);
			BufferedInputStream bins = new BufferedInputStream(is);// 放到缓冲流里面
			
			OutputStream outs = getResponse().getOutputStream();// 获取文件输出IO流
			BufferedOutputStream bouts = new BufferedOutputStream(outs);
			getResponse().setContentType("application/x-download");// 设置response内容的类型
			getResponse().setHeader("Content-disposition", "attachment;filename="+ URLEncoder.encode(sfilename, "utf-8"));// 设置头部信息
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			// 开始向网络传输文件流
			while ((bytesRead = bins.read(buffer, 0, 8192)) != -1) {
				bouts.write(buffer, 0, bytesRead);
			}
			bouts.flush();// 这里一定要调用flush()方法
			is.close();
			bins.close();
			baos.close();
			outs.close();
			bouts.close();
			sheet=null;
			workbook=null;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			renderNull();
		}
	}
	
	
	public void getCombobox() {
		List list =new ArrayList<Map<String,String>>();
		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("id", "0");
		paraMap.put("text", "正常");
		list.add(paraMap);
		Map<String, String> paraMapc = new HashMap<String, String>();
		paraMapc.put("id", "1");
		paraMapc.put("text", "异常");
		list.add(paraMapc);
		renderJson(list);
	}
	
	public void getAlarmValue() {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String user_account = getPara("user_account");
    		Record  record = Db.use("tcms_base").findFirst("select org_id from t_user where user_account = '"+user_account+"'");
    	
    		String tmepId = "";
    		if(null!=record) {
    			String orgId = record.getStr("org_id");
            	tmepId = orgId;
            	if(Integer.parseInt(orgId)!=1) {
            		for(int i=0;i<5;i++) {
                		Record  r = Db.use("tcms_base").findFirst("select org_pid from t_org_structure where org_id="+tmepId);
                		if(Integer.parseInt(r.get("org_pid").toString())==1) {
                    		orgId = tmepId;
                    		break;
                    	}else {
                    		tmepId = r.get("org_pid").toString();
                    	}
                	}
            	}
    		}else {
    			map.put("msg", "数据请求失败");
				map.put("result", "failed");
				map.put("data", null);
				renderJson(map); 
				return;
    		}
    		Record  rc = Db.use("tcms_base").findFirst("select * from t_alarm_value where org_id="+tmepId);
    		if(null!=rc) {
				 map.put("msg", "成功请求");
				 map.put("result", "success");
				 map.put("data", rc);
			}else {
				Record r = new Record();
				r.set("upper_limit", 37);
				r.set("lower_limit", 35);
				map.put("msg", "系统默认参数");
				map.put("result", "success");
				map.put("data", r);
			}
		} catch (Exception e) {
			map.put("msg", "服务异常");
			map.put("result", "failed");
			e.printStackTrace();
		}
		renderJson(map); 
	}
	
}
