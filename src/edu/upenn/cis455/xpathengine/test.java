package edu.upenn.cis455.xpathengine;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

public class test {
	
	public static void main(String[] args){
		String[] queries = {"/bookstore", "/bookstore/book", "/bookstore/book[@category = \"COOKING\"][title[@lang = \"en\"]]/price"};
		XPathEngine XPathHandler = XPathEngineFactory.getXPathEngine();
		XPathHandler.setXPaths(queries);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			InputStream xmlInput = new FileInputStream("test.xml");
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(xmlInput, (DefaultHandler)XPathHandler);
			System.out.println(XPathHandler.isValid(1));
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}

}
