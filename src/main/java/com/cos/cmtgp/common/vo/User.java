package com.cos.cmtgp.common.vo;


public class User {
	private String name;
	private String password;
    private String supplierId;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User(String name,String password) {
		this.setName(name);
		this.setPassword(password);
	}
	public User(String name,String password,String supplierId) {
		this.setName(name);
		this.setPassword(password);
		this.setSupplierId(supplierId);
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	
}
