package com.cos.cmtgp.business.dto;

/**
 * @author pengxiangZhao
 * @Description: TODO
 * @ClassName: MiniTempDTO
 * @Date 2020/9/1 0001
 */
public class MiniTempDTO {
    private String touser;
    private String template_id;
    private String page;
    private MiniTempDataDTO data;
    private String miniprogram_state;

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTemplate_id() {
        return template_id;
    }

    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public MiniTempDataDTO getData() {
        return data;
    }

    public void setData(MiniTempDataDTO data) {
        this.data = data;
    }

    public String getMiniprogram_state() {
        return miniprogram_state;
    }

    public void setMiniprogram_state(String miniprogram_state) {
        this.miniprogram_state = miniprogram_state;
    }
}
