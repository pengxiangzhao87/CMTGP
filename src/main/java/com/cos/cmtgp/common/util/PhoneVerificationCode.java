package com.cos.cmtgp.common.util;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.cos.cmtgp.business.model.GlobalConf;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author pengxiangZhao
 * @Description: TODO
 * @ClassName: PhoneVerificationCode
 * @Date 2020/5/26 0026
 */
public class PhoneVerificationCode {

    public static String sendAuthCode(String phone,String code) throws Exception{
        //初始化acsClient,<accessKeyId>和"<accessSecret>"在短信控制台查询即可。
        List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type=5");
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", confList.get(0).getCAppid(), confList.get(0).getApiSecuret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        //域名，请勿修改
        request.setSysDomain("dysmsapi.aliyuncs.com");
        //API版本号，请勿修改
        request.setSysVersion("2017-05-25");
        //API名称
        request.setSysAction("SendSms");
        //接收号码，格式为：国际码+号码，必填
        request.putQueryParameter("PhoneNumbers", "86"+phone);
        request.putQueryParameter("SignName", "食朝夕");
        request.putQueryParameter("TemplateCode", "SMS_190794914");
        request.putQueryParameter("TemplateParam", "{\"code\":\""+code+"\"}");
//        request.putQueryParameter("SmsUpExtendCode", "12345");
        CommonResponse response = client.getCommonResponse(request);
        //{"Message":"OK","RequestId":"F9553693-7FFF-4C48-8658-3A42A76D228D","BizId":"366107990573586823^0","Code":"OK"}
        //{"Message":"签名不合法(不存在或被拉黑)","RequestId":"04C64507-910E-4971-89AF-671CA726C045","Code":"isv.SMS_SIGNATURE_ILLEGAL"}
        return response.getData();

    }

    public static String sendMini(String phone,String product,Integer type) throws Exception{
        //初始化acsClient,<accessKeyId>和"<accessSecret>"在短信控制台查询即可。
        List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type=5");
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", confList.get(0).getCAppid(), confList.get(0).getApiSecuret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        //域名，请勿修改
        request.setSysDomain("dysmsapi.aliyuncs.com");
        //API版本号，请勿修改
        request.setSysVersion("2017-05-25");
        //API名称
        request.setSysAction("SendSms");
        //接收号码，格式为：国际码+号码，必填
        request.putQueryParameter("PhoneNumbers", "86"+phone);
        request.putQueryParameter("SignName", "食朝夕");
        //1：新订单，2：退款：3：二次支付
        if(type==1){
            request.putQueryParameter("TemplateCode", "SMS_202821703");
        }else if(type==2){
            request.putQueryParameter("TemplateCode", "SMS_202821707");
        }else{
            request.putQueryParameter("TemplateCode", "SMS_202816644");
        }
        request.putQueryParameter("TemplateParam", "{\"product\":\""+product+"\"}");
//        request.putQueryParameter("SmsUpExtendCode", "12345");
        CommonResponse response = client.getCommonResponse(request);
        //{"Message":"OK","RequestId":"F9553693-7FFF-4C48-8658-3A42A76D228D","BizId":"366107990573586823^0","Code":"OK"}
        //{"Message":"签名不合法(不存在或被拉黑)","RequestId":"04C64507-910E-4971-89AF-671CA726C045","Code":"isv.SMS_SIGNATURE_ILLEGAL"}
        return response.getData();

    }



//    public static void main(String[] args) {
//        //初始化acsClient,<accessKeyId>和"<accessSecret>"在短信控制台查询即可。
//        //LTAI4GF9FfUZkBrrx9Qf4cVk
//        //jUw3iB924bCCebGrIIWGEs4pJLf2Hl
//        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", PhoneVerificationCode.ACCESSKEYID, PhoneVerificationCode.ACCESSSECRET);
//        IAcsClient client = new DefaultAcsClient(profile);
//        CommonRequest request = new CommonRequest();
//        request.setSysMethod(MethodType.POST);
//        //域名，请勿修改
//        request.setSysDomain("dysmsapi.aliyuncs.com");
//        //API版本号，请勿修改
//        request.setSysVersion("2017-05-25");
//        //API名称
//        request.setSysAction("SendSms");
//        //接收号码，格式为：国际码+号码，必填
//        request.putQueryParameter("PhoneNumbers", "8618614061833");
//        request.putQueryParameter("SignName", "食朝夕");
//        request.putQueryParameter("TemplateCode", "SMS_190794914");
//        request.putQueryParameter("TemplateParam", "{\"code\":\"1234\"}");
////        request.putQueryParameter("SmsUpExtendCode", "12345");
//        try {
//            CommonResponse response = client.getCommonResponse(request);
//            System.out.println(response.getData());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
