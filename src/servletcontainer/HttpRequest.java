package servletcontainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class HttpRequest implements HttpServletRequest {

	public Socket clientSocket;
	private BufferedReader in;
	private String request_method;
	private String protocol;
	private String url;
	private String hostname;
	private String absolute_path;
	private String path;
	private String query;
	private StopSign stopSign;
	Session session;
	TaskQueue taskQueue;
	ServerSocket serverSocket;
	Worker[] threadPool;
	private HashMap<String, String[]> queries = new HashMap<String, String[]>();;
	private HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
	private Hashtable<String, Object> attributes = new Hashtable<String, Object>();
	private boolean isValidRequest = true;
	private HashMap<String, Session> sessionPool;
	ScheduledExecutorService scheduledExecutorService;

	public HttpRequest(Socket clientSocket, HttpServer httpServer) {
		this.clientSocket = clientSocket;
		this.absolute_path = httpServer.absolutepath;
		this.sessionPool = httpServer.sessionPool;
		this.stopSign = httpServer.stopSign;
		this.taskQueue = httpServer.taskQueue;
		this.serverSocket = httpServer.serverSocket;
		this.threadPool = httpServer.threadpool;
		this.scheduledExecutorService = httpServer.scheduledExecutorService;
		try {
			in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			parseInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			isValidRequest = false;
		}
	}

	void setStopped() {
		stopSign.setStopped();
	}

	public boolean isValidRequest() {
		return isValidRequest;
	}

	private String decodeString(String s) throws UnsupportedEncodingException {
		String result = java.net.URLDecoder.decode(s, "UTF-8");
		return result;
	}

	private void parseInputStream() {
		try {
			String s;
			int count = 0;
			while ((s = in.readLine()) != null) {
				System.out.println(s);
				if (s.equals(""))
					break;
				// determine the first line
				if (count == 0) {
					if (s.contains("HTTP/1.0")) {
						String pattern_0 = "^(GET|POST|HEAD) (/.*) (HTTP/1\\.0)$";
						Pattern r = Pattern.compile(pattern_0);
						Matcher m = r.matcher(s);
						if (m.find()) {
							request_method = m.group(1);
							url = m.group(2);
							protocol = m.group(3);
							parseURL();
						}
					} else if (s.contains("HTTP/1.1")) {
						String pattern_1 = "^(GET|POST|HEAD|PUT|DELETE|TRACE|OPTIONS|CONNECTION|PATCH) (/.*) (HTTP/1\\.1)$";
						Pattern r = Pattern.compile(pattern_1);
						Matcher m = r.matcher(s);
						if (m.find()) {
							request_method = m.group(1);
							url = m.group(2);
							protocol = m.group(3);
							parseURL();
						}
					} else {
						isValidRequest = false;
						return;
					}
				} else {
					String pattern = "^(.+?): (.+)$";
					Pattern r = Pattern.compile(pattern);
					Matcher m = r.matcher(s);
					if (m.find()) {
						String header = m.group(1);
						String value = m.group(2);
						if (!headers.containsKey(header.toLowerCase())) {
							headers.put(header.toLowerCase(),
									new ArrayList<String>());
						}
						headers.get(header.toLowerCase()).add(value);
					}
				}
				count++;
			}
			if (count == 0) {
				isValidRequest = false;
			} else
				parseBody();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isValidRequest = false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			isValidRequest = false;
		}

	}

	private void parseBody() throws IOException {
		if(headers.containsKey("content-length")){
			int bodysize = Integer.parseInt(headers.get("content-length").get(0));
			if(bodysize>0){
				char[] body = new char[bodysize];
				in.read(body, 0, bodysize);
				String bodystr = java.net.URLDecoder.decode(new String(body), "UTF-8");
				System.out.println(bodystr);
				parseQuery(bodystr);
			}
			
		}
	}

	private void parseURL() {
		String pattern = "^(http://.+?)?(/.*?)(\\?.*?)?(#.*?)?$";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(url);
		if (m.find()) {
			hostname = m.group(1);
			path = m.group(2);
			query = m.group(3);
			if (query != null)
				query = query.substring(1);
			parseQuery(query);
			// fragment = m.group(4);
		} else {
			isValidRequest = false;
		}
	}

	private void parseQuery(String query) {
		if (query != null) {
			HashMap<String, List<String>> buffer = new HashMap<String, List<String>>();
			String[] querylist = query.split("&");
			for (String q : querylist) {
				String[] qq = q.split("=");
				if (qq.length != 2) {
					isValidRequest = false;
					return;
				}
				try {
					qq[0] = decodeString(qq[0]);
					qq[1] = decodeString(qq[1]);
				} catch (UnsupportedEncodingException e) {
					isValidRequest = false;
					return;
				}
				if (buffer.containsKey(qq[0])) {
					buffer.get(qq[0]).add(qq[1]);
				} else {
					List<String> values = new ArrayList<String>();
					values.add(qq[1]);
					buffer.put(qq[0], values);
				}
			}
			// convert arraylist to array
			for (String key : buffer.keySet()) {
				List<String> values = buffer.get(key);
				String[] result = values.toArray(new String[values.size()]);
				queries.put(key, result);
			}
		}
	}

	boolean isControl() {
		return path.equals("/control");
	}

	boolean isShutDown() {
		return path.equals("/shutdown");
	}

	boolean isValidPath() {
		String fpath = absolute_path + path;
		File file = new File(fpath);
		try {
			return file.getCanonicalPath().contains(absolute_path);
		} catch (IOException e) {
			return false;
		}
	}

	File getPath() {
		String fpath = absolute_path + path;
		File file = new File(fpath);
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			return null;
		}
	}

	String getExtension() {
		if (isValidFile()) {
			String fpath = absolute_path + path;
			File file = new File(fpath);
			String filename = file.getName();
			int index = filename.lastIndexOf('.');
			if (index == 0)
				return null;
			if (index == -1)
				return null;
			if (index < filename.length() - 1)
				return filename.substring(index + 1);
		}
		return null;
	}

	boolean isValidDirectory() {
		String fpath = absolute_path + path;
		File file = new File(fpath);
		try {
			return file.getCanonicalFile().isDirectory();
		} catch (IOException e) {
			return false;
		}
	}

	boolean isValidFile() {
		String fpath = absolute_path + path;
		File file = new File(fpath);
		try {
			return file.getCanonicalFile().isFile();
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public Object getAttribute(String arg0) {
		if (attributes.containsKey(arg0)) {
			return attributes.get(arg0);
		}
		return null;
	}

	@Override
	public Enumeration getAttributeNames() {
		return attributes.keys();
	}

	@Override
	public String getCharacterEncoding() {
		return "ISO-8859-1";
	}

	@Override
	public int getContentLength() {
		if (headers.containsKey("content-length")) {
			return Integer.parseInt(headers.get("content-length").get(0));
		}
		return -1;
	}

	@Override
	public String getContentType() {
		if (headers.containsKey("content-type")) {
			return headers.get("content-type").get(0);
		}
		return null;
	}

	@Override
	public String getLocalAddr() {
		return clientSocket.getLocalAddress().getHostAddress();
	}

	@Override
	public String getLocalName() {
		return clientSocket.getLocalAddress().getHostName();
	}

	@Override
	public int getLocalPort() {
		return clientSocket.getLocalPort();
	}

	@Override
	public Locale getLocale() {
		if (headers.containsKey("accept-language")) {
			String s = headers.get("accept-language").get(0);
			String[] r = s.split("-");
			return new Locale(r[0], r[1]);
		}
		return null;
	}

	@Override
	public String getParameter(String arg0) {
		String[] values = getParameterValues(arg0);
		if (values.length >= 1)
			return values[0];
		return null;
	}

	@Override
	public Map getParameterMap() {
		return queries;
	}

	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(queries.keySet());
	}

	@Override
	public String[] getParameterValues(String arg0) {
		if (arg0 == null || arg0.length() == 0)
			return null;
		if (queries.containsKey(arg0)) {
			return queries.get(arg0);
		}
		return null;
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	// deprecated
	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return clientSocket.getRemoteSocketAddress().toString();
	}

	@Override
	public String getRemoteHost() {
		if (headers.containsKey("host")) {
			return headers.get("host").get(0);
		}
		return null;
	}

	@Override
	public int getRemotePort() {
		return clientSocket.getPort();
	}

	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		return "HttpServer";
	}

	@Override
	public int getServerPort() {
		return clientSocket.getLocalPort();
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		if (attributes.containsKey(arg0)) {
			attributes.remove(arg0);
		}
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAuthType() {
		return BASIC_AUTH;
	}

	@Override
	public String getContextPath() {
		// servlets in the root context
		return "";
	}

	@Override
	public Cookie[] getCookies() {
		if (headers.containsKey("cookie")) {
			List<Cookie> cookies = new ArrayList<Cookie>();
			for (String s : headers.get("cookie")) {
				String[] slist = s.split(";");
				for (String c : slist) {
					String[] clist = c.split("=");
					Cookie cookie = new Cookie(clist[0], clist[1]);
					cookies.add(cookie);
				}
			}
			return cookies.toArray(new Cookie[cookies.size()]);
		}
		return null;
	}

	@Override
	public long getDateHeader(String arg0) {
		if (headers.containsKey("date")) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"EEE, d MMM yyyy HH:mm:ss z");
			String dateInString = "Tue, 15 Nov 1994 08:12:31 GMT";
			Date date;
			try {
				date = formatter.parse(dateInString);
				long milliseconds = date.getTime();
				return milliseconds;
			} catch (ParseException e) {
				return -1;
			}
		}
		return -1;
	}

	@Override
	public String getHeader(String arg0) {
		if (headers.containsKey(arg0)) {
			return headers.get(arg0).get(0);
		}
		return null;
	}

	@Override
	public Enumeration getHeaderNames() {
		return Collections.enumeration(headers.keySet());
	}

	@Override
	public Enumeration getHeaders(String arg0) {
		if (headers.containsKey(arg0)) {
			return Collections.enumeration(headers.get(arg0));
		}
		return null;
	}

	@Override
	public int getIntHeader(String arg0) {
		if (headers.containsKey(arg0)) {
			return Integer.parseInt(headers.get(arg0).get(0));
		}
		return -1;
	}

	@Override
	public String getMethod() {
		return request_method;
	}

	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString() {
		return query;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		return path;
	}

	@Override
	public StringBuffer getRequestURL() {
		StringBuffer s = new StringBuffer();
		s.append("http://" + clientSocket.getLocalAddress().getHostName());
		s.append(":" + clientSocket.getLocalPort());
		if (path != null)
			s.append(path);
		return s;
	}

	@Override
	public String getRequestedSessionId() {
		// find from URL
		for (String s : queries.keySet()) {
			if (s.equals("JSESSIONID")) {
				return queries.get(s)[0];
			}
		}
		// find from Cookie
		Cookie[] cookies = getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("JSESSIONID")) {
				return cookie.getValue();
			}
		}
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession() {
		String value = null;
		// find from URL
		for (String s : queries.keySet()) {
			if (s.equals("JSESSIONID")) {
				session = sessionPool.get(queries.get(s)[0]);
				if (session != null) {
					session.setLastAccessedTime(System.currentTimeMillis());
					session.sessionTask.cancel();
					session.sessionTask = new SessionTask(session, sessionPool);
					scheduledExecutorService.schedule(session.sessionTask,
							session.getMaxInactiveInterval(), TimeUnit.SECONDS);
					return session;
				}
			}
		}
		// find from Cookie
		Cookie[] cookies = getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("JSESSIONID")) {
					value = cookie.getValue();
					if (sessionPool.containsKey(value)) {
						session = sessionPool.get(value);
						if (session != null) {
							session.setLastAccessedTime(System
									.currentTimeMillis());
							session.sessionTask.cancel();
							session.sessionTask = new SessionTask(session,
									sessionPool);
							scheduledExecutorService.schedule(
									session.sessionTask,
									session.getMaxInactiveInterval(),
									TimeUnit.SECONDS);
							return session;
						}
					}
				}
			}
		}
		session = new Session(sessionPool, scheduledExecutorService);
		sessionPool.put(session.getId(), session);
		session.sessionTask = new SessionTask(session, sessionPool);
		scheduledExecutorService.schedule(session.sessionTask,
				session.getMaxInactiveInterval(), TimeUnit.SECONDS);
		return session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		String value = null;
		// find from URL
		for (String s : queries.keySet()) {
			if (s.equals("JSESSIONID")) {
				session = sessionPool.get(queries.get(s)[0]);
				if (session != null) {
					session.setLastAccessedTime(System.currentTimeMillis());
					session.sessionTask.cancel();
					session.sessionTask = new SessionTask(session, sessionPool);
					scheduledExecutorService.schedule(session.sessionTask,
							session.getMaxInactiveInterval(), TimeUnit.SECONDS);
					return session;
				}
			}
		}
		// find from Cookie
		Cookie[] cookies = getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("JSESSIONID")) {
				value = cookie.getValue();
				if (sessionPool.containsKey(value)) {
					session = sessionPool.get(value);
					if (session != null) {
						session.setLastAccessedTime(System.currentTimeMillis());
						session.sessionTask.cancel();
						session.sessionTask = new SessionTask(session,
								sessionPool);
						scheduledExecutorService.schedule(session.sessionTask,
								session.getMaxInactiveInterval(),
								TimeUnit.SECONDS);
						return session;
					}
				}
			}
		}
		// not find the session
		if (create == true) {
			session = new Session(sessionPool, scheduledExecutorService);
			sessionPool.put(session.getId(), session);
			session.sessionTask = new SessionTask(session, sessionPool);
			scheduledExecutorService.schedule(session.sessionTask,
					session.getMaxInactiveInterval(), TimeUnit.SECONDS);
			return session;
		}
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		Session session;
		Cookie[] cookies = getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("JSESSIONID")) {
				String value = cookie.getValue();
				if (sessionPool.containsKey(value)) {
					session = sessionPool.get(value);
					if (session != null)
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		Session session;
		for (String s : queries.keySet()) {
			if (s.equals("JSESSIONID")) {
				session = sessionPool.get(queries.get(s)[0]);
				if (session != null) {
					session.setLastAccessedTime(System.currentTimeMillis());
					return true;
				}
			}
		}
		return false;
	}

	// deprecated
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return isRequestedSessionIdFromCookie()
				|| isRequestedSessionIdFromUrl();
	}

	// exception

	@Override
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
