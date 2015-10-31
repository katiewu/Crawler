package test.edu.upenn.cis455;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import junit.framework.TestCase;

public class XPathEngineImplTest extends TestCase {
	XPathEngine XPathHandler;
	String[] expressions;

	protected void setUp() throws Exception {
		XPathHandler = XPathEngineFactory.getXPathEngine();
		expressions = new String[]{"/rss/channel/item/title[contains(text(),\"war\")]"};
		XPathHandler.setXPaths(expressions);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try{
			InputStream xmlInput = new FileInputStream("test.xml");
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(xmlInput, (DefaultHandler)XPathHandler);
		}
		catch(Throwable err){
			err.printStackTrace();
		}
	}
	
	public void testIsValid() {
		assertEquals(XPathHandler.isValid(0), true);
	}
	
	public void testEvaluate(){
		assertEquals(XPathHandler.evaluate(null)[0], true);
//		assertEquals(XPathHandler.evaluate(null)[1], true);
//		assertEquals(XPathHandler.evaluate(null)[2], true);
//		assertEquals(XPathHandler.evaluate(null)[3], true);
//		assertEquals(XPathHandler.evaluate(null)[4], true);
	}

}
