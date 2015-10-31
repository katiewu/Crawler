package edu.upenn.cis455.crawler.info;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.w3c.tidy.Tidy;

public class HttpClient {
	
	public HttpClient(){
		
	}
	
//	public HttpClient(String url, int maxSize){
//		this.maxSize = maxSize*1024*1024;
//		URLInfo urlinfo = new URLInfo(url);
//		hostName = urlinfo.getHostName();
//		path = urlinfo.getFilePath();
//		portNumber = urlinfo.getPortNo();
//		Socket socket;
//		try {
//			socket = new Socket(InetAddress.getByName(hostName), portNumber);
//			//send HEAD request
//			PrintWriter pw = new PrintWriter(socket.getOutputStream());
//			pw.println("HEAD "+path+" "+"HTTP/1.1");
//			pw.println("Host: "+hostName);
//			pw.println("User-Agent:cis455crawler");
//			pw.println("");
//			pw.flush();
//			//get response
//			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			String s;
//			s = br.readLine();
//			if(processInitialLine(s)){
//				while((s = br.readLine()) != null){
//					if(s.equals("")) break;
//					Pattern r = Pattern.compile("(.*?): (.*)");
//					Matcher m = r.matcher(s);
//					if(m.find()){
//						responseHeaders.put(m.group(1).toLowerCase(), m.group(2));
//					}
////					System.out.println(s);
//				}
//				if(isValidType() && getContentLength() != -1) isValid = true;
//			}
//			br.close();
//		} catch (UnknownHostException e) {
//			isValid = false;
//			e.getStackTrace();
//		} catch (IOException e) {
//			isValid = false;
//			e.getStackTrace();
//		}
//	}
	
	public InputStream execute(String url){
		if(url.startsWith("https")){
			URL https_url;
			try {
				https_url = new URL(url);
				HttpsURLConnection urlConnection = (HttpsURLConnection)https_url.openConnection();
				return urlConnection.getInputStream();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		else if(url.startsWith("http")){
			URLInfo urlinfo = new URLInfo(url);
			String hostName = urlinfo.getHostName();
			String path = urlinfo.getFilePath();
			int portNumber = urlinfo.getPortNo();
			System.out.println(hostName+" "+path+" "+portNumber);
			Socket socket;
			try {
				socket = new Socket(InetAddress.getByName(hostName), portNumber);
				//send HEAD request
				PrintWriter pw = new PrintWriter(socket.getOutputStream());
				pw.println("GET "+path+" "+"HTTP/1.1");
				pw.println("Host: "+hostName);
				pw.println("User-Agent:cis455crawler");
				pw.println("");
				pw.flush();
				
				HashMap<String, String> responseHeaders = new HashMap<String, String>();
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String s;
				s = br.readLine();
				if(processInitialLine(s)){
					while((s = br.readLine()) != null){
						if(s.equals("")) break;
						Pattern r = Pattern.compile("(.*?): (.*)");
						Matcher m = r.matcher(s);
						if(m.find()){
							responseHeaders.put(m.group(1).toLowerCase(), m.group(2));
						}
						System.out.println(s);
					}
					if(responseHeaders.containsKey("content-length")){
						int bodysize = Integer.parseInt(responseHeaders.get("content-length"));
						char[] body = new char[bodysize];
						br.read(body, 0, bodysize);
						String responseBody = new String(body);
						return convertXML(responseBody, responseHeaders.get("content-type"));
					}
				}
				return null;
			} catch (UnknownHostException e) {
				e.getStackTrace();
				return null;
			} catch (IOException e) {
				e.getStackTrace();
				return null;
			} 
		}
		else{
			return null;
		}
		
	}
	
	public InputStream convertXML(String responseBody, String type){
		if(type.equals("text/html")){
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setDocType("omit");
			ByteArrayInputStream inputStream;
			try {
				inputStream = new ByteArrayInputStream(responseBody.getBytes("UTF-8"));
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				tidy.parseDOM(inputStream, outputStream);
				responseBody = outputStream.toString("UTF-8");
				System.out.println(responseBody);
				return new ByteArrayInputStream(responseBody.getBytes());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}				
		}
		return new ByteArrayInputStream(responseBody.getBytes());
	}
	
//	public void sendGETRequest(){
//		Socket socket;
//		try {
//			socket = new Socket(InetAddress.getByName(hostName), portNumber);
//			//send GET request
//			PrintWriter pw = new PrintWriter(socket.getOutputStream());
//			pw.println("GET "+path+" "+"HTTP/1.1");
//			pw.println("Host: "+hostName);
//			pw.println("User-Agent:cis455crawler");
//			pw.println("");
//			pw.flush();
//			//get response
//			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			String s;
//			while((s = br.readLine()) != null){
////				if(s.equals("")) break;
//				Pattern r = Pattern.compile("(.*?): (.*)");
//				Matcher m = r.matcher(s);
//				if(m.find()){
//					responseHeaders.put(m.group(1).toLowerCase(), m.group(2));
//				}
//				System.out.println(s);
//			}	
//			br.close();
//		} catch (UnknownHostException e) {
//			isValid = false;
//			e.getStackTrace();
//		} catch (IOException e) {
//			isValid = false;
//			e.getStackTrace();
//		}
//	}
	
	public boolean processInitialLine(String line){
		if(line.equals("HTTP/1.1 200 OK")) return true;
		return false;
	}

	
//	public boolean isValidType(){
//		if(responseHeaders.containsKey("content-type")){
//			String type = responseHeaders.get("content-type");
//			if(type.equals("text/html")) return true;
//			if(type.equals("application/xml")) return true;
//			if(type.equals("text/xml")) return true;
//			if(type.endsWith("+xml")) return true;
//		}
//		return false;
//	}
//	
//	public int getContentLength(){
//		if(responseHeaders.containsKey("content-length")){
//			int size = Integer.parseInt(responseHeaders.get("content-length"));
//			if(size<maxSize) return size;
//		}
//		return -1;
//	}
//	
//	public boolean isValid(){
//		return isValid;
//	}
//	
//	public static void main(String[] args){
//		String url = "https://inet-csm.symplicity.com/students/";
//		HttpClient client = new HttpClient(url, 1);
//		client.sendGETRequest();
//	}

}
