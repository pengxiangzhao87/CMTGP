package com.cos.cmtgp.business.controller;

import com.cos.cmtgp.business.model.FoodInfo;
import com.cos.cmtgp.business.model.MenuInfo;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.util.List;

/**
 * @author pengxiangZhao
 * @Description: TODO
 * @ClassName: MenuController
 * @Date 2020/12/4 0004
 */
public class MenuController extends BaseController {

    public void queryMenuList(){
        Integer pageNo = getPager().getPage();
        Integer pageSize = getPager().getRows();
        Integer category1Id = getParaToInt("category1Id");
        Integer category2Id = getParaToInt("category2Id");
        String menuName = getPara("menuName");
        try{
            StringBuffer select = new StringBuffer(" select a.m_name,a.m_id,a.m_img_adr,a.m_cook_time,a.m_video_adr,a.m_cook_price,a.is_order ");
            StringBuffer from = new StringBuffer(" from t_menu_info a ");
            if(StringUtil.isNotEmpty(menuName)){
                from.append(" where a.m_name like '"+menuName+"' ");
            }
            if(category1Id!=null){
                select.append(" and a.category1_id= "+category1Id);
            }
            if(category2Id!=null){
                select.append(" and a.category2_id= "+category2Id);
            }
            Page<Record> paginate = Db.paginate(pageNo, pageSize, select.toString(), from.toString());
            renderSuccess("",paginate);
        }catch(Exception ex){
            ex.printStackTrace();
            renderFailed();
        }
    }

    public void queryFoodList(){
        Integer pageNo = getPager().getPage();
        Integer pageSize = getPager().getRows();
        Integer category1Id = getParaToInt("category1Id");
        Integer category2Id = getParaToInt("category2Id");
        String foodName = getPara("foodName");
        try{
            StringBuffer select = new StringBuffer(" select a.* ");
            StringBuffer from = new StringBuffer(" from t_food_info a ");
            if(StringUtil.isNotEmpty(foodName)){
                from.append(" where a.f_name like '"+foodName+"' ");
            }
            Page<Record> paginate = Db.paginate(pageNo, pageSize, select.toString(), from.toString());
            renderSuccess("",paginate);
        }catch(Exception ex){
            ex.printStackTrace();
            renderFailed();
        }
    }

    public void queryHomePageCategory() {
        List<Record> recordList = Db.find(" select * from t_category1_info ");
        renderSuccess("",recordList);
    }

    public void queryMenuDetail(){
        Integer menuId = getParaToInt("menuId");
        MenuInfo menuInfo = MenuInfo.dao.findById(menuId);
        renderSuccess("",menuInfo);
    }

    public void queryMenuFreeFood(){
        Integer menuId = getParaToInt("menuId");
        StringBuffer sb = new StringBuffer(" select c.f_id,c.f_name,c.f_img_adr,c.f_type,c.f_price,c.f_unit,c.f_init_number ");
        sb.append(" from t_menu_info a,t_menu_option b,t_food_info c ");
        sb.append(" where a.m_id=b.menu_id and b.food_id=c.f_id and b.is_free=1 and a.m_id= "+menuId);
        List<Record> recordList = Db.find(sb.toString());
        renderSuccess("",recordList);
    }

    public void queryMenuOption(){
        Integer menuId = getParaToInt("menuId");
        Integer foodId = getParaToInt("foodId");
        StringBuffer sb = new StringBuffer(" select a.menu_id,a.m_group,a.m_number,b.f_id,f_name,b.f_type,b.f_price,b.f_unit,b.f_img_adr" +
                ",round(case f_type when 0 then f_price*m_number else m_number/50*f_price end ,2) as totalPrice ");
        sb.append(" from t_menu_option a,t_food_info b ");
        sb.append(" where a.food_id=b.f_id and a.menu_id="+menuId);
        if(foodId!=null){
            sb.append(" and a.food_id="+foodId);
        }else{
            sb.append(" and a.m_init=1 ");
        }
        List<Record> recordList = Db.find(sb.toString());
        renderSuccess("",recordList);
    }

    public void queryOtherOption(){
        Integer menuId = getParaToInt("menuId");
        Integer group = getParaToInt("group");
        StringBuffer sb = new StringBuffer(" select a.m_number,b.f_id,f_name,b.f_type,b.f_price,b.f_unit,b.f_img_adr" +
                ",round(case f_type when 0 then f_price*m_number else m_number/50*f_price end ,2) as totalPrice ");
        sb.append(" from t_menu_option a,t_food_info b ");
        sb.append(" where a.food_id=b.f_id and a.is_free=0 ");
        sb.append(" and a.m_group="+group);
        sb.append(" and a.menu_id="+menuId);
        List<Record> recordList = Db.find(sb.toString());
        renderSuccess("",recordList);
    }

    public void queryFoodDetail(){
        Integer foodId = getParaToInt("foodId");
        FoodInfo foodInfo = FoodInfo.dao.findById(foodId);
        renderSuccess("",foodInfo);
    }

    public void goodAndMenu(){
        Integer foodId = getParaToInt("foodId");
        StringBuffer sb = new StringBuffer("select a.m_id,a.m_name,SUBSTRING_INDEX(a.m_img_adr,'~',1) as m_img_adr,a.m_cook_time,a.m_cook_price ");
        sb.append(" from t_menu_info a ,t_menu_option b");
        sb.append(" where a.m_id=b.menu_id and b.food_id= "+foodId);
        sb.append(" limit 0,8 ");
        List<Record> recordList = Db.find(sb.toString());
        renderSuccess("",recordList);
    }


}
