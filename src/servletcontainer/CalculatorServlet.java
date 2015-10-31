package servletcontainer;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CalculatorServlet extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
  {
	  response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      if (request.getParameterMap().size() == 0) {
          BufferedReader bf = new BufferedReader(new FileReader(new File(
                  "index.html")));
          int noOfByte = 0;
          char[] buf = new char[1024];
          while ((noOfByte = bf.read(buf)) != -1) {
              out.println(buf);
          }
          out.flush();
      } else {
          int v1 = Integer.valueOf(request.getParameter("num1")).intValue();
          int v2 = Integer.valueOf(request.getParameter("num2")).intValue();
          out.println("<html><head><title>Foo</title></head>");
          out.println("<body>" + v1 + "+" + v2 + "=" + (v1 + v2)
                  + "</body></html>");
          response.flushBuffer();
      }
      out.close();
//    response.setContentType("text/html");
//    response.setStatus(200);
////    response.setDateHeader("Date", 1000000000000L);
//    HttpSession session = request.getSession();
//    session.setMaxInactiveInterval(60);
//    PrintWriter out = response.getWriter();
////    response.sendRedirect("/");
//    int v1 = Integer.valueOf(request.getParameter("num1")).intValue();
//    int v2 = Integer.valueOf(request.getParameter("num2")).intValue();
//    out.println("<html><head><title>Foo</title></head>");
//    out.println("<body>"+v1+"+"+v2+"="+(v1+v2)+"</body></html>");
//    out.close();
  }
}