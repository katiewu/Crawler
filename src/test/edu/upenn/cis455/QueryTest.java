package test.edu.upenn.cis455;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import edu.upenn.cis455.xpathengine.PathNode;
import edu.upenn.cis455.xpathengine.Query;

public class QueryTest extends TestCase {

	Query query;
	
	protected void setUp() throws Exception {
		String expression = "/a[e]/b[c]/d";
		query = new Query(expression, 0, 0);	
	}
	
	public void testChildren(){
		PathNode head = query.getHead();
		List<PathNode> children = head.getChildren();
		List<String> childrenNodes = new ArrayList<String>();
		for(PathNode child:children){
			childrenNodes.add(child.getPathName());
		}
		assertEquals(childrenNodes.size(), 2);
		assertEquals(childrenNodes.contains("e"), true);
		assertEquals(childrenNodes.contains("b"), true);
		assertEquals(childrenNodes.contains("d"), false);
		assertEquals(childrenNodes.contains("c"), false);
	}

}
