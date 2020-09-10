package com.cos.cmtgp.business.dto;

import java.math.BigDecimal;

/**
 * @author pengxiangZhao
 * @Description: TODO
 * @ClassName: DetailDTO
 * @Date 2020/5/21 0021
 */
public class DetailDTO {
    private Integer sId;
    private BigDecimal paymentPrice;
    private Integer orderNum;

    public Integer getsId() {
        return sId;
    }

    public void setsId(Integer sId) {
        this.sId = sId;
    }

    public BigDecimal getPaymentPrice() {
        return paymentPrice;
    }

    public void setPaymentPrice(BigDecimal paymentPrice) {
        this.paymentPrice = paymentPrice;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}
