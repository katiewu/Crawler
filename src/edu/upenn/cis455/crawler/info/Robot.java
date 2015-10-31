package edu.upenn.cis455.crawler.info;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Robot {
	String url;
	String hostName;
	String protocol;
	InputStream inputStream;
	List<String> disallowList = new ArrayList<String>();
	List<String> allowList = new ArrayList<String>();
	int crawl_delay = 0;
	long last_visited = 0;
	
	public Robot(String url){		
		try {
			URL robot_url = new URL(getRobotURL(url));
			hostName = robot_url.getHost();
			protocol = robot_url.getProtocol();
			System.out.println("robot "+hostName+" "+protocol);
			if(protocol.equals("http")){
				URLConnection urlConnection = robot_url.openConnection();
				urlConnection.connect();
				inputStream = urlConnection.getInputStream();
			}
			else if(protocol.equals("https")){
				HttpsURLConnection urlConnection = (HttpsURLConnection)robot_url.openConnection();
				urlConnection.connect();
				inputStream = urlConnection.getInputStream();
			}
			parseInputStream();
		} 
		catch (MalformedURLException e) { 
		    e.printStackTrace();
		} 
		catch (IOException e) {   
		    e.printStackTrace();
		}
	}
	
	private String getRobotURL(String url){
		URLInfo urlinfo = new URLInfo(url);
		String hostName = urlinfo.getHostName();
		String protocol = urlinfo.getProtocol();
		String robotURL = protocol+"://"+hostName+"/robots.txt";
		return robotURL;
	}
	
	private void parseInputStream() throws IOException{
		if(inputStream!=null){
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String s;
			int flag = 0;
			while((s = br.readLine())!=null){
				if(s.toLowerCase().equals("user-agent: *")) break;
				else if(s.toLowerCase().equals("user-agent: cis455crawler")){
					flag = 1;
					break;
				}
			}
			while((s = br.readLine())!=null){
				s = s.toLowerCase();
			    if(s.startsWith("disallow: ")){
			    	disallowList.add(s.substring(10));
			    }
			    else if(s.startsWith("allow: ")){
			    	allowList.add(s.substring(7));
			    }
			    else if(s.startsWith("crawl-delay")){
			    	crawl_delay = Integer.parseInt(s.substring(13));
			    }
			    else if(s.startsWith("user-agent")) break;
			}
			if(s != null && s.startsWith("user-agent: *")){
				if(flag == 1) return;
			}
			else if(s != null && s.startsWith("user-agent: cis455crawler")){
				disallowList.clear();
				allowList.clear();
				crawl_delay = 0;
				while((s = br.readLine())!=null){
					s = s.toLowerCase();
					if(s.startsWith("disallow: ")){
				    	disallowList.add(s.substring(10));
				    }
				    else if(s.startsWith("allow: ")){
				    	allowList.add(s.substring(7));
				    }
				    else if(s.startsWith("crawl-delay")){
				    	crawl_delay = Integer.parseInt(s.substring(13));
				    }
				    else if(s.startsWith("user-agent")) break;
				}
			}

		}
	}
	
	public boolean isURLValid(String url){
		URLInfo urlinfo = new URLInfo(url);
		String path = urlinfo.getFilePath();
		for(String allowpath:allowList){
			if(allowpath.endsWith("/")){
				if(path.contains(allowpath)) return true;
			}
			else{
				if(path.equals(allowpath)) return true;
			}
		}
		for(String disallowpath:disallowList){
			if(disallowpath.endsWith("/")){
				if(path.contains(disallowpath)) return false;
			}
			else{
				if(path.equals(disallowpath)) return false;
			}
		}
		return true;
	}
	
	public void setLastVisited(){
		Calendar cal = Calendar.getInstance();
    	last_visited = cal.getTime().getTime();	
	}
	
	public long getLastVisited(){
		return last_visited;
	}
	
	public int getCrawlDelay(){
		return crawl_delay;
	}
	
	
}
