package test.edu.upenn.cis455;

import junit.framework.TestCase;
import edu.upenn.cis455.servlet.XPathServlet;

public class XPathServletTest extends TestCase {
	XPathServlet servlet;
	String queries;
	String xmlfile;
	protected void setUp() throws Exception {
		queries = "/note/to;/note";
		xmlfile = "http://www.w3schools.com/xml/note.xml";
		servlet = new XPathServlet();
	}
	
	public void testGetResult(){
		boolean[] result = servlet.getResult(queries, xmlfile);
		assertEquals(result[0], true);
		assertEquals(result[1], true);
	}

}
