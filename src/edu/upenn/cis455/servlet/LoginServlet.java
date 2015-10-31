package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class LoginServlet extends HttpServlet{
	
//	private static final String filepath = "/home/cis455/workspace_ee";
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
		response.setContentType("text/html");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		HttpSession session = request.getSession(true);
		PrintWriter out = response.getWriter();
		String filepath = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(filepath);
		db.setup();
		User user = db.getUser(username);
		System.out.println("User: "+user);
		if(user!=null && user.getPassword().equals(password)){
			//successful login
			session.setAttribute("valid", "true");
			session.setAttribute("username", username);
			response.sendRedirect("user");
		}
		else{
			session.setAttribute("valid", "false");
			response.sendRedirect("");
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		HttpSession session = request.getSession(true);
	    PrintWriter out = response.getWriter();
	    out.println("<html>");
	    out.println("<body>");
	    out.println("<h3>name: Wu Jingyuan</h3>");
	    out.println("<h3>pennkey: wujingyu</h3>");
	    out.println("<h2>Sign in to your account</h2>");
	    if(session.getAttribute("valid")!=null && session.getAttribute("valid").equals("false")) {
	    	out.println("<font color=\"red\">The user does not exist!</font><br>"); 
	    }
	    out.println("<form action=\"\" method=\"post\">");
	    out.println("Username: <input name = \"username\"/><br>");
	    out.println("Password: <input type = \"password\" name = \"password\"/><br>");
	    out.println("<input type=\"submit\" value = \"Login\"/>");
	    out.println("</form>");
	    out.println("<h3>Don't have an account</h3>");
	    out.println("<form action=\"signup\" method=\"get\">");
	    out.println("<input type=\"submit\" value = \"Sign Up\"/>");
	    out.println("</form>");
	    out.println("<h3><a href=\"channels\">Check all the channels</a></h3>");
	    out.println("</body>");
	    out.println("</html>");
	    out.close();
	}

}
