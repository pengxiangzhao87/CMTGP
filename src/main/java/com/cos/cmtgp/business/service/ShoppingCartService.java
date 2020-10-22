package com.cos.cmtgp.business.service;

import com.cos.cmtgp.business.model.CommodityInfo;
import com.cos.cmtgp.business.model.ShoppingInfo;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ShoppingCartService {

    /**
     * APP
     * 添加到购物车
     * @param newEntity
     */
    @Before(Tx.class)
    public void addShopingCart(ShoppingInfo newEntity){
        CommodityInfo commodityInfo = CommodityInfo.dao.findById(newEntity.getSId());
        List<ShoppingInfo> shoppingInfoList = ShoppingInfo.dao.find("select * from t_shopping_info where u_id=? and s_id=?", newEntity.getUId(), newEntity.getSId());
        Integer sum = null;
        if(shoppingInfoList.size()>0){
            ShoppingInfo shoppingInfo = shoppingInfoList.get(0);
            if(newEntity.getSNum()==null ){
                sum = commodityInfo.getInitNum() + shoppingInfo.getSNum();
            }else{
                sum = newEntity.getSNum() + shoppingInfo.getSNum();
            }
            shoppingInfo.setDateTime(new Date());
            shoppingInfo.setSNum(sum);
            shoppingInfo.update();
        }else{
            if(newEntity.getSNum()==null ){
                sum = commodityInfo.getInitNum();
                newEntity.setSNum(sum);
            }else{
                sum = newEntity.getSNum();
            }
            newEntity.setDateTime(new Date());
            newEntity.save();
        }
    }

    /**
     * APP
     *  查询购物车列表
     */
    public List<Record> queryShoppingCart(Integer userId,String areaFlag,Integer supId){
        String sql = " select c.s_corporate_name,c.s_id as supId,c.short_start,c.short_deal,c.short_free,c.city_start,c.city_deal,c.city_free,c.long_start,c.long_deal,c.long_free, " +
                " a.id,b.s_id,SUBSTRING_INDEX(b.s_address_img,'~',1)  as imgUrl,b.s_name,b.state,a.s_num,b.price_unit,b.init_unit, " +
                " round(case b.init_unit when 0 then a.s_num/50*b.price_unit else a.s_num*b.price_unit end,2) as totalPrice,a.is_check " +
                " from t_shopping_info a " +
                " left join t_commodity_info b on a.s_id=b.s_id " +
                " left join t_supplier_setting c on b.p_id=c.s_id " +
                " where find_in_set(b.delivery_area,'"+areaFlag+"')>0 and b.dr=0 and a.u_id="+userId;
        if(supId!=null){
            sql += " and c.s_id="+supId+" and a.is_check=1 and state=1 ";
        }
        sql += " order by b.state desc ";
        List<Record> recordList = Db.find(sql);
        Map<String,List<Record>> map = new HashMap<String,List<Record>>();
        Map<String,Record> sumMap = new HashMap<String,Record>();
        for(Record record : recordList){
            String s_corporate_name = record.getStr("s_corporate_name");
            Integer state = record.getInt("state");
            Integer isCheck = record.getInt("is_check");
            Integer initUnit = record.getInt("init_unit");
            Integer sNum = record.getInt("s_num");
            Integer sum = initUnit==0?sNum/50:sNum;
            BigDecimal totalPrice = record.getBigDecimal("price_unit").multiply(new BigDecimal(sum + "")).setScale(2, RoundingMode.HALF_UP);
            record.set("isTouchMove",false);
            if(initUnit==0 && sNum<=50 || initUnit==1 && sNum==1){
                record.set("disabled",true);
            }else {
                record.set("disabled",false);
            }
            if(map.containsKey(s_corporate_name)){
                map.get(s_corporate_name).add(record);
                if(state==1 && !sumMap.isEmpty()) {
                    Record re = sumMap.get(s_corporate_name);
                    if(isCheck==1){
                        re.set("totalPrice", re.getBigDecimal("totalPrice").add(totalPrice));
                    }
                    Boolean selectedAll = re.getBoolean("selectedAll");
                    if(selectedAll){
                        re.set("selectedAll",isCheck==1);
                    }
                    sumMap.put(s_corporate_name,re);
                }
            }else{
                List<Record> list = new ArrayList<Record>();
                list.add(record);
                map.put(s_corporate_name,list);
                if(state==1){
                    Record re = new Record();
                    re.set("totalPrice",isCheck==1?totalPrice:BigDecimal.valueOf(0));
                    re.set("selectedAll",isCheck==1);
                    sumMap.put(s_corporate_name,re);
                }
            }
        }
        List<Record> result = new ArrayList<Record>();
        for(String str : map.keySet()){
            Record item = new Record();
            item.set("supplier",str);
            List<Record> records = map.get(str);
            item.set("goods",records);
            item.set("supId",records.get(0).getInt("supId"));
            Record record = sumMap.get(str);
            //初始化
            item.set("postage",BigDecimal.valueOf(0));
            item.set("canBtn",false);
            item.set("btnMsg","");
            if(record!=null){
                item.set("selectedAll",record.getBoolean("selectedAll"));
                BigDecimal totalPrice = record.getBigDecimal("totalPrice");
                item.set("totalPrice",totalPrice);
                BigDecimal postage = BigDecimal.valueOf(0);
                String btnMsg = "去结算";
                boolean canBtn ;
                if(areaFlag.contains("0")){
                    BigDecimal shortStart = records.get(0).getBigDecimal("short_start");
                    BigDecimal shortFree = records.get(0).getBigDecimal("short_free");
                    BigDecimal shortDeal = records.get(0).getBigDecimal("short_deal");
                    if(totalPrice.compareTo(shortStart)==-1){
                        btnMsg = "差"+shortStart.subtract(totalPrice)+"元起送";
                        postage = shortDeal;
                        canBtn = false;
                    }else if(totalPrice.compareTo(shortFree)!=-1){
                        canBtn = true;
                    }else{
                        postage = shortDeal;
                        canBtn = true;
                    }
                }else if(areaFlag.contains("1")){
                    BigDecimal cityStart = records.get(0).getBigDecimal("city_start");
                    BigDecimal cityFree = records.get(0).getBigDecimal("city_free");
                    BigDecimal cityDeal = records.get(0).getBigDecimal("city_deal");
                    if(totalPrice.compareTo(cityStart)==-1){
                        btnMsg = "差"+cityStart.subtract(totalPrice)+"元起送";
                        postage = cityDeal;
                        canBtn = false;
                    }else if(totalPrice.compareTo(cityFree)!=-1){
                        canBtn = true;
                    }else{
                        postage = cityDeal;
                        canBtn = true;
                    }
                }else{
                    BigDecimal longStart = records.get(0).getBigDecimal("long_start");
                    BigDecimal longFree = records.get(0).getBigDecimal("long_free");
                    BigDecimal longDeal = records.get(0).getBigDecimal("long_deal");
                    if(totalPrice.compareTo(longStart)==-1){
                        btnMsg = "差"+longStart.subtract(totalPrice)+"元起送";
                        postage = longDeal;
                        canBtn = false;
                    }else if(totalPrice.compareTo(longFree)!=-1){
                        canBtn = true;
                    }else{
                        postage = longDeal;
                        canBtn = true;
                    }
                }
                item.set("postage",postage);
                item.set("canBtn",canBtn);
                item.set("btnMsg",btnMsg);
            }
            result.add(item);
        }
        return result;
    }

    /**
     * APP
     * 微调
     * @param id
     * @param number
     * @param sId
     * @return
     */
    @Before(Tx.class)
    public void fineTuning(Integer id,Integer number,Integer sId){
        String updateSql = " update t_shopping_info set s_num=?,date_time=now() where id=?";
        int update = Db.update(updateSql, number,id);
    }

    /**
     * APP
     * 商品是否选中
     * @param id
     * @param check
     */
    @Before(Tx.class)
    public void checkCommodity(String id,Integer check){
        int update = Db.update("update t_shopping_info set is_check="+check+" where id in ("+id+")");
    }


    /**
     * APP
     * 从购物车移除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteShoppingCart(String ids){
        int update = Db.update("delete from t_shopping_info where id in ("+ids+")");
    }
}
