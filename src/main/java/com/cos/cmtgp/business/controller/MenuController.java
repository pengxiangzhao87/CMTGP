package com.cos.cmtgp.business.controller;

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


}
