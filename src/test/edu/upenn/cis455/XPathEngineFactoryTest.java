package test.edu.upenn.cis455;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.helpers.DefaultHandler;

import edu.upenn.cis455.xpathengine.Query;
import edu.upenn.cis455.xpathengine.QueryIndex;
import edu.upenn.cis455.xpathengine.SaxHandler;
import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

public class XPathEngineFactoryTest {
	XPathEngine XPathHandler;
	String[] expressions;
	
	public void setup(){
		XPathHandler = XPathEngineFactory.getXPathEngine();
		expressions = new String[]{"/a[e]/b[c]/d", "/a[b/c]/e", "/bookstore/book[@category = \"COOKING\"][title[@lang = \"en\"]]/price"};
		XPathHandler.setXPaths(expressions);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try{
			InputStream xmlInput = new FileInputStream("test.html");
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(xmlInput, (DefaultHandler)XPathHandler);
		}
		catch(Throwable err){
			
		}
	}

	@Test
	public void testIsValid() {
		assertEquals(XPathHandler.isValid(0), false);
		assertEquals(XPathHandler.isValid(1), false);
		assertEquals(XPathHandler.isValid(1), true);
	}
	
	public void testEvaluate(){
		
	}

}
