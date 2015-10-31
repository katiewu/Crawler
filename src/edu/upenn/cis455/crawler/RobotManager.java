package edu.upenn.cis455.crawler;

import java.util.Calendar;
import java.util.HashMap;

import edu.upenn.cis455.crawler.info.Robot;
import edu.upenn.cis455.crawler.info.URLInfo;

/**
 * RobotManager
 * provides the functionalities that check whether the crawling action 
 * meets with the requirement of robot.txt
 * store robot.txt information of visited hosts
 * @author Jingyuan
 *
 */
public class RobotManager {
	static HashMap<String, Robot> robots = new HashMap<String, Robot>();
	
	public static boolean isValid(String url){
		addRobot(url);
		URLInfo urlinfo = new URLInfo(url);
		String hostName = urlinfo.getHostName();
		Robot robot = robots.get(hostName);
		return robot.isURLValid(url);
	}
	
	public static void addRobot(String url){
		URLInfo urlinfo = new URLInfo(url);
		String hostName = urlinfo.getHostName();
		if(!robots.containsKey(hostName)){
			Robot robot = new Robot(url);
			robots.put(hostName, robot);
		}
	}
	
	public static boolean checkDelay(String url){
		URLInfo urlinfo = new URLInfo(url);
		String hostname = urlinfo.getHostName();
		if(robots.containsKey(hostname)){
			Robot robot = robots.get(hostname);
			int crawl_delay = robot.getCrawlDelay();
			if(crawl_delay == 0) return true;
			long last_visited = robot.getLastVisited();
			Calendar cal = Calendar.getInstance();
			long current_time = cal.getTime().getTime();
			if((current_time-last_visited)>=(crawl_delay*1000)) return true;
			else return false;
		}
		return true;
	}

	public static void setCurrentTime(String url){
		URLInfo urlinfo = new URLInfo(url);
		String hostname = urlinfo.getHostName();
		Robot robot = robots.get(hostname);
		robot.setLastVisited();
	}
}
