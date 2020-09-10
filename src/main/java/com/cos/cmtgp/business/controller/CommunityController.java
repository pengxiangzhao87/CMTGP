package com.cos.cmtgp.business.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cos.cmtgp.business.model.CommodityInfo;
import com.cos.cmtgp.business.model.HotissueBasic;
import com.cos.cmtgp.business.model.HotissueDetail;
import com.cos.cmtgp.business.model.HotissueReply;
import com.cos.cmtgp.business.service.CommunityService;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.VideoImg;
import com.cos.cmtgp.system.service.LoginService;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import javax.servlet.http.HttpServletRequest;

/**
 *	  社区接口
 */
public class CommunityController extends BaseController {
	LoginService loginService = enhance(LoginService.class);
	CommunityService communityService = enhance(CommunityService.class);

	/**
	 * APP
	 * 发布
	 */
	public void sendHotissue(){
		List<UploadFile> uploadFiles = getFiles();
		String json = getPara("hotissueBasic");
		try{
			HotissueBasic hotissueBasic = FastJson.getJson().parse(json, HotissueBasic.class);
			StringBuffer imagUrl = new StringBuffer();
			for(UploadFile file : uploadFiles){
				if(file.getParameterName().equals("videoFile")){
					String imgFile = System.currentTimeMillis()+"jpg";
					String imgUrl=file.getUploadPath()+"\\"+System.currentTimeMillis()+imgFile;
					String videoUrl=file.getUploadPath()+"\\"+file.getFileName();
					VideoImg.getTempPath(imgUrl,videoUrl);
					hotissueBasic.setHAddressImg(imgFile);
					hotissueBasic.setHAddressVideo(file.getFileName());
				}else{
					imagUrl.append(file.getFileName()+"~");
				}
			}
			if(imagUrl.length()>0){
				hotissueBasic.setHAddressImg(imagUrl.substring(0,imagUrl.length()-1));
			}
			if(communityService.insertHotissue(hotissueBasic)){
				renderSuccess();
			}else{
				renderFailed();
			}
		}catch(Exception ex){
			addOpLog("sendHotissue ===> json="+json);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 删除
	 */
	public void deleteHotissue(){
		Integer hId = getParaToInt("hId");
		try{
			if(communityService.deleteHotissue(hId)){
				renderSuccess();
			}else{
				renderFailed();
			}
		}catch(Exception ex){
			addOpLog("deleteHotissue ===> hId="+hId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * 新增第一层评论
	 */
	public void addReplyFirst(){
		String json = HttpKit.readData(getRequest());
		try{
			HotissueDetail hotissueDetail = FastJson.getJson().parse(json, HotissueDetail.class);
			communityService.replyFirst(hotissueDetail);
			renderSuccess();
		}catch(Exception ex){
			addOpLog("addReplyFirst ===> json="+json);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * 删除第一层评论
	 */
	public void deleteReplyFirst(){
		Integer hId = getParaToInt("hId");
		Integer dId = getParaToInt("dId");
		try{
			communityService.deleteFirst(hId,dId);
			renderSuccess();
		}catch(Exception ex){
			addOpLog("deleteReplyFirst ===> hId="+hId+",dId="+dId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 查询第一层评论
	 */
	public void queryFirstReply(){
		Integer hId = getParaToInt("hId");
		Integer uId = getParaToInt("uId");
		try{
			renderSuccess("",communityService.queryFirstReply(hId,uId));
		}catch(Exception ex){
			addOpLog("queryFirstReply ===> hId="+hId + ",uId="+uId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * 新增第二层评论
	 */
	public void addReplySecond(){
		String json = HttpKit.readData(getRequest());
		Integer hId = getParaToInt("hId");
		try{
			HotissueReply hotissueReply = FastJson.getJson().parse(json, HotissueReply.class);
			communityService.replySecond(hotissueReply,hId);
			renderSuccess();
		}catch(Exception ex){
			addOpLog("addReplySecond ===> json="+json+",hId="+hId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * 删除第二层评论
	 */
	public void deleteReplySecond(){
		Integer hId = getParaToInt("hId");
		Integer rId = getParaToInt("rId");
		try{
			communityService.deleteSecond(hId,rId);
			renderSuccess();
		}catch(Exception ex){
			addOpLog("deleteReplySecond ===> hId="+hId+",rId="+rId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 查询第二层评论
	 */
	public void querySecondReply(){
		Integer dId = getParaToInt("dId");
		Integer uId = getParaToInt("uId");
		try{
			renderSuccess("",communityService.querySecondReply(dId,uId));
		}catch(Exception ex){
			addOpLog("querySecondReply ===> dId="+dId+",uId="+uId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 点赞或取消
	 */
	public void likedOrNot(){
		Integer flag = getParaToInt("flag");
		Integer hId = getParaToInt("hId");
		Integer lId = getParaToInt("lId");
		Integer uId = getParaToInt("uId");
		try{
			communityService.isOrNotLiked(flag,hId,lId,uId);
			renderSuccess();
		}catch(Exception ex){
			addOpLog("likedOrNot ===> hId="+hId+",lId="+lId+",uId="+uId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 分页社区查询
	 */
	public void queryCommunityByPage(){
		Integer pageNo = getPager().getPage();
		Integer pageSize = getPager().getRows();
		Integer flag = getParaToInt("flag");
		String uPlot = getPara("uPlot");
		try{
			renderSuccess("",communityService.selectHotissue(pageNo,pageSize,flag,uPlot));
		}catch(Exception ex){
			addOpLog("queryCommunityByPage ===> json=");
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 关注
	 */
	public void queryConcern(){
		Integer uId = getParaToInt("uId");
		try{
			renderSuccess("",communityService.queryConcern(uId));
		}catch(Exception ex){
			addOpLog("queryConcern ===> uId="+uId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * App
	 * 关注或取消关注
	 */
	public void concernOrNot(){
		Integer flag = getParaToInt("flag");
		Integer uId = getParaToInt("uId");
		Integer rId = getParaToInt("rId");
		try{
			if(communityService.concernOrNot(flag,uId,rId)){
				renderSuccess();
			}else{
				renderFailed();
			}
		}catch(Exception ex){
			addOpLog("concernOrNot ===> flag="+flag + ",uId="+uId+",rId="+rId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 粉丝
	 */
	public void queryFans(){
		Integer uId = getParaToInt("uId");
		try{
			renderSuccess("",communityService.queryFans(uId));
		}catch(Exception ex){
			addOpLog("queryFans ===> uId="+uId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 喜欢的
	 */
	public void queryLiked(){
		Integer uId = getParaToInt("uId");
		try{
			renderSuccess("",communityService.queryLiked(uId));
		}catch(Exception ex){
			addOpLog("queryLiked ===> uId="+uId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 删除喜欢的热点
	 */
	public void deleteLiked(){
		Integer uId = getParaToInt("uId");
		Integer hId = getParaToInt("hId");
		try{
			if(communityService.deleteLiked(uId,hId)){
				renderSuccess();
			}else{
				renderFailed();
			}
		}catch(Exception ex){
			addOpLog("deleteLiked ===> uId="+uId+",hId="+hId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * App
	 * 查看个人信息
	 */
	public void queryPersonalInfo(){
		Integer rId = getParaToInt("rId");
		Integer uId = getParaToInt("uId");
		String uPlot = getPara("uPlot");
		try{
			renderSuccess("",communityService.queryPersonalInfo(uId,rId,uPlot));
		}catch(Exception ex){
			addOpLog("concernOrNot ===> rId="+rId + ",uId="+uId+",uPlot="+uPlot);
			ex.printStackTrace();
			renderFailed();
		}
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
