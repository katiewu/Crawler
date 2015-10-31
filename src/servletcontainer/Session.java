package servletcontainer;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class Session implements HttpSession{
	
	static int count;
	
	HashMap<String, Object> attributes = new HashMap<String, Object>();
	long creationTime;
	long lastAccessedTime;
	String sessionID;
	int MaxInactiveInterval = 60*5;
	SessionTask sessionTask= null;
	HashMap<String, Session> sessionPool;
	ScheduledExecutorService scheduledExecutorService;
	
	public Session(HashMap<String, Session> sessionPool, ScheduledExecutorService scheduledExecutorService){
		creationTime = System.currentTimeMillis();
		lastAccessedTime = creationTime;
		count++;
		sessionID = Integer.toString(count); 
		this.sessionPool = sessionPool;
		this.scheduledExecutorService = scheduledExecutorService;
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
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public String getId() {
		return sessionID;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	public void setLastAccessedTime(long time) {
		lastAccessedTime = time;
	}
	
	@Override
	public int getMaxInactiveInterval() {
		return MaxInactiveInterval;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	// deprecated
	@Override
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(String arg0) {
		if(attributes.containsKey(arg0)) return attributes.get(arg0);
		return null;
	}

	// deprecated
	@Override
	public String[] getValueNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invalidate() {
		attributes.clear();
	}

	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}

	// deprecated
	@Override
	public void putValue(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAttribute(String arg0) {
		attributes.remove(arg0);
	}

	// deprecated
	@Override
	public void removeValue(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}

	@Override
	public void setMaxInactiveInterval(int arg0) {
		MaxInactiveInterval = arg0;
		sessionTask.cancel();
		sessionTask = new SessionTask(this, sessionPool);
		scheduledExecutorService.schedule(sessionTask, arg0,TimeUnit.SECONDS);
		System.out.println(getMaxInactiveInterval()+" "+sessionTask);
	}

}
