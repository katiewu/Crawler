package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;

public class ChannelsServlet extends HttpServlet{
//	private static final String filepath = "/home/cis455/workspace_ee";
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {	
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String filepath = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(filepath);
		db.setup();
		HttpSession session = request.getSession(false);
		List<Channel> channels = db.getAllChannels();
		out.println("<html>");
		out.println("<body>");
		out.println("<h2>Channels:</h2>");
		for(int i=0;i<channels.size();i++){
			int index = i+1;
			if(session.getAttribute("username") == null || session.getAttribute("username") == null){
				out.println("Channel "+index+":   "+channels.get(i).getChannelName()
		    			+"&nbsp;"
		    			+",owned by "+channels.get(i).getUsername()
		    			+"&nbsp;"
		    			+"<a href =\"display?username="+channels.get(i).getUsername()
		    			+"&channelname="+channels.get(i).getChannelName()+"\">"
		    			+"Display Channel</a><br>");
			}
			else{
				String username = (String)session.getAttribute("username");
				out.println("Channel "+index+":   "+channels.get(i).getChannelName()
		    			+"&nbsp;"
		    			+",owned by "+channels.get(i).getUsername()
		    			+"&nbsp;"
		    			+"<a href =\"display?username="+channels.get(i).getUsername()
		    			+"&channelname="+channels.get(i).getChannelName()+"\">"
		    			+"Display Channel</a>"
		    			+"&nbsp;"
		    			+"<a href = \"subscribe?owner="+channels.get(i).getUsername()
		    			+"&channelname="+channels.get(i).getChannelName()+"\">"
		    			+"Subscribe Channel</a><br>");
			}
			
		}
		out.println("</body>");
		out.println("</html>");
		out.close();
	}
}
