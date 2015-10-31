package servletcontainer;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class HttpContext implements ServletContext{
	
	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	private HashMap<String,String> contentType = new HashMap<String,String>();
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
	
	public HttpContext(){
		attributes = new HashMap<String, Object>();
		initParams = new HashMap<String, String>();
	}
	
	
	@Override
	public Object getAttribute(String arg0) {
		if(attributes.containsKey(arg0)) return attributes.get(arg0);
		return null;
	}

	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public ServletContext getContext(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String arg0) {
		if(initParams.containsKey(arg0)) return initParams.get(arg0);
		return null;
	}

	@Override
	public Enumeration getInitParameterNames() {
		return Collections.enumeration(initParams.keySet());
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public String getMimeType(String arg0) {
		if(contentType.containsKey(arg0)) return contentType.get(arg0);
		return null;
	}

	@Override
	public int getMinorVersion() {
		return 4;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set getResourcePaths(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerInfo() {
		return "HttpServer";
	}
	
	// deprecated
	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletContextName() {
		return null;
	}

	// deprecated
	@Override
	public Enumeration getServletNames() {
		return null;
	}
	
	// deprecated
	@Override
	public Enumeration getServlets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(String msg) {
		System.err.println(msg);
	}

	// deprecated
	@Override
	public void log(Exception arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void log(String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace(System.err);
	}

	@Override
	public void removeAttribute(String arg0) {
		attributes.remove(arg0);	
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
