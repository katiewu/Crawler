package servletcontainer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimerTask;

public class SessionTask extends TimerTask{

	Session session;
	HashMap<String, Session> sessionPools;
	
	public SessionTask(Session session, HashMap<String, Session> sessionPools){
		this.session = session;
		this.sessionPools = sessionPools;
	}
	
	public void run() {
		System.out.println("sessionTask processing"+this);
		Calendar cal = Calendar.getInstance();
		long currentTime = cal.getTimeInMillis();
		long lastTime = session.getLastAccessedTime();
		int maxTime = session.getMaxInactiveInterval();
		int duration = (int)(currentTime-lastTime)/1000;
		if(duration>=maxTime){
			sessionPools.remove(session.getId());
		}
	}

}
