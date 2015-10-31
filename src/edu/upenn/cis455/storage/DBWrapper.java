package edu.upenn.cis455.storage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

public class DBWrapper {

	private String filepath = null;

	private Environment environment;
	private EntityStore store;

	private DataAccessor dataAccessor;

	public DBWrapper(String filepath) {
		this.filepath = filepath;
		File f = new File(filepath);
		if (!f.exists()) f.mkdir();
	}

	public void setup() {
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
			File f = new File(filepath);
			environment = new Environment(f, envConfig);
			StoreConfig storeConfig = new StoreConfig();
			storeConfig.setAllowCreate(true);
			store = new EntityStore(environment, "EntityStore", storeConfig);

			dataAccessor = new DataAccessor(store);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void sync(){
		if(store != null) store.sync();
		if(environment != null) environment.sync();
	}

	public void putUser(String username, String password) {
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		dataAccessor.UserIndex.put(user);
	}

	public void putChannel(String username, String channelname,
			String stylesheetURL, String set, HashSet<String> xmlset) {
		Channel channel = new Channel();
		channel.setChannelName(channelname, username);
		channel.setStylesheetURL(stylesheetURL);
		channel.setXPath(set);
		channel.setXML(xmlset);
		dataAccessor.ChannelIndex.put(channel);
	}
	
	public void putChannel(String username, String channelname,
			String stylesheetURL, List<String> set, HashSet<String> xmlset) {
		Channel channel = new Channel();
		channel.setChannelName(channelname, username);
		channel.setStylesheetURL(stylesheetURL);
		channel.setXPathSet(set);
		channel.setXML(xmlset);
		dataAccessor.ChannelIndex.put(channel);
	}
	
	public void deleteChannel(String username, String channelname){
		String primarykey = username+";"+channelname;
		dataAccessor.ChannelIndex.delete(primarykey);
	}
	
	public void addSubscribe(String username, String owner, String channelname){
		String primarykey = owner+";"+channelname;
		User user = dataAccessor.UserIndex.get(username);
		user.setSubscribe(owner, channelname);
		dataAccessor.UserIndex.put(user);
	}
	
	public void unSubscribe(String username, String owner, String channelname){
		User user = dataAccessor.UserIndex.get(username);
		List<String> subscribes = user.getSubscribe();
		String unSubscribe = null;
		for(int i=0;i<subscribes.size();i++){
			String[] channelinfo = subscribes.get(i).split(";");
			if(channelinfo[1].equals(channelname) && channelinfo[0].equals(owner)){
				unSubscribe = subscribes.get(i);
				break;
			}
		}
		if(unSubscribe!=null) subscribes.remove(unSubscribe);
		dataAccessor.UserIndex.put(user);
	}
	
	public void putWebpage(String url, byte[] content, String type) {
		Webpage webpage = new Webpage();
		webpage.setContent(content);
		Calendar cal = Calendar.getInstance();
    	long current_time = cal.getTime().getTime();
		webpage.setCrawlTime(current_time);
		if(type.startsWith("text/html")) webpage.setType("html");
		else webpage.setType("xml");
		webpage.setURL(url);
		dataAccessor.WebpageIndex.put(webpage);
	}

//	public void putWebpage(String url, String content, String type) {
//		Webpage webpage = new Webpage();
//		webpage.setContent(content);
//		Calendar cal = Calendar.getInstance();
//    	long current_time = cal.getTime().getTime();
//		webpage.setCrawlTime(current_time);
//		if(type.startsWith("text/html")) webpage.setType("html");
//		else webpage.setType("xml");
//		webpage.setURL(url);
//		dataAccessor.WebpageIndex.put(webpage);
//	}

	public User getUser(String username) {
		System.out.println(dataAccessor == null);
		System.out.println( dataAccessor.UserIndex== null);
		return dataAccessor.UserIndex.get(username);
	}

	public Channel getChannel(String username, String channelname) {
		String primarykey = username + ";" + channelname;
		return dataAccessor.ChannelIndex.get(primarykey);
	}

	public Webpage getWebpage(String url) {
		return dataAccessor.WebpageIndex.get(url);
	}

	public List<Channel> getUserChannels(String username) {
		List<Channel> channels = new ArrayList<Channel>();
		EntityCursor<Channel> sec_cursor = dataAccessor.ChannelUsernameIndex.subIndex(username).entities();
		try {
			for(Channel channel:sec_cursor){
				channels.add(channel);
			}
		} finally {
			sec_cursor.close();
		}
		return channels;
	}

	public List<Channel> getAllChannels() {
		List<Channel> channels = new ArrayList<Channel>();
		EntityCursor<Channel> cursor = dataAccessor.ChannelIndex.entities();
		try {
			for (Channel channel : cursor) {
				channels.add(channel);
			}
		} finally {
			cursor.close();
		}
		return channels;
	}
	
	public void ProcessXpath(String url){
		Webpage webpage = getWebpage(url);
		if(webpage.getType().equals("xml")){
			List<Channel> channels = getAllChannels();
			for(Channel channel:channels){
				String[] expressions = channel.getXPathset().toArray(new String[channel.getXPathset().size()]);
				try{
					XPathEngine XPathHandler = XPathEngineFactory.getXPathEngine();
					XPathHandler.setXPaths(expressions);
					SAXParserFactory factory = SAXParserFactory.newInstance();
					InputStream xmlInput = new ByteArrayInputStream(webpage.getContent());
					SAXParser saxParser = factory.newSAXParser();
					saxParser.parse(xmlInput, (DefaultHandler)XPathHandler);
					boolean[] result = XPathHandler.evaluate(null);
					for(int i=0;i<result.length;i++){
						if(result[i] == true){
							System.out.println("*****************************************");
							System.out.println(webpage.getURL());
							System.out.println("*****************************************");
							HashSet<String> set = channel.getMatchURLs();
							set.add(webpage.getURL());
							putChannel(channel.getUsername(), channel.getChannelName(),
									channel.getStylesheetURL(), channel.getXPathset(), set);
							break;
						}
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public List<String> ProcessXPath(String[] expressions){
		List<String> urls = new ArrayList<String>(); 
		
		EntityCursor<Webpage> cursor = dataAccessor.WebpageIndex.entities();
		for(Webpage webpage:cursor){
			if(webpage.getType().equals("xml")){
				System.out.println(webpage.getURL());
				try{
					XPathEngine XPathHandler = XPathEngineFactory.getXPathEngine();
					XPathHandler.setXPaths(expressions);
					SAXParserFactory factory = SAXParserFactory.newInstance();
					InputStream xmlInput = new ByteArrayInputStream(webpage.getContent());
					SAXParser saxParser = factory.newSAXParser();
					saxParser.parse(xmlInput, (DefaultHandler)XPathHandler);
					boolean[] result = XPathHandler.evaluate(null);
					for(int i=0;i<result.length;i++){
						if(result[i] == true){
							System.out.println(webpage.getURL());
							urls.add(webpage.getURL());
							break;
						}
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return urls;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void close() {
		if (environment != null) {
			try {
				store.close();
				environment.close();
			} catch (Exception dbe) {
				System.err
						.println("Error closing environment" + dbe.toString());
			}
		}
	}

	public static void main(String[] args) {
		String filepath = "/home/cis455";
		DBWrapper db = new DBWrapper(filepath);
		db.setup();
//		db.putWebpage("www.google.com", "this is just a test", "html");
////		db.putWebpage("www.google.com", "modify it", "html");
//		Webpage webpage = db.getWebpage("www.google.com");
//		System.out.println(webpage.getContent());
//		db.putChannel("katie", "movie", "www.test.com",
//				"/note/to;/note;/path/test[@attribute = 'abc']");
//		db.putChannel("peter", "song", "www.baidu.com", "/abc/dbs;/uw/uo");
//		db.putChannel("katie", "stars", "www.test.com", "/stars/songs");
//		List<Channel> lists = db.getUserChannels("katie");
//		for (Channel channel : lists) {
//			System.out.println(channel.getPrimaryKey() + " "
//					+ channel.getChannelName());
//		}

		db.putUser("katie", "password");
		User user = db.getUser("katie");
		System.out.println(user.getPassword());
		User user1 = db.getUser("peter");
		System.out.println(user1);
	}

}
