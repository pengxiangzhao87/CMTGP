package com.cos.cmtgp.business.service;

import com.cos.cmtgp.business.model.CommodityInfo;
import com.cos.cmtgp.business.model.ShoppingInfo;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

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
    public List<Record> queryShoppingCart(Integer userId){
        String sql = " select c.s_corporate_name,a.id,b.s_id,SUBSTRING_INDEX(b.s_address_img,'~',1)  as imgUrl,b.s_name,b.state, " +
                " CONCAT('￥',b.s_price,'/',b.s_unit) as sell,a.s_num,b.price_unit,b.init_unit, " +
                " round(case b.init_unit when 0 then a.s_num/50*b.price_unit else a.s_num*b.price_unit end,2) as totalPrice,a.is_check " +
                " from t_shopping_info a " +
                " left join t_commodity_info b on a.s_id=b.s_id " +
                "  left join t_supplier_setting c on b.p_id=c.s_id " +
                " where a.u_id="+userId + " order by b.state desc ";
        List<Record> recordList = Db.find(sql);
        Map<String,List<Record>> map = new HashMap<String,List<Record>>();
        for(Record record : recordList){
            String s_corporate_name = record.getStr("s_corporate_name");
            if(map.containsKey(s_corporate_name)){
                map.get(s_corporate_name).add(record);
            }else{
                List<Record> list = new ArrayList<Record>();
                list.add(record);
                map.put(s_corporate_name,list);
            }
        }
        List<Record> result = new ArrayList<Record>();
        for(String str : map.keySet()){
            Record item = new Record();
            item.set("supplier",str);
            item.set("goods",map.get(str));
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
