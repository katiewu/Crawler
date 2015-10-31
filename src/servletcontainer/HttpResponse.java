package servletcontainer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class HttpResponse implements HttpServletResponse {
	
	HttpRequest httpRequest;
	Socket clientSocket;
	PrintBufferedWriter out;
	List<Cookie> cookies = new ArrayList<Cookie>();
	HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
	String characterEncoding = "ISO-8859-1";
	int statusCode = 200;
	int contentLength;
	String contentType = "text/html";
	Locale locale;
	String headerbody;
	Session session;
	boolean isCommitted = false;

	HashMap<Integer, String> statusMap = new HashMap<Integer, String>();
	
	{
		statusMap.put(100, "Continue");
		statusMap.put(101, "Switching Protocols");
		statusMap.put(102, "Processing");
		statusMap.put(200, "OK");
		statusMap.put(201, "Created");
		statusMap.put(202, "Accepted");
		statusMap.put(203, "Non-Authoritative");
		statusMap.put(204, "No Content");
		statusMap.put(205, "Reset Content");
		statusMap.put(206, "Partial Content");
		statusMap.put(207, "Multi-Status");
		statusMap.put(208, "Already Reported");
		statusMap.put(226, "IM Used");
		statusMap.put(300, "Multiple Choice");
		statusMap.put(301, "Moved Permanently");
		statusMap.put(302, "Found");
		statusMap.put(303, "See Other");
		statusMap.put(304, "Not Modified");
		statusMap.put(305, "Use Proxy");
		statusMap.put(306, "Switch Proxy");
		statusMap.put(307, "Temporary Redirect");
		statusMap.put(308, "Permanent Redirect");
		statusMap.put(400, "Bad Request");
		statusMap.put(401, "Unauthorized"	);
		statusMap.put(402, "Payment Required"	);
		statusMap.put(403, "Forbidden"	);
		statusMap.put(404, "Not Found"	);
		statusMap.put(405, "Method Not Allowed"	);
		statusMap.put(406, "Not Acceptable"	);
		statusMap.put(407, "Proxy Authentication Required"	);
		statusMap.put(408, "Request Timeout"	);
		statusMap.put(409, "Conflict"	);
		statusMap.put(410, "Gone"	);
		statusMap.put(411, "Length Required"	);
		statusMap.put(412, "Precondition Failed"	);
		statusMap.put(413, "Request Entity Too Large"	);
		statusMap.put(414, "Request-URI Too Long"	);
		statusMap.put(415, "Unsupported Media Type"	);
		statusMap.put(416, "Requested Range Not Satisfiable"	);
		statusMap.put(417, "Expectation Failed"	);
		statusMap.put(500, "Internal Server Error"	);
		statusMap.put(501, "Not Implemented"	);
		statusMap.put(502, "Bad Gateway"	);
		statusMap.put(503, "Service Unavailable"	);
		statusMap.put(504, "Gateway Timeout"	);
		statusMap.put(505, "HTTP Version Not Supported"	);
		statusMap.put(511, "Network Authentication Required"	);
	}
	
	
	public HttpResponse(HttpRequest httpRequest){
		this.httpRequest = httpRequest;
		this.session = httpRequest.session;
		this.clientSocket = httpRequest.clientSocket;
	}
	
	@Override
	public void flushBuffer() throws IOException {
		out.flush();
	}

	@Override
	public int getBufferSize() {
		return out.getBufferSize();
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new HttpOutputStream(clientSocket);
	}
	

	@Override
	public PrintWriter getWriter() throws IOException {
		if(out!=null) return out;
		this.out = new PrintBufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()), httpRequest, this);
		return out;
	}

	@Override
	public boolean isCommitted() {	
		return isCommitted;
	}

	@Override
	public void reset() {
		if(out.isWriteOut()) throw new IllegalStateException();
		else out.reset();		
	}

	@Override
	public void resetBuffer() {
		if(out.isWriteOut()) throw new IllegalStateException();
		else out.resetBuffer();
	}

	@Override
	public void setBufferSize(int arg0) {
		if(out.isBufferWrite()) throw new IllegalStateException();
		else out.setBufferSize(arg0);
	}

	@Override
	public void setCharacterEncoding(String en) {
		characterEncoding = en;
		addHeader("Content-Encoding", en);
	}

	@Override
	public void setContentLength(int arg0) {
		contentLength = arg0;
		addHeader("Content-Length", String.valueOf(arg0));
	}

	@Override
	public void setContentType(String arg0) {
		contentType = arg0;
		addHeader("Content-Type", arg0);
	}

	@Override
	public void setLocale(Locale arg0) {
		locale = arg0;
		addHeader("Content-Language", arg0.toLanguageTag());
	}

	@Override
	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
		addHeader("Set-Cookie", cookie.getName()+"="+cookie.getValue());
	}

	private String longToDateString(long time) {
        Date date=new Date(time);
	    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(date.getTime());
	}
	
	@Override
	public void addDateHeader(String header, long time) {
		addHeader(header, longToDateString(time));
	}

	@Override
	public void addHeader(String header, String value) {
		if(!headers.containsKey(header)) headers.put(header, new ArrayList<String>());
		headers.get(header).add(value);
	}

	@Override
	public void addIntHeader(String header, int value) {
		addHeader(header, Integer.toString(value));
	}

	@Override
	public boolean containsHeader(String header) {
		return headers.containsKey(header);
	}

	@Override
	public String encodeRedirectURL(String url) {
		if(session!=null){
			url = url+"?JSESSIONID="+session.getId();
		}
		return url;
	}
	
	// deprecated
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeURL(String url) {
		if(session!=null){
			url = url+"?JSESSIONID="+session.getId();
		}
		return url;
	}

	// deprecated
	public String encodeUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendError(int sc) throws IOException {
		if(isCommitted()) throw new IllegalStateException();
		out = (PrintBufferedWriter) getWriter();
		out.reset();
		List<String> cookieheader = out.headers.get("Set-Cookie");
		out.headers = new HashMap<String, List<String>>();
		if(cookieheader!=null) out.headers.put("Set-Cookie", cookieheader);
		out.headers.put("Content-Type", new ArrayList<String>());
		out.headers.get("Content-Type").add("text/html");
		setStatus(sc);
		out.println("<html><body>"+sc+" "+statusMap.get(sc)+"</body></html>");
		out.close();
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		if(isCommitted()) throw new IllegalStateException();
		out = (PrintBufferedWriter) getWriter();
		reset();
		List<String> cookieheader = out.headers.get("Set-Cookie");
		out.headers = new HashMap<String, List<String>>();
		if(cookieheader!=null) out.headers.put("Set-Cookie", cookieheader);
		out.headers.put("Content-Type", new ArrayList<String>());
		out.headers.get("Content-Type").add("text/html");
		setStatus(sc);
		out.println("<html><body>"+msg+"</body></html>");
		out.close();
	}

	@Override
	public void sendRedirect(String loc) throws IOException {
		if(isCommitted()) throw new IllegalStateException();
		out = (PrintBufferedWriter) getWriter();
		reset();
		List<String> cookieheader = out.headers.get("Set-Cookie");
		out.headers = new HashMap<String, List<String>>();
		if(cookieheader!=null) out.headers.put("Set-Cookie", cookieheader);
		out.headers.put("Content-Type", new ArrayList<String>());
		out.headers.get("Content-Type").add("text/html");
		out.headers.put("Location", new ArrayList<String>());
		if(loc.startsWith("/")) out.headers.get("Location").add(httpRequest.getContextPath()+loc);
		else out.headers.get("Location").add(httpRequest.getRequestURI()+"/"+loc);
		setStatus(SC_FOUND);
		out.flush();
	}

	@Override
	public void setDateHeader(String header, long value) {
		setHeader(header, longToDateString(value));
	}

	@Override
	public void setHeader(String header, String value) {
		headers.remove(header);
		headers.put(header, new ArrayList<String>());
		headers.get(header).add(value);
	}

	@Override
	public void setIntHeader(String header, int value) {
		setHeader(header, Integer.toString(value));
	}
	
	public int getStatus(){
		return statusCode;
	}

	@Override
	public void setStatus(int sc) {
		statusCode = sc;
	}

	// deprecated
	@Override
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

}
