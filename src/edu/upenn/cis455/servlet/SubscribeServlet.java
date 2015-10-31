package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class SubscribeServlet extends HttpServlet{
	
//	private static final String filepath = "/home/cis455/workspace_ee";
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		HttpSession session = request.getSession(false);
		PrintWriter out = response.getWriter();
		if(session == null){
			out.println("<html>");
			out.println("<body>");
			out.println("You must login!");
			out.println("</body>");
			out.println("</html>");
		}
		else{
			String filepath = getServletContext().getInitParameter("BDBstore");
			DBWrapper db = new DBWrapper(filepath);
			db.setup();
			String username = (String)session.getAttribute("username");
			String owner = request.getParameter("owner");
			String channelname = request.getParameter("channelname");
			User user = db.getUser(username);
			db.addSubscribe(username, owner, channelname);
			db.sync();
			response.sendRedirect("user");
		}
		
		
		
		
	}
}
