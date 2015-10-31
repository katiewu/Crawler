package servletcontainer;


import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class HttpConfig implements ServletConfig{

	private String name;
	private HttpContext context;
	private HashMap<String,String> initParams;
	
	public HttpConfig(String name, HttpContext context) {
		this.name = name;
		this.context = context;
		initParams = new HashMap<String,String>();
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
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public String getServletName() {
		return name;
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}

}
