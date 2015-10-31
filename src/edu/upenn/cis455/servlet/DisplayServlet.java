package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Webpage;

public class DisplayServlet extends HttpServlet {
//	private static final String filepath = "/home/cis455/workspace_ee";
	
	public List<String> findXPath(Channel channel){
		List<String> set = channel.getXPathset();
		String[] expressions = set.toArray(new String[set.size()]);
		for(String expression:expressions) System.out.println("expression"+expression);
		String filepath = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(filepath);
		db.setup();
		List<String> urls = db.ProcessXPath(expressions);
		return urls;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String username = request.getParameter("username");
		String channelname = request.getParameter("channelname");
		String filepath = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(filepath);
		db.setup();
		Channel channel = db.getChannel(username, channelname);
		if(channel == null){
			PrintWriter out = response.getWriter();
			out.println("<html>");
			out.println("<body>");
			out.println("Channel does not exist!");
			out.println("</body>");
			out.println("</html>");
		}
		else{
//			List<String> urls = findXPath(channel);
			HashSet<String> urls = channel.getMatchURLs();
			String stylesheetURL = channel.getStylesheetURL();
			PrintWriter out = response.getWriter();
			response.setContentType("text/xml");
			out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			out.println("<?xml-stylesheet type=\"text/xsl\" href=\""+stylesheetURL+"\"?>");
			out.println("<documentcollection>");
			for(String url:urls){
				Webpage webpage = db.getWebpage(url);
				long crawl_time = webpage.getCrawlTime();
				Date date = new Date(crawl_time);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				String crawl = sdf.format(date); 
				String location = webpage.getURL();
				byte[] content = webpage.getContent();
				out.println("<document crawled=\""+crawl+"\" location=\""+location+"\">");
				ByteArrayInputStream bis = new ByteArrayInputStream(content);
				BufferedReader reader = new BufferedReader(new InputStreamReader(bis));
		        StringBuilder sb = new StringBuilder();
		        String line;
		        while ((line = reader.readLine()) != null) {
		        	if(!line.startsWith("<?xml")){
		        		sb.append(line);
			            sb.append("\n");
		        	}
		        }
		        out.println(sb);
				out.println("</document>");
			}
			out.println("</documentcollection>");
		}
	}
}
