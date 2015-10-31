package test.edu.upenn.cis455;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase 
{
  public static Test suite() 
  {
    try {
      Class[]  testClasses = {
    		  Class.forName("test.edu.upenn.cis455.PathNodeTest"),
    		  Class.forName("test.edu.upenn.cis455.QueryTest"),
    		  Class.forName("test.edu.upenn.cis455.XPathEngineImplTest"),
    		  Class.forName("test.edu.upenn.cis455.XPathServletTest"),
    		  Class.forName("test.edu.upenn.cis455.XPathCrawlerTest"),
    		  Class.forName("test.edu.upenn.cis455.DBWrapperTest")
      };   
      
      return new TestSuite(testClasses);
    } catch(Exception e){
      e.printStackTrace();
    } 
    
    return null;
  }
  
  public static void main(String[] args){
	  TestResult result = new TestResult();
	  suite().run(result);
  }
}
