package com.cos.cmtgp.business.service;


import com.cos.cmtgp.business.model.*;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommunityService {

    /**
     * APP
     * 发布
     * @param hotissueBasic
     */
    public boolean insertHotissue(HotissueBasic hotissueBasic){
        hotissueBasic.setHDatetime(new Date());
        hotissueBasic.setHAppreciateNo(0);
        hotissueBasic.setHDiscussNo(0);
        hotissueBasic.setState(0);
        return hotissueBasic.save();
    }

    /**
     * 删除热点
     * @param hId
     * @return
     */
    public boolean deleteHotissue(Integer hId){
        if(Db.update("update t_hotissue_basic set state=1 where h_id="+hId)>0){
            return true;
        }
        return false;
    }


    /**
     * 新增第一层评论
     * @param hotissueDetail
     */
    @Before(Tx.class)
    public boolean replyFirst(HotissueDetail hotissueDetail){
        hotissueDetail.setDDatetime(new Date());
        if(hotissueDetail.save()){
            return this.discussNum("+1",hotissueDetail.getHId());
        }
        return false;
    }

    /**
     * 删除第一层评论
     * @param hId
     * @param dId
     */
    @Before(Tx.class)
    public boolean deleteFirst(Integer hId,Integer dId){
        if(Db.deleteById("t_hotissue_detail",dId)){
            return this.discussNum("-1",hId);
        }
        return false;
    }

    /**
     * APP
     * 查询第一层评论
     * @param hId
     * @return
     */
    public List<Record> queryFirstReply(Integer hId,Integer uId){
        String sql = " select d.*,count(e.id) as isLiked from ( " +
                " select a.d_id,a.h_id,a.u_id,b.u_avatar_url,b.u_nick_name,a.d_datetime,a.d_evaluate_context,count(c.id) as likedNum " +
                ",case when f.u_id=a.u_id then 1 else 0 end as isAuthor " +
                ",case when a.u_id=" + uId + " then 1 else 0 end as canDelete " +
                " from t_hotissue_detail a " +
                " left join t_hotissue_basic f on f.h_id=a.h_id " +
                " left join t_user_setting b on a.u_id=b.u_id " +
                " left join t_hotissue_liked c on a.d_id=c.l_id where a.h_id="+hId + " group by a.d_id) d " +
                " left join t_hotissue_liked e on d.d_id=e.l_id and e.u_id="+uId +
                " group by d.d_id order by d.d_datetime desc,d.likedNum desc";
        return Db.find(sql);
    }


    /**
     * 新增第二层评论
     * @param hotissueReply
     * @param hId
     */
    @Before(Tx.class)
    public boolean replySecond(HotissueReply hotissueReply,Integer hId){
        hotissueReply.setRDatetime(new Date());
        if(hotissueReply.save()){
            return this.discussNum("+1",hId);
        }
        return false;
    }

    /**
     * 删除第二层评论
     * @param hId
     * @param rId
     */
    @Before(Tx.class)
    public boolean deleteSecond(Integer hId,Integer rId){
        if(Db.deleteById("t_hotissue_reply", rId)){
            return this.discussNum("-1",hId);
        }
        return false;
    }

    /**
     * APP
     * 查询第二次评论
     * @param dId
     * @return
     */
    public List<Record> querySecondReply(Integer dId,Integer uId){
        String sql = " select e.*,count(f.id) as isLiked from ( " +
                " select a.r_id,a.d_id,b.u_avatar_url as beforeUrl,b.u_nick_name as beforeName " +
                " ,case when a.r_reply_id=i.u_id then 1 else 0 end as beforeIsAuthor,c.u_nick_name as afterName " +
                " ,case when a.u_id=i.u_id then 1 else 0 end as afterIsAuthor,a.r_content,a.r_datetime,count(d.id) as likedNum " +
                " ,case when a.r_reply_id=" + uId + " then 1 else 0 end as canDelete" +
                " from t_hotissue_reply a " +
                " left join t_hotissue_detail g on g.d_id=a.d_id " +
                " left join t_hotissue_basic i on g.h_id=i.h_id " +
                " left join t_user_setting b on a.r_reply_id=b.u_id " +
                " left join t_user_setting c on c.u_id=a.u_id " +
                " left join t_hotissue_liked d on a.r_id=d.l_id " +
                " where a.d_id = "+dId+" group by a.r_id)e " +
                " left join t_hotissue_liked f on e.r_id=f.l_id and f.u_id= "+ uId +
                " group by e.r_id order by e.r_datetime asc";
        return Db.find(sql);
    }

    private boolean discussNum(String value,Integer hId){
        if(!"".equals(value)){
            if(Db.update("update t_hotissue_basic set h_discuss_no = h_discuss_no" + value + " where h_id=" + hId)>0){
                return true;
            }
        }
        return false;
    }

    /**
     * 点赞或取消赞
     * @param flag
     * @param hId
     * @param lId
     * @param uId
     */
    @Before(Tx.class)
    public boolean isOrNotLiked(Integer flag,Integer hId,Integer lId,Integer uId){
        if(flag==0){
            if(Db.delete("delete from t_hotissue_liked where l_id=" + lId + " and u_id="+uId)<1){
                return false;
            }
        }else{
            HotissueLiked hotissueLiked = new HotissueLiked();
            hotissueLiked.setLId(lId);
            hotissueLiked.setUId(uId);
            if(!hotissueLiked.save()){
                return false;
            }
        }
        String value = flag==0?"-1":"+1";
        if(Db.update("update t_hotissue_basic set h_appreciate_no = h_appreciate_no"+value+" where h_id="+hId)>0){
            if(Db.update("update t_user_setting set u_liked=u_liked"+value+" where u_id="+uId)>0){
                return true;
            }
        }
        return false;
    }

    /**
     * APP
     * 分页社区查询
     * @param pageNo
     * @param pageSize
     * @param flag
     * @param uPlot
     * @return
     */
    public Page<Record> selectHotissue(Integer pageNo,Integer pageSize,Integer flag,String uPlot){
        StringBuffer sql = new StringBuffer(" from t_hotissue_basic where state=0 ");
        if(flag!=0){
            //所在社区
            sql.append(" and u_plot='"+uPlot+"'");
        }
        sql.append(" order by h_datetime desc,h_appreciate_no desc,h_discuss_no desc ");
        return Db.paginate(pageNo, pageSize, "select h_id,h_context,h_discuss_no,h_appreciate_no,SUBSTRING_INDEX(h_address_img,'~',1) as coverUrl,h_address_img,h_address_video ", sql.toString());
    }


    /**
     * APP
     * 查询关注
     * @param uId
     * @return
     */
    public List<Record> queryConcern(Integer uId){
        String sql = " select a.u_id,b.u_nick_name,b.u_avatar_url ,case when c.u_id is not null then 1 else 0 end as concern " +
                " from t_user_relation a  " +
                " left join t_user_setting b on a.u_rid=b.u_id " +
                " left join t_user_relation c on c.u_rid=a.u_id and a.u_rid=c.u_id " +
                " where a.u_id="+uId + " order by concern desc ";
        return Db.find(sql);
    }

    /**
     * APP
     * 关注或者取消关注
     * @param flag
     * @param uId
     * @param rId
     * @return
     */
    @Before(Tx.class)
    public boolean concernOrNot(Integer flag,Integer uId,Integer rId){
        if(flag==0){
            if(Db.update("delete from t_user_relation where u_id="+uId+" and u_rid="+rId)<1){
                return false;
            }
        }else{
            UserRelation relation = new UserRelation();
            relation.setUId(uId);
            relation.setURid(rId);
            if(!relation.save()){
                return false;
            }
        }
        if(Db.update("update t_user_setting set u_concern=u_concern"+(flag==0?"-1":"+1")+" where u_id="+uId)>0){
            if(Db.update("update t_user_setting set u_fans=u_fans"+(flag==0?"-1":"+1")+" where u_id="+rId)>0){
                return true;
            }
        }
        return false;
    }

    /**
     * APP
     * 查询粉丝
     * @param uId
     * @return
     */
    public List<Record> queryFans(Integer uId){
        String sql = " select b.u_id,b.u_nick_name,b.u_avatar_url,case when c.u_id is not null then 1 else 0 end as fans " +
                " from t_user_relation a " +
                " left join t_user_setting b on a.u_id=b.u_id " +
                " left join t_user_relation c on a.u_id=c.u_rid and a.u_rid=c.u_id " +
                " where a.u_rid="+uId+" order by fans desc";
        return Db.find(sql);
    }

    /**
     * APP
     * 查询喜欢的热点
     * @param uId
     * @return
     */
    public List<Record> queryLiked(Integer uId){
        String sql = " select a.h_id,SUBSTRING_INDEX(a.h_address_img,'~',1) as coverUrl,a.h_address_img,a.h_address_video,a.h_context,a.state " +
                " from t_hotissue_basic a ,t_hotissue_liked b " +
                " where a.h_id=b.l_id and b.u_id="+uId;
        return Db.find(sql);
    }

    /**
     * APP
     * 删除喜欢的热点
     * @param uId
     * @param hId
     * @return
     */
    public boolean deleteLiked(Integer uId,Integer hId){
        if(Db.update(" delete from t_hotissue_liked where l_id="+hId+" and u_id="+uId)>0){
            return true;
        }
        return false;
    }

    /**
     * APP
     * 查看个人信息
     * @param uId
     */
    public Record queryPersonalInfo(Integer uId,Integer loginId,String uPlot){
        String sql = " select a.u_avatar_url,a.u_nick_name,a.u_content " +
                " ,case when b.u_id is not null then 1 else 0 end as concern " +
                " ,case when c.u_id is not null then 1 else 0 end as fans " +
                " from t_user_setting a " +
                " left join t_user_relation b on b.u_id="+loginId+" and b.u_rid=a.u_id " +
                " left join t_user_relation c on c.u_rid="+loginId+" and c.u_id=a.u_id " +
                " where a.u_id="+uId;
        Record info = Db.find(sql).get(0);
            String hotissue = " select a.h_id,a.h_discuss_no,a.h_appreciate_no,SUBSTRING_INDEX(a.h_address_img,'~',1) as coverUrl,a.h_context,a.h_address_img,a.h_address_video" +
                " from t_hotissue_basic a " +
                " where a.u_id="+uId+" and (a.u_plot is null or a.u_plot='"+uPlot+"')";
        List<Record> records = Db.find(hotissue);
        Record result = new Record();
        result.set("info",info);
        result.set("hotissueList",records);
        return result;
    }
}
