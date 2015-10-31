package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import edu.upenn.cis455.crawler.info.HttpClient;
import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		System.out.println("get post");
		response.setContentType("text/html");
		String queries = request.getParameter("Xpath");
		String[] expressions = queries.split(";");
		String xmlfile = request.getParameter("xmlfile");
		boolean[] result = getResult(queries, xmlfile);	
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<body>");
		if(result == null){
			out.println("Please check the link to the XML file or XPath");		
		}
		else{
			out.println("XML file: "+xmlfile+"<br>");
			for(int i=0;i<result.length;i++){
				out.println("XPath: "+expressions[i]+"\t"+result[i]+"<br>");
			}	
		}
		out.println("</body>");
		out.println("</html>");
		out.close();
	}
	
	public boolean[] getResult(String queries, String xmlfile){
		try{
			String[] expressions = queries.split(";");
			HttpClient client = new HttpClient();
			InputStream xmlInput = client.execute(xmlfile);
			if(xmlInput == null) return null;
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XPathEngine XPathHandler = XPathEngineFactory.getXPathEngine();
			XPathHandler.setXPaths(expressions);
			saxParser.parse(xmlInput, (DefaultHandler)XPathHandler);
			return XPathHandler.evaluate(null);		
		} catch(Throwable err){
			err.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    out.println("<html>");
	    out.println("<body>");
	    out.println("full name: Wu Jingyuan<br>");
	    out.println("seas login: wujingyu<br>");
	    out.println("<form action=\"\" method=\"post\">");
	    out.println("Xpath: <input name = \"Xpath\"/></br>");
	    out.println("XML file: <input name = \"xmlfile\"/></br>");
	    out.println("<input type=\"submit\" value = \"Submit\"/>");
	    out.println("</form>");
	    out.println("</body>");
	    out.println("</html>");
	    out.close();
	}

}









