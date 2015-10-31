package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class SignupServlet extends HttpServlet{
	
//	private static final String filepath = "/home/cis455/workspace_ee";
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String filepath = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(filepath);
		db.setup();
		HttpSession session = request.getSession(true);
		
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		User user = db.getUser(username);
		System.out.println(user);
		if(user==null){
			//successfully create a new account
			session.setAttribute("signup", "successful");
			session.setAttribute("username", username);
			db.putUser(username, password);
			db.sync();
			response.sendRedirect("user");
		}
		else{
			//username has already been used
			session.setAttribute("signup", "fail");
			response.sendRedirect("signup");
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		HttpSession session = request.getSession(true);
	    PrintWriter out = response.getWriter();
	    out.println("<html>");
	    out.println("<body>");
	    out.println("<h2>Sign Up</h2>");
	    if(session.getAttribute("signup")!=null && session.getAttribute("signup").equals("fail")){
	    	out.println("<font color=\"red\">User has already exist.</font><br>");
	    }
	    out.println("<form action=\"signup\" method=\"post\">");
	    out.println("Username: <input name = \"username\"/></br>");
	    out.println("Password: <input type = \"password\" name = \"password\"/></br>");
	    out.println("<input type=\"submit\" value = \"Signup\"/>");
	    out.println("</body>");
	    out.println("</html>");
	    out.close();
	}

}
