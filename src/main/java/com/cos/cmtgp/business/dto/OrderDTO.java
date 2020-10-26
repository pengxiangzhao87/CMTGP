package com.cos.cmtgp.business.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author pengxiangZhao
 * @Description: TODO
 * @ClassName: OrderDTO
 * @Date 2020/5/21 0021
 */
public class OrderDTO {
    private Integer uId;
    private BigDecimal totalPrice;
    private String name;
    private String phone;
    private String address;
    private Integer status;
    private Integer channel;
    private BigDecimal postCost;
    private String rangeTime;
    private String token;
    private List<DetailDTO> details;

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public BigDecimal getPostCost() {
        return postCost;
    }

    public void setPostCost(BigDecimal postCost) {
        this.postCost = postCost;
    }

    public List<DetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<DetailDTO> details) {
        this.details = details;
    }

    public String getRangeTime() {
        return rangeTime;
    }

    public void setRangeTime(String rangeTime) {
        this.rangeTime = rangeTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
