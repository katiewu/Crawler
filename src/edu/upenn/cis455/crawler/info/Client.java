package edu.upenn.cis455.crawler.info;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Client {
	String url;
	String hostName;
	String path;
	int portNumber;
	int contentLength;
	String contentType = "text/html";
	long last_modified;
	
	public Client(String url){
		this.url = url;
		URLInfo urlinfo = new URLInfo(url);
		this.hostName = urlinfo.getHostName();
		this.path = urlinfo.getFilePath();
		this.portNumber = urlinfo.getPortNo();
	}
	
	public void executeHEAD(){
		if(url.startsWith("https")){
			URL https_url;
			try {
				https_url = new URL(url);
				HttpsURLConnection urlConnection = (HttpsURLConnection)https_url.openConnection();
				urlConnection.connect();
				contentLength = urlConnection.getContentLength();
				contentType = urlConnection.getContentType();
				last_modified = urlConnection.getLastModified();
//				System.out.println("execute HEAD: "+url+" "+contentLength+" "+contentType);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(url.startsWith("http")){
			Socket socket;
			try {
//				System.out.println(hostName+" "+portNumber);
				socket = new Socket(InetAddress.getByName(hostName), portNumber);
				//send HEAD request
				PrintWriter pw = new PrintWriter(socket.getOutputStream());
				pw.println("HEAD "+path+" "+"HTTP/1.0");
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
							if(m.group(1).toLowerCase().equals("content-length")){
								contentLength = Integer.parseInt(m.group(2));
							}
							if(m.group(1).toLowerCase().equals("content-type")){
								contentType = m.group(2);
							}
							if(m.group(1).toLowerCase().equals("last-modified")){
								String date = m.group(2);
								SimpleDateFormat f = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
								Date d = f.parse(date);
								last_modified = d.getTime();
							}
						}
						System.out.println(s);
					}
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} 
		}
	}
	
	public InputStream executeGET(){
		if(url.startsWith("https")){
			URL https_url;
			try {
				https_url = new URL(url);
				HttpsURLConnection urlConnection = (HttpsURLConnection)https_url.openConnection();
				urlConnection.connect();
				contentLength = urlConnection.getContentLength();
				contentType = urlConnection.getContentType();
				last_modified = urlConnection.getLastModified();
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
			System.out.println("http");
			Socket socket;
			try {
				System.out.println(hostName+" "+portNumber);
				socket = new Socket(InetAddress.getByName(hostName), portNumber);
				//send HEAD request
				PrintWriter pw = new PrintWriter(socket.getOutputStream());
				pw.println("GET "+path+" "+"HTTP/1.0");
				pw.println("Host: "+hostName);
				pw.println("User-Agent:cis455crawler");
				pw.println("");
				pw.flush();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String s;
				while((s = br.readLine()) != null){
					if(s.equals("")) break;
				}
				StringBuilder sb = new StringBuilder();
				while((s = br.readLine()) != null){
					sb.append(s);
					sb.append("\n");
				}
//				char[] body = new char[contentLength];
//				br.read(body, 0, contentLength);
				String responseBody = new String(sb);
				return new ByteArrayInputStream(responseBody.getBytes());
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} 
		}
		return null;
	}
	
	public boolean processInitialLine(String s){
		Pattern p = Pattern.compile("HTTP/1.0 (\\d{3}) .*");
		Matcher m = p.matcher(s);
		if(m.find()){
			int status_code = Integer.parseInt(m.group(1));
			if(status_code<400) return true;
		}
		return false;
	}
	
	public boolean isValid(int maxSize){
		return isValidType() && isValidLength(maxSize);
	}
	
	public boolean isValidType(){
		if(contentType.startsWith("text/html")) return true;
		if(contentType.startsWith("application/xml")) return true;
		if(contentType.startsWith("text/xml")) return true;
		if(contentType.endsWith("+xml")) return true;
		return false;
	}
	
	public boolean isValidLength(int maxSize){
		if(contentLength > maxSize*1024*1024) return false;
		return true;
	}
	
	public long getLastModifiedTime(){
		return last_modified;
	}
	
	public String getContentType(){
		return contentType;
	}
	
	public int getContentLength(){
		return contentLength;
	}
	
	


	public static void main(String[] args) throws IOException {
		String url = "http://www.tutorialspoint.com/java/java_regular_expressions.htm";
		Client client = new Client(url);
		client.executeHEAD();
		InputStream inputStream = client.executeGET();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String s;
		while((s = br.readLine())!=null) System.out.println(s);
//		for(String link:client.extractLink()){
//			System.out.println(link);
//		}
	}
		
}
