package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;

public class UserServlet extends HttpServlet{
//	private static final String filepath = "/home/cis455/workspace_ee";
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession(false);
		if(session == null || session.getAttribute("username") == null){
			out.println("<html>");
			out.println("<body>");
			out.println("You must login!");
			out.println("</body>");
			out.println("</html>");
		}
		else{
			String username = (String)session.getAttribute("username");
			String channelname = request.getParameter("channelname");
			String xpath = request.getParameter("xpath");
			String xsl = request.getParameter("xsl");
			String filepath = getServletContext().getInitParameter("BDBstore");
			DBWrapper db = new DBWrapper(filepath);
			db.setup();
			db.putChannel(username, channelname, xsl, xpath, new HashSet<String>());
			db.sync();
			response.sendRedirect("user");
		}
		
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(session == null || session.getAttribute("username") == null){
			out.println("<html>");
			out.println("<body>");
			out.println("You must login in!");
			out.println("</body>");
			out.println("</html>");
		}
		else{
			String filepath = getServletContext().getInitParameter("BDBstore");
			DBWrapper db = new DBWrapper(filepath);
			db.setup();
			String username = (String)session.getAttribute("username");
			out.println("<html>");
			out.println("<body>");
			out.println("<h1>Welcome, "+username+"</h1>");
			out.println("<h2>Create a new channel</h2>");
			out.println("<form action=\"user\" method=\"post\">");
			out.println("Channel Name: <br>");
			out.println("<input name = \"channelname\" size = \"70\" /><br>");
			out.println("XPath Expressions(separate by semicolon):<br>");
			out.println("<textarea cols = \"50\" rows = \"5\" name = \"xpath\"></textarea><br>");
			out.println("URL of XSL stylesheet: <br>");
			out.println("<input name = \"xsl\" size = \"70\" /><br>");
		    out.println("<input type=\"submit\" value = \"Create\"/>");
		    out.println("</form>");
		    out.println("<h2>Channels that you create</h2>");
		    List<Channel> channels = db.getUserChannels(username);
		    for(int i=0;i<channels.size();i++){
		    	int index = i+1;
		    	out.println("Channel "+index+":   "+channels.get(i).getChannelName()
		    			+"&nbsp;"
		    			+"<a href =\"display?username="+channels.get(i).getUsername()
		    			+"&channelname="+channels.get(i).getChannelName()+"\">"
		    			+"Display Channel</a>"
		    			+"&nbsp;"
		    			+"<a href =\"delete?channelname="+channels.get(i).getChannelName()
		    			+"&username="+channels.get(i).getUsername()+"\">"
		    			+"Delete Channel</a><br>");
		    }
		    out.println("<h2>Channels that you subscribe</h2>");
		    List<String> subscribes = db.getUser(username).getSubscribe();
		    if(subscribes != null){
		    	for(int i=0;i<subscribes.size();i++){
			    	int index = i+1;
			    	String[] channelinfo = subscribes.get(i).split(";");
			    	String owner = channelinfo[0];
			    	String channelname = channelinfo[1];
			    	out.println("Channel "+index+":   "+channelname
			    			+"&nbsp;"
			    			+"<a href =\"display?username="+owner
			    			+"&channelname="+channelname+"\">"
			    			+"&nbsp;"
			    			+"<a href = \"unsubscribe?channelname="+channelname
			    			+"&owner="+owner
			    			+"&username="+username+"\">"
			    			+"Unsubscribe</a><br>");
			    }
		    }    
		    out.println("<h2><a href = \"channels\"/>View all the channels</a></h2>");
		    out.println("<br><br>");
		    
		    out.println("<a href = \"logout\">Logout</a>");
		    out.println("</body>");
		    out.println("</html>");
		}
	}
}
