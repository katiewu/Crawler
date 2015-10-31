package test.edu.upenn.cis455;

import java.util.List;

import junit.framework.TestCase;
import edu.upenn.cis455.crawler.Processor;
import edu.upenn.cis455.crawler.RobotManager;
import edu.upenn.cis455.crawler.URLFrontier;

public class XPathCrawlerTest extends TestCase {
	String url;
	URLFrontier urlFrontier;
	
	protected void setUp() throws Exception {
		super.setUp();
		url = "https://dbappserv.cis.upenn.edu/crawltest.html";
		urlFrontier = new URLFrontier(1);
	}
	
	public void testProcessor(){
		List<String> links = Processor.extractLink(url);
		assertEquals(links.contains("https://dbappserv.cis.upenn.edu/crawltest/nytimes/"), true);
		assertEquals(links.contains("https://dbappserv.cis.upenn.edu/crawltest/bbc/"), true);
		assertEquals(links.contains("https://dbappserv.cis.upenn.edu/crawltest/cnn/"), true);
		assertEquals(links.contains("https://dbappserv.cis.upenn.edu/crawltest/international/"), true);
	}
	
	public void testRobotManager(){
		RobotManager.addRobot(url);
		assertEquals(RobotManager.isValid("https://dbappserv.cis.upenn.edu/crawltest/marie/private/"), false);
		assertEquals(RobotManager.isValid("https://dbappserv.cis.upenn.edu/crawltest/foo/"), false);
		assertEquals(RobotManager.isValid("https://dbappserv.cis.upenn.edu/crawltest/marie/private"), true);
		assertEquals(RobotManager.isValid("https://dbappserv.cis.upenn.edu/crawltest/"), true);		
	}
	
	public void testURLFrontier(){
		urlFrontier.pushURL(url);
		assertEquals(urlFrontier.filter(url), false);
		assertEquals(urlFrontier.filter("https://dbappserv.cis.upenn.edu/crawltest/marie/private"), true);
	}
	
	
}
