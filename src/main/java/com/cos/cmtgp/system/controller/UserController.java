package com.cos.cmtgp.system.controller;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.StringUtil;
import com.cos.cmtgp.common.vo.User;
import com.cos.cmtgp.system.service.LoginService;
import com.cos.cmtgp.system.service.UserService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

/**
 * 人员检测信息
 */
public class UserController extends BaseController {
	LoginService loginService = enhance(LoginService.class);
	UserService userService = enhance(UserService.class);
	
	
	/**
	 * 人员查询页面
	 */
	public void listPage() {
		//setAttr("dictDatatarget_type", TaskBase.me.getDictDatatarget_type());
		//setAttr("dictDatastatus", TaskBase.me.getDictDatastatus());
		render("list.html");
	}
	
	
	/**
	 * 人员查询数据
	 */
	public void listData() {
		Map<String, String[]> paraMap = getParaMap();
		String orderBy = getOrderBy();
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = "u.org_id asc";
		}
		int pageNum = getPager().getPage();
		int pageSize = getPager().getRows();
		User user = getSessionAttr("sysUser"); 
		renderJson(userService.findPage(pageSize,pageNum,paraMap,orderBy,""));
	}
	
	
	/**
	 * 	人员查询页面
	 */
	public void addPage() {
		//setAttr("dictDatatarget_type", TaskBase.me.getDictDatatarget_type());
		//setAttr("dictDatastatus", TaskBase.me.getDictDatastatus());
		setAttr("orgName", getPara("orgName"));
		setAttr("orgId", getPara("orgId"));
		render("add.html");
	}
	
	
	/**
	 * 获取上传图片
	 */
	public void upload() {
		 List<UploadFile> uploadFiles = getFiles();
		 Map<String, Object> datagrid = userService.Checkexcel(uploadFiles);
		 renderJson(datagrid);
  }
	
	public void download(){
		String sfilename="人员信息录入模板.xls";
		String sheetName="人员信息录入模板";
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(sheetName);
		HSSFCellStyle setBorder1 = workbook.createCellStyle();
		HSSFFont font1 = workbook.createFont();
		HSSFRow row = sheet.createRow(0);
		font1.setFontName("Arial");
		font1.setFontHeightInPoints((short) 10);//设置字体大小
		setBorder1.setFont(font1);//选择需要用到的字体格式
		setBorder1.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
		setBorder1.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		setBorder1.setFillForegroundColor(IndexedColors.ROSE.getIndex());  //设置前景色
		User user = getSessionAttr("sysUser");
		 List<Record> rc = Db.use("tcms_base").find("select org_id,org_name from t_org_structure where FIND_IN_SET(org_id,getOrgChildLst("+""+"))");
		 String[] textlist = new String[rc.size()];  
		 for(int i=0;i<rc.size();i++) {
			 String orgName = rc.get(i).get("org_name");
			 String orgId = rc.get(i).get("org_id").toString();
			 textlist[i]=orgId+"_"+orgName;
		 }
		  
		 sheet = setHSSFValidation(sheet, textlist, 1, 1000, 1, 1);// 第一列的前501行都设置为选择列表形式. 
		try {
			HSSFCell cell0 = row.createCell(0);
			cell0.setCellValue("姓名");
			cell0.setCellStyle(setBorder1);
			
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue("所属机构");
			cell1.setCellStyle(setBorder1);
			
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue("身份证号");
			cell2.setCellStyle(setBorder1);
			
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue("性别");
			cell3.setCellStyle(setBorder1);
			
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue("电话");
			cell4.setCellStyle(setBorder1);
			
			HSSFCell cell5 = row.createCell(5);
			cell5.setCellValue("家属电话");
			cell5.setCellStyle(setBorder1);
			
			HSSFCell cell6 = row.createCell(6);
			cell6.setCellValue("设备编号");
			HSSFCellStyle setBorder2 = workbook.createCellStyle();
			HSSFFont font2 = workbook.createFont();
			font2.setFontName("Arial");
			font2.setFontHeightInPoints((short) 12);//设置字体大小
			setBorder2.setFont(font2);//选择需要用到的字体格式
			cell6.setCellStyle(setBorder2);
			
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
	
	/** 
     * 设置某些列的值只能输入预制的数据,显示下拉框. 
     *  
     * @param sheet 
     *            要设置的sheet. 
     * @param textlist 
     *            下拉框显示的内容 
     * @param firstRow 
     *            开始行 
     * @param endRow 
     *            结束行 
     * @param firstCol 
     *            开始列 
     * @param endCol 
     *            结束列 
     * @return 设置好的sheet. 
     */  
    public static HSSFSheet setHSSFValidation(HSSFSheet sheet,  
            String[] textlist, int firstRow, int endRow, int firstCol,  
            int endCol) {  
        // 加载下拉列表内容  
        DVConstraint constraint = DVConstraint  
                .createExplicitListConstraint(textlist);  
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列  
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,  
                endRow, firstCol, endCol);  
        // 数据有效性对象  
        HSSFDataValidation data_validation_list = new HSSFDataValidation(  
                regions, constraint);  
        sheet.addValidationData(data_validation_list);  
        return sheet;  
    }
	
	public void listOrgsJson() {
		User user = getSessionAttr("sysUser");
		renderJson(userService.orgsJson(""));
	}
	
	
}
