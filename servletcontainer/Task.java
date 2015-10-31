package servletcontainer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class Task {
	Socket clientSocket;
	HashMap<String, HttpServlet> servlets;
	HashMap<String, Session> sessionPool;
	HttpRequest request;
	HttpResponse response;
	StopSign stopSign;
	HttpServer httpServer;
	String absolute_path;

	public Task(Socket clientSocket, HttpServer httpServer) {
		this.clientSocket = clientSocket;
		this.httpServer = httpServer;
		this.servlets = httpServer.servlets;
		this.sessionPool = httpServer.sessionPool;
		this.stopSign = httpServer.stopSign;
		this.absolute_path = httpServer.absolutepath;
	}
	
	public String getServletPath(){
		String uri = request.getRequestURI();
		for (String url : servlets.keySet()) {
			if(url.equals("default")) continue;
			String path = url;
			// match ., convert . to \\.
			String pattern2 = "(.*)(\\.)(.+)$";
			Pattern patt2 = Pattern.compile(pattern2);
			Matcher matcher2 = patt2.matcher(path);
			if (matcher2.find()) {
				path = matcher2.group(1) + "\\." + matcher2.group(3);
			}
			// match *, convert * to .*
			String pattern1 = "(.*)(\\*)(.*)";
			Pattern patt1 = Pattern.compile(pattern1);
			Matcher matcher1 = patt1.matcher(path);
			if (matcher1.find()) {
				path = matcher1.group(1) + ".*" + matcher1.group(3);
			}
//			path = request.getContextPath() + path;
			path = "^"+path+"$";
			Pattern patt = Pattern.compile(path);
			Matcher matcher = patt.matcher(uri);
			if (matcher.matches()) {
				return url;
			}
			if(url.endsWith("/*")){
				String special_url = url.substring(0, url.length()-2);
				if(special_url.equals(uri)) return url;
			}
		}
		return null;
	}

	public void process() throws ServletException, IOException {
		request = new HttpRequest(clientSocket, httpServer);
		response = new HttpResponse(request);
		if (request.isValidRequest()) {
			String url = getServletPath();
			if(url != null){
				HttpServlet servlet = servlets.get(url);
				servlet.service(request, response);
			}
			else{
				HttpServlet defaultServlet = servlets.get("default");
				defaultServlet.service(request, response);
			}
		}
		else{
//			defaultServlet.service(request, response);
			clientSocket.close();
		}
	}
	
	HttpRequest getRequest(){
		return request;
	}

}
