package com.cos.cmtgp.common.vo;


public class SysMenu {

	private String create_time;
	private int order_num;
	private String icon;
	private int parent_id;
	private String menu_url;
	private String menu_name;
	private int id;
	
	public SysMenu(int id,String menu_name,String menu_url,int parent_id,String icon,int order_num,String create_time){
		this.setCreate_time(create_time);
		this.setIcon(icon);
		this.setId(id);
		this.setMenu_name(menu_name);
		this.setMenu_url(menu_url);
		this.setParent_id(parent_id);
		this.setOrder_num(order_num);
		
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public int getOrder_num() {
		return order_num;
	}
	public void setOrder_num(int order_num) {
		this.order_num = order_num;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getParent_id() {
		return parent_id;
	}
	public void setParent_id(int parent_id) {
		this.parent_id = parent_id;
	}
	public String getMenu_url() {
		return menu_url;
	}
	public void setMenu_url(String menu_url) {
		this.menu_url = menu_url;
	}
	public String getMenu_name() {
		return menu_name;
	}
	public void setMenu_name(String menu_name) {
		this.menu_name = menu_name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
