package com.cos.cmtgp.business.controller;

import com.alibaba.fastjson.JSON;
import com.cos.cmtgp.business.model.CartInfo;
import com.cos.cmtgp.business.model.CartMenu;
import com.cos.cmtgp.business.model.FoodInfo;
import com.cos.cmtgp.business.model.MenuInfo;
import com.cos.cmtgp.common.base.BaseController;
import com.cos.cmtgp.common.util.DateUtil;
import com.cos.cmtgp.common.util.StringUtil;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.math.BigDecimal;
import java.util.*;

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
        Integer foodId = getParaToInt("foodId");
        Integer uId = getParaToInt("uId");
        try{
            StringBuffer select = new StringBuffer(" select a.m_name,a.m_id,SUBSTRING_INDEX(a.m_img_adr,'~',1) as m_img_adr,a.m_cook_time,a.m_video_adr,a.m_cook_price,a.is_order,case when c.c_id is null then 0 else 1 end as isCar ");
            StringBuffer from = new StringBuffer(" from t_menu_info a ");
            from.append(" left join t_cart_info c on a.m_id=c.menu_id "+ (uId==null?"": " and c.user_id="+uId));
            if(foodId!=null){
                from.append(" inner join t_menu_option b on a.m_id=b.menu_id ");
            }
            from.append(" where 1=1 ");
            if(StringUtil.isNotEmpty(menuName)){
                from.append(" and a.m_name like '%"+menuName+"%' ");
            }
            if(category1Id!=null){
                from.append(" and a.category1_id= "+category1Id);
            }
            if(category2Id!=null){
                from.append(" and a.category2_id= "+category2Id);
            }
            if(foodId!=null){
                from.append(" and b.food_id= "+foodId);
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
        Integer uId = getParaToInt("uId");
        try{
            StringBuffer select = new StringBuffer(" select round(case a.f_type when 0 then a.f_price*a.f_init_number else a.f_init_number/50*a.f_price end ,2) as totalPrice,a.*,case when b.c_id is null then 0 else 1 end as isCar ");
            StringBuffer from = new StringBuffer(" from t_food_info a  ");
            from.append(" left join t_cart_info b on a.f_id=b.food_id "+ (uId==null?"":" and b.user_id="+uId));
            from.append(" where 1=1 ");
            if(StringUtil.isNotEmpty(foodName)){
                from.append(" and a.f_name like '%"+foodName+"%' ");
            }
            if(category1Id!=null){
                from.append(" and a.category1_id="+category1Id);
            }
            if(category2Id!=null){
                from.append(" and a.category2_id="+category2Id);
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
                ",round(case b.f_type when 0 then b.f_price*a.m_number else a.m_number/50*b.f_price end ,2) as totalPrice ");
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
                ",round(case b.f_type when 0 then b.f_price*a.m_number else a.m_number/50*f_price end ,2) as totalPrice ");
        sb.append(" from t_menu_option a,t_food_info b ");
        sb.append(" where a.food_id=b.f_id and a.is_free=0 ");
        sb.append(" and a.m_group="+group);
        sb.append(" and a.menu_id="+menuId);
        List<Record> recordList = Db.find(sb.toString());
        renderSuccess("",recordList);
    }

    public void queryFoodDetail(){
        Integer foodId = getParaToInt("foodId");
        String sql = "select case a.f_type when 0 then a.f_init_number*a.f_price else a.f_init_number/50*a.f_price end as totalPrice,a.* from t_food_info a where a.f_id="+foodId;
        Record foodInfo = Db.find(sql).get(0);
        StringBuffer sb = new StringBuffer("select a.m_id,a.m_name,SUBSTRING_INDEX(a.m_img_adr,'~',1) as m_img_adr,a.m_cook_time,a.m_cook_price ");
        sb.append(" from t_menu_info a ,t_menu_option b");
        sb.append(" where a.m_id=b.menu_id and b.food_id= "+foodId);
        sb.append(" limit 0,10 ");
        List<Record> menuList = Db.find(sb.toString());
        foodInfo.set("menuList",menuList);
        renderSuccess("",foodInfo);
    }

    public void queryCategoryList(){
        StringBuffer sb = new StringBuffer(" select a.c_id,a.c_name,a.c_img_adr,a.c_type,b.c_id as second_id,b.c_name as second_name,b.c_img_adr as second_img ");
        sb.append(" from t_category1_info a left join t_category2_info b  on a.c_id=b.first_id ");
        sb.append(" order by a.c_sort,b.c_sort asc ");
        List<Record> categoryList = Db.find(sb.toString());
        Map<Integer,Record> firstMap = new HashMap<Integer, Record>();
        Map<Integer,List<Record>> secondMap = new HashMap<Integer,List<Record>>();
        for(Record record : categoryList){
            Integer cId = record.getInt("c_id");
            if(!firstMap.containsKey(cId)){
                Record first = new Record();
                first.set("c_id",cId);
                first.set("c_name",record.getStr("c_name"));
                first.set("c_img_adr",record.getStr("c_img_adr"));
                first.set("c_type",record.getStr("c_type"));
                firstMap.put(cId,first);
            }
            Record second = new Record();
            second.set("first_id",cId);
            second.set("c_id",record.getInt("second_id"));
            second.set("c_name",record.getStr("second_name"));
            second.set("c_img_adr",record.getStr("second_img"));
            if(secondMap.containsKey(cId)){
                secondMap.get(cId).add(second);
            }else{
                List<Record> recordList = new ArrayList<Record>();
                recordList.add(second);
                secondMap.put(cId,recordList);
            }
        }
        List<Record> resultList = new ArrayList<Record>();
        for(Integer firstId : firstMap.keySet()){
            for(Integer secondId : secondMap.keySet()){
                if(firstId.intValue() == secondId.intValue()){
                    Record record = firstMap.get(firstId);
                    record.set("category2List",secondMap.get(secondId));
                    resultList.add(record);
                    break;
                }
            }
        }
        renderSuccess("",resultList);
    }

    public void queryHotSearch(){
        List<Record> recordList = Db.find("select *from t_conf_search");
        renderSuccess("",recordList);
    }

    public void addMenuToCart(){
        String json = HttpKit.readData(getRequest());
        Map<String, Object> map = FastJson.getJson().parse(json,Map.class);
        Integer mId = (Integer)map.get("mId");
        Integer uId = (Integer)map.get("uId");
        String remark = map.get("remark").toString();
        List<CartMenu> cartMenuList = JSON.parseArray(map.get("list").toString(), CartMenu.class);
        Date now = new Date();
        CartInfo cartInfo = new CartInfo()
                .setUserId(uId)
                .setMenuId(mId)
                .setCNumber(1L)
                .setCRemark(remark)
                .setIsSelected(1)
                .setGmtCreate(now)
                .setGmtModified(now);
        if(cartInfo.save()){
            for(CartMenu cartMenu : cartMenuList){
                cartMenu.setCartId(cartInfo.getCId());
                cartMenu.setGmtCreate(now);
                cartMenu.setGmtModified(now);
            }
            Db.batchSave(cartMenuList,cartMenuList.size());
        }
        renderSuccess();
    }

    public void addMenuDefaultToCart(){
        Integer menuId = getParaToInt("menuId");
        Integer uId = getParaToInt("uId");
        List<Record> recordList = Db.find("select food_id,m_number,m_group from t_menu_option where menu_id=" + menuId + " and m_init=1 and is_free=0");
        Date now = new Date();
        CartInfo cartInfo = new CartInfo()
                .setUserId(uId)
                .setMenuId(menuId)
                .setCNumber(1L)
                .setIsSelected(1)
                .setGmtCreate(now)
                .setGmtModified(now);
        if(cartInfo.save()){
            List<CartMenu> cartMenuList = new ArrayList<CartMenu>();
            for(Record record : recordList){
                CartMenu cartMenu = new CartMenu();
                cartMenu.setFoodId(record.getInt("food_id"));
                cartMenu.setCNumber(record.getLong("m_number"));
                cartMenu.setCGroup(record.getInt("m_group"));
                cartMenu.setCartId(cartInfo.getCId());
                cartMenu.setGmtCreate(now);
                cartMenu.setGmtModified(now);
                cartMenuList.add(cartMenu);
            }
            Db.batchSave(cartMenuList,cartMenuList.size());
        }
        renderSuccess();
    }

    public void addFoodToCart(){
        String json = HttpKit.readData(getRequest());
        CartInfo cartInfo = FastJson.getJson().parse(json,CartInfo.class);
        Date now = new Date();
        if(cartInfo.getCType()==null){
            cartInfo.setCType(0);
        }
        cartInfo.setIsSelected(1);
        cartInfo.setGmtCreate(now);
        cartInfo.setGmtModified(now);
        cartInfo.save();
        renderSuccess();
    }

    public void queryCartList(){
        Integer uId = getParaToInt("uId");
        String sql = " select a.c_id,a.is_selected,a.menu_id,a.food_id,a.c_number,b.food_id as f_id,b.c_number as f_num,SUBSTRING_INDEX(c.m_img_adr,'~',1) as m_img_adr,c.m_name,c.m_cook_price,c.m_cook_time " +
                " from t_cart_info a left join t_cart_menu b on a.c_id=b.cart_id left join t_menu_info c on a.menu_id=c.m_id where a.user_id="+uId+" order by a.gmt_modified desc";
        List<Record> recordList = Db.find(sql);
        Map<Integer,Record> menuMap = new HashMap<Integer, Record>();
        List<Record> foodList = new ArrayList<Record>();
        BigDecimal sum = new BigDecimal("0.00");
        Boolean flag = true;
        for(Record record : recordList){
            Integer foodId = record.getInt("food_id");
            if(foodId==null){
                foodId = record.getInt("f_id");
            }
            //食材
            FoodInfo foodInfo = FoodInfo.dao.findById(foodId);
            Integer fType = foodInfo.getFType();
            BigDecimal fPrice = foodInfo.getFPrice();
            //保存bean
            Record item = new Record();
            Integer cId = record.getInt("c_id");
            item.set("cId",cId);
            Integer cNumber = record.getInt("c_number");
            item.set("num",cNumber);
            Integer isSelected = record.getInt("is_selected");
            if(isSelected==0){
                flag = false;
            }
            item.set("isSelected",isSelected);
            if(record.getInt("food_id")==null){
                Integer menuId = record.getInt("menu_id");
                Integer fNum = record.getInt("f_num");
                item.set("mId",menuId);
                item.set("mName",record.getStr("m_name"));
                item.set("cookTime",record.getInt("m_cook_time"));
                item.set("mImg",record.getStr("m_img_adr"));
                item.set("isHidden",1);
                BigDecimal totalPrice = fType == 0 ? fPrice.multiply(BigDecimal.valueOf(fNum)) : fPrice.multiply(BigDecimal.valueOf(fNum).divide(BigDecimal.valueOf(50)));
                if(menuMap.containsKey(cId)){
                    Record menu = menuMap.get(cId);
                    BigDecimal oldPrice = menu.getBigDecimal("totalPrice");
                    BigDecimal sumPrice = totalPrice.multiply(BigDecimal.valueOf(cNumber)).setScale(2);
                    sum = sum.add(isSelected==1?sumPrice:BigDecimal.valueOf(0));
                    menu.set("totalPrice",oldPrice.add(sumPrice));
                }else{
                    BigDecimal cookPrice = record.getBigDecimal("m_cook_price");
                    BigDecimal sumPrice = (cookPrice.add(totalPrice)).multiply(BigDecimal.valueOf(cNumber)).setScale(2);
                    sum = sum.add(isSelected==1?sumPrice:BigDecimal.valueOf(0));
                    item.set("totalPrice",sumPrice);
                    menuMap.put(cId,item);
                }
            }else{
                item.set("fId",foodId);
                item.set("fName",foodInfo.getFName());
                item.set("price",fPrice);
                item.set("fType",fType);
                BigDecimal totalPrice = fType == 0 ? fPrice.multiply(BigDecimal.valueOf(cNumber)) : fPrice.multiply(BigDecimal.valueOf(cNumber).divide(BigDecimal.valueOf(50)));
                sum = sum.add(isSelected==1?totalPrice:BigDecimal.valueOf(0));
                item.set("totalPrice",totalPrice.setScale(2));
                item.set("unit",foodInfo.getFUnit());
                item.set("fImg",foodInfo.getFImgAdr().split("~")[0]);
                foodList.add(item);
            }
        }

        Collection<Record> collection = menuMap.values();
        List<Record> menuList = new ArrayList<Record>(collection);
        Record result = new Record();
        result.set("sum",sum.setScale(2));
        result.set("selectedAll",flag?1:0);
        result.set("menuList",menuList);
        result.set("foodList",foodList);
        renderSuccess("",result);
    }

    public void checkCartItem(){
        Integer cId = getParaToInt("cId");
        Integer uId = getParaToInt("uId");
        Integer isSelected = getParaToInt("isSelected");
        String now = DateUtil.getDateYDMHMS();
        StringBuffer sb = new StringBuffer("update t_cart_info set gmt_modified='"+now+"',is_selected="+isSelected);
        if(cId!=null){
            sb.append(" where c_id="+cId);
        }else{
            sb.append(" where user_id="+uId);
        }
        Db.update(sb.toString());
        renderSuccess();
    }

    public void fineCart(){
        Integer cId = getParaToInt("cId");
        Integer flag = getParaToInt("flag");//0增加，1减少
        Integer type = getParaToInt("type");
        String now = DateUtil.getDateYDMHMS();
        Db.update(" update t_cart_info set  gmt_modified='"+now+"',c_number=c_number"+(flag==0?"+":"-")+(type==0?1:50)+" where c_id= "+cId);
        renderSuccess();
    }

    public void deleteCart(){
        String cIds = getPara("cIds");
        Db.delete("delete from t_cart_info where c_id in ("+cIds+")");
        Db.delete("delete from t_cart_menu where cart_id in ("+cIds+")");
        renderSuccess();
    }

    public void queryCartMenuOption(){
        Integer cId = getParaToInt("cId");
        Integer mId = getParaToInt("mId");
        String optionSql = "select b.cart_id,b.c_id,b.c_group,a.menu_id,c.f_id,c.f_name,c.f_type,c.f_price,b.c_number,SUBSTRING_INDEX(c.f_img_adr,'~',1) as f_img_adr,c.f_unit " +
                ",case c.f_type when 0 then b.c_number*c.f_price else b.c_number/50*c.f_price end as totalPrice " +
                " from t_cart_info a,t_cart_menu b,t_food_info c " +
                " where a.c_id=b.cart_id and b.food_id=c.f_id and a.c_id="+cId;
        List<Record> optionList = Db.find(optionSql);
        String freeSql = " select c.f_id,c.f_name,c.f_img_adr,c.f_type,c.f_price,c.f_unit,c.f_init_number " +
                " from t_menu_info a,t_menu_option b,t_food_info c " +
                " where a.m_id=b.menu_id and b.food_id=c.f_id and b.is_free=1 and a.m_id= "+mId;
        List<Record> freeList = Db.find(freeSql);
        String menuSql = "select a.c_id,a.c_type,b.m_name,b.m_cook_time,b.m_cook_price,a.c_number,a.c_remark from t_cart_info a,t_menu_info b where a.menu_id=b.m_id and a.c_id="+cId;
        Record menuInfo = Db.find(menuSql).get(0);
        menuInfo.set("optionList",optionList);
        menuInfo.set("freeList",freeList);
        renderSuccess("",menuInfo);
    }

    public void fineMenuCart(){
        String json = HttpKit.readData(getRequest());
        Map<String, Object> map = FastJson.getJson().parse(json,Map.class);
        Integer cId = Integer.valueOf(map.get("cId").toString());
        Integer cType = Integer.valueOf(map.get("cType").toString());
        String cRemark = map.get("cRemark").toString();
        List<CartMenu> cartMenuList = JSON.parseArray(map.get("list").toString(), CartMenu.class);
        String now = DateUtil.getDateYDMHMS();
        Db.update("update t_cart_info set gmt_modified='"+now+"',c_type="+cType+",c_remark='"+cRemark+"' where c_id="+cId);
        Db.batchUpdate(cartMenuList,cartMenuList.size());
        renderSuccess();
    }

    public void queryCartNum(){
        Integer uId = getParaToInt("uId");
        List<Record> recordList = Db.find("select count(1) as num from t_cart_info where user_id=" + uId);
        renderSuccess("",recordList.get(0).getInt("num"));
    }
}
