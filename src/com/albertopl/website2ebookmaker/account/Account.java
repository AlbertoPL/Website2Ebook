package com.albertopl.website2ebookmaker.account;

import java.util.ArrayList;
import java.util.List;

import com.Ostermiller.util.Base64;

public class Account {

	private String username;
	private boolean guest;
	private List<String> websiteURLs;
	private String passwordHash;
	
	public Account() {
		username = "Guest";
		passwordHash = "";
		websiteURLs = new ArrayList<String>();
		guest = true;
	}
	
	public Account(String username) {
		this();
		this.username = username;
	}
	
	public void addWebsite(String url) {
		websiteURLs.add(url);
	}
	
	public boolean hasWebsite(String url) {
		return websiteURLs.contains(url);
	}
	
	public String getUsername() {
		return username;
	}
	
	public boolean isGuest() {
		return guest;
	}
	
	public void setUsername(String email) {
		username = email;
		guest = false;
	}
	
	//This is because we are using Cloudmine, this can change!
	public void setCloudMinePasswordHash(String password) {
		passwordHash = Base64.encode(username + ":" + password);
	}
	
	public String getPasswordHash() {
		return passwordHash;
	}
}
