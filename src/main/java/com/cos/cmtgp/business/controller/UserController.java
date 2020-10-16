package com.cos.cmtgp.business.controller;


import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import com.alibaba.fastjson.JSONObject;
import com.cos.cmtgp.business.model.AddressInfo;
import com.cos.cmtgp.business.model.FeedbackInfo;
import com.cos.cmtgp.business.model.UserSetting;
import com.cos.cmtgp.business.service.UserService;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.*;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;

import com.jfinal.upload.UploadFile;

/**
 *	  用户接口
 */
public class UserController extends BaseController {
	UserService userService = enhance(UserService.class);


	public void feedBack(){
		String content = getPara("content");
		Integer uId = getParaToInt("uId");
		FeedbackInfo feedbackInfo = new FeedbackInfo();
		feedbackInfo.setContent(content);
		feedbackInfo.setUId(uId);
		feedbackInfo.setCreateTime(new Date());
		feedbackInfo.save();
		renderSuccess();
	}


	/**
	 * 微信小程序登录
	 * 手机号登录
	 */
	public void miniLogin() {
		String phone = getPara("phone");
		String macCode = getPara("macCode");
		String verifyCode = getPara("verifyCode")==null?"":getPara("verifyCode");
		try{
			if(!validPhoneNumber(phone)){
				renderFailed("请输入正确手机号码");
			}else{
				if("".equals(verifyCode)){
					String sql = "select * from t_user_setting where u_phone ='"+phone+"' and u_logout=1";
					UserSetting userSetting = UserSetting.dao.findFirst(sql);
					if(userSetting!=null){
						if(macCode.equals(userSetting.getUCode())){
							userSetting.setULastTime(new Date());
							renderSuccess();
						}else{
							renderFailed("与上次登录地址不一致，请获取验证码重新登录");
						}
					}else{
						renderFailed("请重新登录");
					}
				}else{
					String phoneCode = getSessionAttr(phone) == null ? "" : getSessionAttr(phone).toString();
					if (!"".equals(phoneCode)) {
						Map<String, Object> jsonMap = (Map) JSONObject.parse(phoneCode);
						long time = Long.parseLong(jsonMap.get("time").toString());
						long now = System.currentTimeMillis();
						if (now - time > 5 * 60 * 1000) {
							removeSessionAttr(phone);
							renderFailed("验证码失效");
						} else if(!verifyCode.equals(jsonMap.get("verifyCode").toString())){
							renderFailed("验证码有误");
						}else{
							String sql = "select * from t_user_setting where u_phone ='"+phone+"' ";
							UserSetting userSetting = UserSetting.dao.findFirst(sql);
							if(userSetting==null){
								userSetting = new UserSetting();
								userSetting.setUPhone(phone);
								userSetting.setUNickName("请修改昵称");
								userSetting.setUAvatarUrl("userAvatar.jpg");
								userSetting.setUCode(macCode);
								userSetting.setUConcern(0);
								userSetting.setUFans(0);
								userSetting.setULiked(0);
								userSetting.setULogout(1);
								userSetting.setULastTime(new Date());
								if(userSetting.save()){
									removeSessionAttr(phone);
									renderSuccess();
								}else{
									renderFailed();
								}
							}else{
								userSetting.setULastTime(new Date());
								userSetting.setUCode(macCode);
								userSetting.setULogout(1);
								userSetting.setULastTime(new Date());
								if(userSetting.update()){
									removeSessionAttr(phone);
									renderSuccess();
								}else{
									renderFailed();
								}
							}
						}
					} else {
						renderFailed("验证码失效");
					}
				}
			}
		}catch(Exception ex){
			addOpLog("login ===>phone="+phone+",macCode="+macCode+",verifyCode="+verifyCode);
			ex.printStackTrace();
			renderFailed();
		}
	}


	/**
	 * APP
	 * 我的
	 */
	public void queryMyInfo(){
		Integer userId = getParaToInt("uId");
		try{
			renderSuccess("",userService.getMyInfo(userId));
		}catch(Exception ex){
			addOpLog("queryCategoryList ===> userId="+userId);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 查询收货地址
	 */
	public void queryAddressList(){
		Integer uId = getParaToInt("userId");
		Integer isUsed = getParaToInt("isUsed");
		try{
			renderSuccess("",userService.selectAddressList(uId,isUsed));
		}catch(Exception ex){
			addOpLog("queryAddressList ===> uId="+uId+",isUsed="+isUsed);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * APP
	 * 更新默认地址
	 */
	public void checkAddress(){
		Integer uId = getParaToInt("uId");
		Integer aId = getParaToInt("aId");
		try{
			Db.update("update t_address_info set is_used=0 where u_id="+uId);
			Db.update("update t_address_info set is_used=1 where a_id="+aId);
			renderSuccess();
		}catch(Exception ex){
			addOpLog("checkAddress ===>uId="+uId+",aId="+aId);
			ex.printStackTrace();
			renderFailed();
		}

	}

	/**
	 * APP
	 * 地址新增修改
	 */
	public void saveAddress(){
		String json = HttpKit.readData(getRequest());
		try{
			AddressInfo address = FastJson.getJson().parse(json, AddressInfo.class);
			if(address.getAId()!=null){
				if(address.update()){
					renderSuccess();
				}else{
					renderFailed();
				}
			}else{
				Db.update("update t_address_info set is_used=0 where u_id="+address.getUId());
				if(address.save()){
					renderSuccess();
				}else{
					renderFailed();
				}
			}
		}catch(Exception ex){
			addOpLog("saveAddress ===>json="+json);
			ex.printStackTrace();
			renderFailed();
		}
	}

	public void deleAddress(){
		Integer aId = getParaToInt("aId");
		Db.update("delete from t_address_info where a_id="+aId);
		renderSuccess();
	}

	/**
	 * APP
	 * 查询余额
	 */
	public void queryAccount(){
		Integer uId = getParaToInt("uId");
		try{
			UserSetting userSetting = UserSetting.dao.findById(uId);
			if(userSetting!=null){
				renderSuccess("",userSetting.getAccountPrice());
			}else{
				renderFailed();
			}
		}catch(Exception ex){
			addOpLog("queryAccount ===>uId="+uId);
			ex.printStackTrace();
			renderFailed();
		}
	}


	/**
	 * APP
	 * 手机号登录
	 */
	public void login() {
		String phone = getPara("phone");
		String macCode = getPara("macCode");
		String verifyCode = getPara("verifyCode")==null?"":getPara("verifyCode");
		try{
			if(!validPhoneNumber(phone)){
				renderFailed("请输入正确手机号码");
			}else{
				if("".equals(verifyCode)){
					String sql = "select * from t_user_setting where u_phone ='"+phone+"' and u_logout=1";
					UserSetting userSetting = UserSetting.dao.findFirst(sql);
					if(userSetting!=null){
						if(macCode.equals(userSetting.getUCode())){
							userSetting.setULastTime(new Date());
							renderSuccess("",userSetting.getUId());
						}else{
							renderFailed("与上次登录地址不一致，请获取验证码重新登录");
						}
					}else{
						renderFailed("请重新登录");
					}
				}else{
					String phoneCode = getSessionAttr(phone) == null ? "" : getSessionAttr(phone).toString();
					if (!"".equals(phoneCode)) {
						Map<String, Object> jsonMap = (Map) JSONObject.parse(phoneCode);
						long time = Long.parseLong(jsonMap.get("time").toString());
						long now = System.currentTimeMillis();
						if (now - time > 30 * 60 * 1000) {
							removeSessionAttr(phone);
							renderFailed("验证码失效");
						} else if(!verifyCode.equals(jsonMap.get("verifyCode").toString())){
							renderFailed("验证码有误");
						}else{
							String sql = "select * from t_user_setting where u_phone ='"+phone+"' ";
							UserSetting userSetting = UserSetting.dao.findFirst(sql);
							if(userSetting==null){
								userSetting = new UserSetting();
								userSetting.setUPhone(phone);
								userSetting.setUNickName("请修改昵称");
								userSetting.setUAvatarUrl("userAvatar.jpg");
								userSetting.setUCode(macCode);
								userSetting.setUConcern(0);
								userSetting.setUFans(0);
								userSetting.setULiked(0);
								userSetting.setULogout(1);
								userSetting.setULastTime(new Date());
								if(userSetting.save()){
									removeSessionAttr(phone);
									renderSuccess("",userSetting.getUId());
								}else{
									renderFailed();
								}
							}else{
								userSetting.setULastTime(new Date());
								userSetting.setUCode(macCode);
								userSetting.setULogout(1);
								userSetting.setULastTime(new Date());
								if(userSetting.update()){
									removeSessionAttr(phone);
									renderSuccess("",userSetting.getUId());
								}else{
									renderFailed();
								}
							}
						}
					} else {
						renderFailed("验证码失效");
					}
				}
			}
		}catch(Exception ex){
			addOpLog("login ===>phone="+phone+",macCode="+macCode+",verifyCode="+verifyCode);
			ex.printStackTrace();
			renderFailed();
		}
	}




	/**
	 * APP
	 * 发送验证码
	 */
	public void sendPhoneVerificationCode(){
		String phone = getPara("phone").trim();
		String verifyCode = String.valueOf(new Random().nextInt(899999) + 100000);//生成短信验证码
		try{
			if(!validPhoneNumber(phone)){
				renderFailed("请输入正确手机号码");
			}else{
				String result = PhoneVerificationCode.sendAuthCode(phone, verifyCode);
//			String result = "{\"Message\":\"OK\",\"RequestId\":\"F9553693-7FFF-4C48-8658-3A42A76D228D\",\"BizId\":\"366107990573586823^0\",\"Code\":\"OK\"}";
				Map<String,String> parse = FastJson.getJson().parse(result, Map.class);
				if("OK".equals(parse.get("Code"))){
					JSONObject json = new JSONObject();
					json.put("verifyCode",verifyCode);
					json.put("time",System.currentTimeMillis());
					setSessionAttr(phone,json.toString());
					renderSuccess(verifyCode);
				}else{
					renderFailed("发送验证码失败");
				}
			}
		}catch(Exception ex){
			addOpLog("sendPhoneVerificationCode ===>phone="+phone+",verifyCode="+verifyCode);
			ex.printStackTrace();
			renderFailed();
		}
	}

	private static boolean validPhoneNumber(String phoneNumber){
		String pattern = "^(((\\+\\d{2}-)?0\\d{2,3}-\\d{7,8})|((\\+\\d{2}-)?(\\d{2,3}-)?([1][3,4,5,7,8][0-9]\\d{8})))$";
		return Pattern.matches(pattern, phoneNumber);
	}



    /**
     * APP
     * 账号退出
     */
    public void logOut(){
        String phone = getPara("phone");
        try{
            if(Db.update("update t_user_setting set u_logout=0 where u_phone="+phone)>0){
                renderSuccess();
            }else{
                renderFailed();
            }
        }catch(Exception ex){
            addOpLog("logOut ===>phone="+phone);
            ex.printStackTrace();
            renderFailed();
        }
    }



    /**
     * APP
     * 上传头像
     */
    public void uploadAvatar(){
		UploadFile file = getFile();
		Integer uId = getParaToInt("uId");
		String name = uId+"_"+System.currentTimeMillis()+"_avatar.png";
		String path = PathKit.getWebRootPath()+"/upload/"+name;
		file.getFile().renameTo(new File(path));
        try{
			if(file==null){
				renderFailed();
			}else{
				UserSetting setting = UserSetting.dao.findById(uId);
				if(!setting.getUAvatarUrl().equals("userAvatar.jpg")){
					new File(PathKit.getWebRootPath()+"/upload/"+setting.getUAvatarUrl()).delete();
				}
				setting.setUAvatarUrl(name);
				if(setting==null || setting.getUId()==null){
					renderFailed();
				}else{
					if(setting.update()){
						renderSuccess("",name);
					}else{
						renderFailed();
					}
				}
			}
        }catch(Exception ex){
            addOpLog("uploadAvatar ===>uId="+uId);
            ex.printStackTrace();
            renderFailed();
        }
    }

	/**
	 * APP
	 * 修改用户信息
	 */
	public void updateUserSetting(){
		String json = HttpKit.readData(getRequest());
		try{
			UserSetting userSetting = FastJson.getJson().parse(json, UserSetting.class);
			if(userSetting==null || userSetting.getUId()==null){
				renderFailed();
			}else{
				UserSetting setting = UserSetting.dao.findById(userSetting.getUId());
				setting.setUNickName(userSetting.getUNickName());
				setting.setUPlot(userSetting.getUPlot());
				setting.setUContent(userSetting.getUContent());
				if(setting.update()){
					renderSuccess();
				}else{
					renderFailed();
				}
			}
		}catch(Exception ex){
			addOpLog("updateUserSetting ===>json="+json);
			ex.printStackTrace();
			renderFailed();
		}
	}

	/**
	 * 添加用户信息
	 */
	public void addUser() {
		if(getModel(UserSetting.class, "model").save()) {
			addOpLog("[用户管理] 添加");
			renderSuccess("用户添加成功");
		}else {
			renderFailed("用户添加失败");
		}
	}
	
	/**
	 * 修改用户信息
	 */
	public void updateUser() {
		if(getModel(UserSetting.class, "model").update()) {
			addOpLog("[用户管理] 修改");
			renderSuccess("用户修改成功");
		}else {
			renderFailed("用户修改失败");
		}
	}
	
	/**
	 * 用户信息查询页面
	 */
	public void listPage() {
		render("list.html");
	}
	
	/**
	 * 用户信息查询
	 */
	public void getUserAll() {
		Map<String, String[]> paraMap = getParaMap();
		String orderBy = getOrderBy();
		int pageNum = getPager().getPage();
		int pageSize = getPager().getRows();
		renderJson(userService.getUserAll(pageSize,pageNum,paraMap,orderBy));
	}

}
