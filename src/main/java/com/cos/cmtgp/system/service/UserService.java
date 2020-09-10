package com.cos.cmtgp.system.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

public class UserService {

	public Map<String, Object> findPage(int pageSize, int pageNum, Map<String, String[]> paraMap, String orderBy,String orgId) {
		boolean b = true;
		StringBuffer buf = new StringBuffer("from t_employee u ,t_org_structure o where u.org_id = o.org_id ");
		boolean bl = false;
		String temp = "";
		for (String paraName : paraMap.keySet()) {
			String prefix = "queryParams[";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefix.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					//处理范围参数
					if(field.startsWith("name")) {
						buf.append(" and u.name like '%"+value+"%'");
					}else if(field.startsWith("IDCard")) {
						buf.append(" and u.IDCard like '%"+value+"%'");
					}else  if(field.startsWith("org_id")) {
						if(!StringUtil.isEmpty(value)) {							
							b = false;
							buf.append(" and u.org_id in("+getOrgs(value)+") ");
						}
					}
				}
			}
			String prefixc = "queryParams[0][";
			if(paraName.startsWith(prefix)) {
				String field = paraName.substring(prefixc.length(), paraName.length() - 1);
				String value = paraMap.get(paraName)[0];
				if(!StringUtil.isEmpty(value)) {
					if(field.startsWith("name")) {
						buf.append(" and u.name like '%"+value+"%'");
					}else if(field.startsWith("IDCard")) {
						buf.append(" and u.IDCard like '%"+value+"%'");
					}else  if(field.startsWith("org_id")) {
						if(!StringUtil.isEmpty(value)) {							
							b = false;
							buf.append(" and u.org_id in("+getOrgs(value)+") ");
						}
					}
				}
			}
		}
		if(b) {
			buf.append(" and u.org_id in("+getOrgs(orgId)+") ");
		}
		if(StringUtil.isEmpty(orderBy)) {
			orderBy = " ORDER BY u.id asc";
		}else {
			buf.append(" ORDER BY "+orderBy);
		}
		Page<Record>  pages = Db.use("tcms_base").paginate(pageNum, pageSize,"select u.id, u.name, u.org_id, u.role_id, CASE u.sex WHEN 0 THEN '女' WHEN 1 THEN '男' END sex, u.phone, u.IDCard, u.family_phone,u.device_code,o.org_name ",buf.toString());
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

	
	public List<Record> orgsJson(String orgId) {
		return Db.use("tcms_base").find("select * from t_org_structure where org_id in("+getOrgs(orgId)+")");
	}

	public Object findById(Integer paraToInt) {
		return null;
	}

	public Record findById(String IDCard) {
		return Db.use("tcms_base").findFirst("select * from t_employee where IDCard= '"+IDCard+"'");
	}

	public Map<String, Object> Checkexcel(List<UploadFile> uploadFiles) {
		List<Record> page = new ArrayList<Record>();
		StringBuffer buf = new StringBuffer();
		boolean bl = false;
		for (UploadFile uploadFile : uploadFiles) {
			Record rc = new Record();
			try {
				if(uploadFile.getFileName().contains(".xlsx")) {					
					//bl = ckeckExcelxlsx(uploadFile.getUploadPath()+File.separator+ uploadFile.getFileName(),page);
				}else if(uploadFile.getFileName().contains(".xls")){
					bl = ckeckExcelxls(uploadFile.getUploadPath()+File.separator+ uploadFile.getFileName(),page);
				}
				if(bl) {
					if(uploadFile.getFileName().contains(".xlsx")) {
						//saveExcelxlsx(uploadFile.getUploadPath()+File.separator+ uploadFile.getFileName());						
					}else if(uploadFile.getFileName().contains(".xls")) {
						saveExcelxls(uploadFile.getUploadPath()+File.separator+ uploadFile.getFileName());												
					}
					rc.set("sheetName", uploadFile.getFileName());
	            	rc.set("state", "0");
					rc.set("Result", "数据导入完成！");
					page.add(rc);
				}
			} catch (IOException e1) {
				rc.set("sheetName", uploadFile.getFileName());
            	rc.set("state", "1");
				rc.set("Result", "数据验证异常！");
			}
		}
		Map<String, Object> datagrid = new HashMap<String, Object>();
		datagrid.put("rows", page);
		datagrid.put("total", page.size());
	return datagrid;
	}
	
	
	private boolean ckeckExcelxls(String filePath,List<Record> rc) throws IOException {
		HSSFWorkbook workBook= null;
	 InputStream is = new FileInputStream(filePath);
	 try {
	     workBook = new HSSFWorkbook(is);
	 } catch (Exception e) {
	     e.printStackTrace();
	 }
	 
	 //循环工作表sheet
	 for(int numShett = 0;numShett<workBook.getNumberOfSheets();numShett++){
		 Record record = new Record();
	     HSSFSheet sheet  = workBook.getSheetAt(numShett);
			if(sheet==null){
			   continue;
	     }
			String sheetName = sheet.getSheetName();
	     //循环Row
	     for(int rowNum=1;rowNum<=sheet.getLastRowNum();rowNum++){
	    	 HSSFRow row = sheet.getRow(rowNum);
	         if(row==null){
	             continue;
	         }
        	 HSSFCell name = row.getCell(0);
        	 HSSFCell orgName = row.getCell(1);
        	 HSSFCell IDCard = row.getCell(2);
        	 HSSFCell sex = row.getCell(3);
        	 HSSFCell phone = row.getCell(4);
        	 HSSFCell family_phone = row.getCell(5);
        	 HSSFCell device_code = row.getCell(6);
             if(name==null||orgName==null || IDCard==null || sex==null || phone==null || family_phone==null) {
            	 record.set("sheetName", sheetName);
            	 record.set("Result", "-第："+(rowNum+1)+"行有空值！");
            	 record.set("state", "1");
            	 rc.add(record);
             }
             if(!getValue(orgName).trim().contains("_")) {
            	 record.set("sheetName", sheetName);
            	 record.set("Result", "-第："+(rowNum+1)+"行无效的组织机构！");
            	 record.set("state", "1");
            	 rc.add(record);
             }
             Record r = Db.use("tcms_base").findFirst("select * from t_employee where IDCard ='"+IDCard+"'");
             if(null!=r) {
            	 record.set("sheetName", sheetName);
            	 record.set("Result", "-第："+(rowNum+1)+"行身份证号已存在！");
            	 record.set("state", "1");
            	 rc.add(record);
             }
	     }
	 }
	 return rc.size()>0?false:true;
	}
	
	 /**
     * 得到Excel表中的值
     * @param hssfCell
     * @return String
     */
    @SuppressWarnings("unused")
    private String getValue(Cell cell){
        DecimalFormat df = new DecimalFormat("###################.###########");
        if(cell.getCellType()==cell.CELL_TYPE_BOOLEAN){
            return String.valueOf(cell.getBooleanCellValue());
        }
        if(cell.getCellType()==cell.CELL_TYPE_NUMERIC){
            return String.valueOf(df.format(cell.getNumericCellValue()));
        }else{
        	cell.setCellType(Cell.CELL_TYPE_STRING);
            return String.valueOf(cell.getStringCellValue());
        }
    }
	
	private boolean ckeckExcelxlsx(String filePath,List<Record> rc) throws IOException {
		 XSSFWorkbook workBook= null;
		 InputStream is = new FileInputStream(filePath);
		 try {
		     workBook = new XSSFWorkbook(is);
		 } catch (Exception e) {
		     e.printStackTrace();
		 }
		 //循环工作表sheet
		 for(int numShett = 0;numShett<workBook.getNumberOfSheets();numShett++){
			 Record record = new Record();
		     XSSFSheet sheet  = workBook.getSheetAt(numShett);
				if(sheet==null){
				   continue;
		     }
				String sheetName = sheet.getSheetName();
		     //循环Row
		     for(int rowNum=1;rowNum<=sheet.getLastRowNum();rowNum++){
		         Row row = sheet.getRow(rowNum);
		         if(row==null){
		             continue;
		         }
		     }
		 }
		 return true;
		}
		
	    /**
	     * 保存Excel数据 
	* @param filePath
	* @return List
	* @throws IOException
	*/
	private void saveExcelxlsx(String filePath) throws IOException {
		XSSFWorkbook workBook= null;
	 InputStream is = new FileInputStream(filePath);
	 try {
	     workBook = new XSSFWorkbook(is);
	 } catch (Exception e) {
	     e.printStackTrace();
	 }
	 //循环工作表sheet
	 for(int numShett = 0;numShett<workBook.getNumberOfSheets();numShett++){
	     XSSFSheet sheet  = workBook.getSheetAt(numShett);
			if(sheet==null){
			   continue;
	     }
			String sheetName = sheet.getSheetName();
	     //循环Row
	     for(int rowNum=1;rowNum<=sheet.getLastRowNum();rowNum++){
	    	 XSSFRow row = sheet.getRow(rowNum);
	         if(row==null){
	             continue;
	         }
	     }
	 }
	}
	    
	    /**
	     * 保存Excel数据 
	* @param filePath
	* @return List
	* @throws IOException
	*/
	private void saveExcelxls(String filePath) throws IOException {
		HSSFWorkbook workBook= null;
	 InputStream is = new FileInputStream(filePath);
	 try {
	     workBook = new HSSFWorkbook(is);
	 } catch (Exception e) {
	     e.printStackTrace();
	 }
	 //循环工作表sheet
	 for(int numShett = 0;numShett<workBook.getNumberOfSheets();numShett++){
	     HSSFSheet sheet  = workBook.getSheetAt(numShett);
			if(sheet==null){
			   continue;
	     }
			String sheetName = sheet.getSheetName();
	     //循环Row
	     for(int rowNum=1;rowNum<=sheet.getLastRowNum();rowNum++){
	    	 HSSFRow row = sheet.getRow(rowNum);
	         if(row==null){
	             continue;
	         }
	         Record record = new Record();
	         HSSFCell name = row.getCell(0);
	         record.set("name", getValue(name).trim());
        	 HSSFCell orgName = row.getCell(1);
        	 record.set("org_id", getValue(orgName).trim().split("_")[0]);
        	 HSSFCell IDCard = row.getCell(2);
        	 record.set("IDCard", getValue(IDCard).trim());
        	 HSSFCell sex = row.getCell(3);
        	 if(getValue(sex).trim().equals("男")) {
        		 record.set("sex", 1);
        	 }else {        		 
        		 record.set("sex", 0);
        	 }
        	 HSSFCell phone = row.getCell(4);
        	 record.set("phone", getValue(phone).trim());
        	 HSSFCell family_phone = row.getCell(5);
        	 record.set("family_phone", getValue(family_phone).trim());
        	 HSSFCell device_code = row.getCell(6);
        	 if(null!=device_code) {        		 
        		 record.set("device_code", getValue(device_code).trim());
        	 }
        	// Db.use("tcms_base").delete("delete from t_employee where IDCard ='"+IDCard+"'");
             Db.use("tcms_base").save("t_employee", record);
	     }
	 }
	}
	
}
