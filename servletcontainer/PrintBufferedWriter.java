package servletcontainer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PrintBufferedWriter extends PrintWriter{
	
	private Writer out;

    private char cb[];
    private int nChars, nextChar;

    private static int defaultCharBufferSize = 8192;
    
    private Formatter formatter;
    private boolean trouble = false;
    private final String lineSeparator;
    private PrintStream psOut = null;
    private boolean isBufferWrite = false;
    private boolean isWriteOut = false;
    private boolean isCommitted = false;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    HashMap<String, List<String>> headers;
    
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
	
	public PrintBufferedWriter(Writer out, HttpRequest httpRequest, HttpResponse httpResponse){
		super(out);
		this.out = out;
        cb = new char[defaultCharBufferSize];
        nChars = defaultCharBufferSize;
        nextChar = 0;
        lineSeparator = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("line.separator"));
        this.headers = httpResponse.headers;
        if(httpRequest.session != null){
        	if(!headers.containsKey("Set-Cookie")){
        		headers.put("Set-Cookie", new ArrayList<String>());
        	}
        	headers.get("Set-Cookie").add("JSESSIONID="+httpRequest.session.sessionID);
        }
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
	}

	public int getBufferSize(){
		return nChars;
	}
	
	public void setBufferSize(int size){
		nChars = size;
		cb = new char[size];
	}
	
	public boolean isBufferWrite(){
		return isBufferWrite;
	}
	
	public boolean isWriteOut(){
		return isWriteOut;
	}
	
	public boolean isCommitted(){
		return isCommitted;
	}
	
	public void reset(){
		nextChar = 0;
	}
	
	public void resetBuffer(){
		nextChar = 0;
		
	}
	
	private String createHeaderBody(){
		StringBuffer headerBody = new StringBuffer();
		headerBody.append(httpRequest.getProtocol()+" "+httpResponse.getStatus()+" "+statusMap.get(httpResponse.getStatus())+"\r\n");
		for(String header:headers.keySet()){
			if(headers.get(header).size() != 0){
				StringBuffer headerline = new StringBuffer();
				headerline.append(header+": ");
				for(String value:headers.get(header)){
					headerline.append(value);
					if(header.equals("Set-Cookie")) headerline.append("; ");
					else headerline.append(", ");
				}
				headerBody.append(headerline.substring(0, headerline.length()-2));
				headerBody.append("\r\n");
			}			
		}
		headerBody.append("\r\n");
		String headerbody = new String(headerBody);
		return headerbody;
	}
	
	public PrintWriter append(char c){
		write(c);
        return this;
	}
	
	public PrintWriter append(CharSequence csq){
		if (csq == null)
            write("null");
        else
            write(csq.toString());
        return this;
	}
	
	public PrintWriter append(CharSequence csq, int start, int end){
		CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
	}
	
	public PrintWriter format(String format, Object ... args) {
        try {
            synchronized (lock) {
                ensureOpen();
                if ((formatter == null) || (formatter.locale() != Locale.getDefault()))
                    formatter = new Formatter(this);
                formatter.format(Locale.getDefault(), format, args);
                out.flush();
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        } catch (IOException x) {
            trouble = true;
        }
        return this;
    }
	
	public PrintWriter printf(String format, Object ... args) {
        return format(format, args);
    }
	
	public PrintWriter printf(Locale l, String format, Object ... args) {
        return format(l, format, args);
    }
	
	public void close() {
        try {
        	synchronized (lock) {
                if (out == null) {
                    return;
                }
                try (Writer w = out) {
                    flushBuffer();
                } finally {
                    out = null;
                    cb = null;
                }
            }
        }
        catch (IOException x) {
            trouble = true;
        }
    }
	
	public boolean checkError() {
        if (out != null) {
            flush();
        }
        if (out instanceof java.io.PrintWriter) {
            PrintWriter pw = (PrintWriter) out;
            return pw.checkError();
        } else if (psOut != null) {
            return psOut.checkError();
        }
        return trouble;
    }
	
	protected void setError() {
        trouble = true;
    }

    protected void clearError() {
        trouble = false;
    }
	
	/** Checks to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }
    
    public void flush() {
        try {
            synchronized (lock) {
            	if(!isCommitted){
            		out.write(createHeaderBody());
            		setCommitted();
            	}
                flushBuffer();
                out.flush();
                isWriteOut = true;
            }
        }
        catch (IOException x) {
            trouble = true;
        }
    }
    
    public void setCommitted(){
    	isCommitted = true;
    	httpResponse.isCommitted = true;
    }
	
    /**
     * Flushes the output buffer to the underlying character stream, without
     * flushing the stream itself.  This method is non-private only so that it
     * may be invoked by PrintStream.
     */
    void flushBuffer() {
    	try{
    		synchronized (lock) {
                ensureOpen();
                if (nextChar == 0)
                    return;
                if(!isCommitted){
            		out.write(createHeaderBody());
            		setCommitted();
            	}
                out.write(cb, 0, nextChar);
                isWriteOut = true;
                nextChar = 0;
            }
    	}
    	catch (IOException x){
    		trouble = true;
    	}
        
    }
    
	public void write(int c){
		try {
            synchronized (lock) {
            	ensureOpen();
            	isBufferWrite = true;
                if (nextChar >= nChars)
                    flushBuffer();
                cb[nextChar++] = (char) c;
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
	}
	
	public void write(String s){
		write(s, 0, s.length());
	}
	
	public void write(String s, int off, int len){
		char[] carr = s.toCharArray();
		write(carr, off, len);
	}
	
	public void write(char[] buf){
		write(buf, 0, buf.length);
	}
	
	private int min(int a, int b) {
        if (a < b) return a;
        return b;
    }
	
	public void write(char[] cbuf, int off, int len){
		try {
			synchronized (lock) {
	            ensureOpen();
	            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
	                ((off + len) > cbuf.length) || ((off + len) < 0)) {
	                throw new IndexOutOfBoundsException();
	            } else if (len == 0) {
	                return;
	            }
	            
	            isBufferWrite = true;
	            if (len >= nChars) {
	                /* If the request length exceeds the size of the output buffer,
	                   flush the buffer and then write the data directly.  In this
	                   way buffered streams will cascade harmlessly. */
	                flushBuffer();
	                out.write(cbuf, off, len);
	                return;
	            }

	            int b = off, t = off + len;
	            while (b < t) {
	                int d = min(nChars - nextChar, t - b);
	                System.arraycopy(cbuf, b, cb, nextChar, d);
	                b += d;
	                nextChar += d;
	                if (nextChar >= nChars)
	                    flushBuffer();
	            }
	        }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
		
	}
	
	public void	print(boolean b){
		write(b ? "true" : "false");
	}
	
	public void print(char c){
		write(c);
	}
	
	public void print(char[] s){
		write(s);
	}
	
	public void print(double d){
		write(String.valueOf(d));
	}
	
	public void print(float f){
		write(String.valueOf(f));
	}
	
	public void print(int i){
		write(String.valueOf(i));
	}
	
	public void print(long i){
		write(String.valueOf(i));
	}
	
	public void print(Object obj){
		write(String.valueOf(obj));
	}
	
	public void print(String s){
		if(s == null) s = "null";
		write(s);
	}
	
	private void newLine() {
        try {
            synchronized (lock) {
                ensureOpen();
                write(lineSeparator);
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }
	
	public void println(){
		newLine();
	}
	
	public void	println(boolean b){
		synchronized (lock) {
            print(b);
            println();
        }
	}
	
	public void println(char c){
		synchronized (lock) {
            print(c);
            println();
        }
	}
	
	public void println(char[] s){
		synchronized (lock) {
            print(s);
            println();
        }
	}
	
	public void println(double d){
		synchronized (lock) {
            print(d);
            println();
        }
	}
	
	public void println(float f){
		synchronized (lock) {
            print(f);
            println();
        }
	}
	
	public void println(int i){
		synchronized (lock) {
            print(i);
            println();
        }
	}
	
	public void println(long i){
		synchronized (lock) {
            print(i);
            println();
        }
	}
	
	public void println(Object obj){
		synchronized (lock) {
            print(obj);
            println();
        }
	}
	
	public void println(String s){
		synchronized (lock) {
            print(s);
            println();
        }
	}
	
	
	
}
