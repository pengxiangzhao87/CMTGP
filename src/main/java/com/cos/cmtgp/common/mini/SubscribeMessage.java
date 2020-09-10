package com.cos.cmtgp.common.mini;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cos.cmtgp.business.dto.MiniTempDTO;
import com.cos.cmtgp.business.dto.MiniTempDataDTO;
import com.cos.cmtgp.business.model.GlobalConf;
import com.cos.cmtgp.common.util.MiniUtil;
import com.cos.cmtgp.common.util.UrlUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author pengxiangZhao
 * @Description: TODO
 * @ClassName: SubscribeMessage
 * @Date 2020/7/27 0027
 */
public class SubscribeMessage extends Thread {
    Logger logger = LogManager.getLogger(getClass());
    private Integer oId;
    private MiniTempDataDTO dataDTO;
    private String tempName;
    private Integer type;
    private String openid;

    public SubscribeMessage(Integer oId,MiniTempDataDTO dataDTO,String tempName,Integer type,String openid) {
        this.oId = oId;
        this.dataDTO = dataDTO;
        this.tempName = tempName;
        this.type = type;
        this.openid = openid;
    }

    @Override
    public void run() {
        Map<String, Object> accessToken = this.getAccessToken(type);
        MiniTempDTO miniTempDTO = new MiniTempDTO();
        miniTempDTO.setTemplate_id(tempName);
        if(type==3){
            miniTempDTO.setPage("/pages/order/detail/detail?oid=" + oId);
        }else{
            miniTempDTO.setPage("/pages/index/detail/detail?oid=" + oId);
        }
        miniTempDTO.setData(dataDTO);
        miniTempDTO.setMiniprogram_state("developer");
        miniTempDTO.setTouser(openid);
        String data = JSONObject.toJSONString(miniTempDTO);
        String url ="https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token="+accessToken.get("access_token");
        String result = null;
        try {
            result = UrlUtil.sendPost(url,data,false);
        } catch (Exception e) {

            e.printStackTrace();
        }
        Map resultMap = (Map)JSON.parse(result);
        if((Integer)resultMap.get("errcode")!=0){
            logger.error("模板：===============>"+result);
        }
    }

    /**
     * 获取token
     * @return
     */
    private Map<String,Object> getAccessToken(Integer type){
        List<GlobalConf> confList = GlobalConf.dao.find("select * from t_global_conf where c_type="+type);
        String cAppid = confList.get(0).getCAppid();
        String cSecret = confList.get(0).getCSecret();
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+cAppid+"&secret="+cSecret;
        String result = UrlUtil.getAsText(url);
        Map<String,Object> parse = (Map<String,Object>) JSON.parse(result);
        return parse;

    }
}
