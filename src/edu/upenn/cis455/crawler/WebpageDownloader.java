package edu.upenn.cis455.crawler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import edu.upenn.cis455.crawler.info.Client;
import edu.upenn.cis455.storage.DBWrapper;


/**
 * WebpageDownloader: Download the webpage and store to the database
 * @author Jingyuan
 *
 */
public class WebpageDownloader {
	
	static DBWrapper db;
	
	public static void setup(DBWrapper db){
		WebpageDownloader.db = db;
	}
	
	public static void download(String url){
		Client client = new Client(url);
		InputStream inputStream = client.executeGET();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int next;
		try{
			while((next = inputStream.read())!=-1){
				bos.write(next);
			}
			bos.flush();
			byte[] content = bos.toByteArray();
			db.putWebpage(url, content, client.getContentType());
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
//		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//		StringBuilder content = new StringBuilder();
//		String s;
//		try {
//			while((s = br.readLine())!=null){
//				content.append(s);
//				content.append("\n");
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		db.putWebpage(url, new String(content), client.getContentType());
//		db.putWebpage(url, inputStream, client.getContentType());
		db.sync();
	}
	
	public static void store(){
		Environment myDbEnvironment = null;
		String path = "database";
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
			myDbEnvironment = new Environment(new File(path), envConfig);
		} 
		catch (DatabaseException dbe) {
			// Exception handling goes here
		}
		
		
		if(myDbEnvironment != null){
			myDbEnvironment.close();
		}
		
	}

}
