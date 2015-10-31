package edu.upenn.cis455.storage;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class User {
	
	@PrimaryKey
	private String username;
	
	private String password;
	private List<String> subscribe;
	
//	public User(String username, String password){
//		this.username = username;
//		this.password = password;
//	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public void setSubscribe(String username, String channelname){
		if(subscribe == null) subscribe = new ArrayList<String>();
		String primarykey = username+";"+channelname;
		if(subscribe.contains(primarykey)) return;
		subscribe.add(primarykey);
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public List<String> getSubscribe(){
		return subscribe;
	}
	

}
