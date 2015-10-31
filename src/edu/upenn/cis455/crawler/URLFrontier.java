package edu.upenn.cis455.crawler;

import java.util.HashMap;
import java.util.LinkedList;

import edu.upenn.cis455.crawler.info.Client;


/**
 * URLFrontier: 
 * store all the webpages that have been visited
 * maintain a queue to get the current request
 * @author Jingyuan
 *
 */
public class URLFrontier {
	HashMap<String, Long> visitedURLs = new HashMap<String, Long>();
	LinkedList<String> queue = new LinkedList<String>();
	int maxSize;
	
	public URLFrontier(int maxSize){
		this.maxSize = maxSize;
	}
	
	public boolean filter(String url){
		Client client = new Client(url);
		client.executeHEAD();
		if(!client.isValid(maxSize)) {
//			System.out.println("filter process: invalid "+url+" "+client.getContentType()+" "+client.getContentLength());
			return false;
		}
		long current_crawl = client.getLastModifiedTime();
		if(visitedURLs.containsKey(url)){
			long last_crawl = visitedURLs.get(url);	
			if(current_crawl>last_crawl){
//				visitedURLs.put(url, current_crawl);
				return true;
			}
			else{
				System.out.println("not modified: "+url);
				return false;
			}
		}
//		visitedURLs.put(url, current_crawl);
		return true;
	}
	
	public boolean isEmpty(){
		return queue.isEmpty();
	}
	
	public String popURL(){
		return queue.poll();
	}
	
	public void pushURL(String url){
		queue.add(url);
		Client client = new Client(url);
		client.executeHEAD();
		long current_crawl = client.getLastModifiedTime();
		visitedURLs.put(url, current_crawl);
	}

}
