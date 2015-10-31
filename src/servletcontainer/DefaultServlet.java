package servletcontainer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.util.HashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class DefaultServlet extends HttpServlet{

	static HashMap<String, String> contentType = new HashMap<String, String>();	
	{
		contentType.put("jar", "application/java-archive");
		contentType.put("pdf", "application/pdf");
		contentType.put("xls", "application/vnd.ms-excel");
		contentType.put("jpeg", "image/jpeg");
		contentType.put("jpg", "image/jpeg");
		contentType.put("jpe", "image/jpeg");
		contentType.put("png", "image/png");
		contentType.put("gif", "image/gif");
		contentType.put("txt", "text/plain");
		contentType.put("html", "text/html");
	}
	public static byte[] readFile(File file){		
		try {
			byte[] body = new byte[(int)file.length()];
			BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
			bin.read(body, 0, body.length);
			bin.close();
			return body;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		
	}
	
	public void doHead(HttpServletRequest req, HttpServletResponse res) throws java.io.IOException{
		HttpRequest request = (HttpRequest)req;
		HttpResponse response = (HttpResponse)res;
		if(request.isValidRequest()){
			
			if(request.isControl()){
				response.setContentType("text/html");
				response.setStatus(200);
				response.setContentLength(0);
				PrintWriter out = response.getWriter();
				out.close();
			}
			else if(request.isShutDown()){
				response.setContentType("text/html");
				response.setStatus(200);
				request.setStopped();
				response.setContentLength(0);
				synchronized(request.taskQueue){
					  request.taskQueue.notifyAll();
					  request.serverSocket.close();
				}
			}
			else if(request.isValidPath() && request.isValidDirectory()){
				response.setContentType("text/html");
			    response.setStatus(200);
			    PrintWriter out = response.getWriter();
			    File[] filelists = request.getPath().listFiles();
			    int count = 0;
			    for(int i=0;i<filelists.length;i++){
			    	count+=filelists[i].getAbsolutePath().length();
			    }
			    response.setContentLength(count);
			    out.close();
			}
			else if(request.isValidPath() && request.isValidFile()){
				String contenttype = contentType.get(request.getExtension());
				if(contenttype == null) response.setContentType("text/plain");
				else response.setContentType(contenttype);
			    response.setStatus(200);			    
			    File file = request.getPath();
			    byte[] body = readFile(file);
			    response.setContentLength(body.length);
			    ServletOutputStream out = response.getOutputStream();
			    out.close();
			}
			else{
				response.sendError(404);
			}
		}
		else{
			response.sendError(404);
		}
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws java.io.IOException{
		HttpRequest request = (HttpRequest)req;
		HttpResponse response = (HttpResponse)res;
		if(request.isValidRequest()){
			if(request.getProtocol().equals("HTTP/1.1") && request.getHeaders("host") == null){
				response.sendError(400);
			}
			else if(request.isControl()){
				response.setContentType("text/html");
				response.setStatus(200);
				PrintWriter out = response.getWriter();
				out.write("<html><head><title>Page Title</title></head>");
				out.write("<body>");
				out.write("Full Name: Wu Jingyuan<br>");
				out.write("SEAS Login: wujingyu<br>");
				for(Worker t:request.threadPool){
					String description = "";			
					if(t.currentTask() == null || t.currentTask().getRequest() == null || t.currentTask().getRequest().getPath() == null) {
						description = "Waiting";
					}
					else{
						State state = t.getState(); 
						switch(state){
						case RUNNABLE: description = t.currentTask().getRequest().getPath().getAbsolutePath(); break;
						case BLOCKED: description = "Waiting"; break;
						case WAITING: description = "Waiting"; break;
						case TIMED_WAITING: description = t.currentTask().getRequest().getPath().getAbsolutePath(); break;
						case TERMINATED: description = "terminated"; break;
						default: description = "Others"; break;
						}
					}			
					out.write(t.getName()+"  "+description+"<br>");
				}
				out.write("<a href=\"/shutdown\"><button type=\"button\">Shut Down Server</button></a>");
				out.write("</body></html>");
				out.close();
			}
			else if(request.isShutDown()){
				response.setContentType("text/html");
				response.setStatus(200);
				PrintWriter out = response.getWriter();
				out.write("<html><body>Server shut down</body></html>");
				out.close();
				request.setStopped();
				synchronized(request.taskQueue){
					  request.taskQueue.notifyAll();
					  request.serverSocket.close();
				}
			}
			else if(request.isValidPath() && request.isValidDirectory()){
				response.setContentType("text/html");
			    response.setStatus(200);
			    PrintWriter out = response.getWriter();
			    File[] filelists = request.getPath().listFiles();
				out.write("<html><body>");
				for(File f:filelists) out.write(f.getName()+"<br>");
				out.write("</body></html>");
			    out.close();
			}
			else if(request.isValidPath() && request.isValidFile()){
				String contenttype = contentType.get(request.getExtension());
				if(contenttype == null) response.setContentType("text/plain");
				else response.setContentType(contenttype);
			    response.setStatus(200);			    
			    File file = request.getPath();
			    byte[] body = readFile(file);
			    response.setContentLength(body.length);
			    ServletOutputStream out = response.getOutputStream();
			    out.write(body);
			    out.close();
			}
			else{
				response.sendError(404);
			}
		}
		else{
			response.sendError(404);
		}
		 
	}
}
