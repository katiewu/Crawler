package test.edu.upenn.cis455;

import java.util.HashMap;
import java.util.List;

import edu.upenn.cis455.xpathengine.PathNode;
import edu.upenn.cis455.xpathengine.Query;
import junit.framework.TestCase;

public class PathNodeTest extends TestCase {
	
	Query query1;
	Query query2;

	protected void setUp() throws Exception {
		String expression1 = "/book[@title = \"Harry Potter\"][@lang = \"en\"]";
		String expression2 = "/book[text() = \"Love Story\"][contains(text(), \"Love\")]";
		query1 = new Query(expression1, 0, 0);	
		query2 = new Query(expression2, 0, 0);
	}
	
	public void testAttributeList(){
		PathNode head = query1.getHead();
		HashMap<String, String> attributeList = head.getAttributeList();
		assertEquals(attributeList.size(), 2);
		assertEquals(attributeList.containsKey("title"), true);
		assertEquals(attributeList.containsKey("lang"), true);
		assertEquals(attributeList.get("title"), "Harry Potter");
		assertEquals(attributeList.get("lang"), "en");
	}
	
	public void testTextList(){
		PathNode head = query2.getHead();
		List<String> textList = head.getTextList();
		List<String> containsList = head.getContainsList();
		assertEquals(textList.size(), 1);
		assertEquals(textList.contains("Love Story"), true);
		assertEquals(containsList.size(), 1);
		assertEquals(containsList.contains("Love"), true);
	}

}
