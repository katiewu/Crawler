package edu.upenn.cis455.storage;

import static com.sleepycat.persist.model.Relationship.MANY_TO_ONE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class Channel {
	
	@PrimaryKey
	private String primarykey;
	
	@SecondaryKey(relate=MANY_TO_ONE)
	private String username;
	
	private String channelName;	
	private String stylesheetURL;
	private List<String> XPathSet = new ArrayList<String>();
	private HashSet<String> matchURLS = new HashSet<String>();
	
//	public Channel(String username, String channelName, String stylesheetURL, String set){
//		this.primarykey = username+";"+channelName;
//		this.username = username;
//		this.channelName = channelName;
//		this.stylesheetURL = stylesheetURL;
//		String[] xpaths = set.split(";");
//		for(String xpath:xpaths){
//			XPathSet.add(xpath);
//		}
//	}
	
	public void setChannelName(String channelName, String username){
		this.channelName = channelName;
		this.username = username;
		this.primarykey = username+";"+channelName;
	}
	
	public void setStylesheetURL(String url){
		this.stylesheetURL = url;
	}
	
	public void setXPath(String set){
		String[] xpaths = set.split(";");
		for(String xpath:xpaths){
			XPathSet.add(xpath);
		}
	}
	
	public void setXPathSet(List<String> set){
		this.XPathSet = set;
	}
	
	public void setXML(HashSet<String> xmlset){
		this.matchURLS = xmlset;
	}
	
	public String getPrimaryKey(){
		return primarykey;
	}
	
	public String getChannelName(){
		return channelName;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getStylesheetURL(){
		return stylesheetURL;
	}
	
	public List<String> getXPathset(){
		return XPathSet;
	}
	
	public HashSet<String> getMatchURLs(){
		return matchURLS;
	}

}
