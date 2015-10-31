package test.edu.upenn.cis455;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

public class DBWrapperTest extends TestCase {
	
	DBWrapper db;

	protected void setUp() throws Exception {
		super.setUp();
		String filepath = "/home/cis455";
		db = new DBWrapper(filepath);
		db.setup();
	}
	
	protected void teardown(){
		db.close();
	}
	
	public void testPutGetUser() {
		String username = "katie";
		String password = "password";
		db.putUser(username, password);
		db.sync();
		User user = db.getUser(username);
		assertEquals(user.getUsername(), "katie");
		assertEquals(user.getPassword(), "password");
		password = "changepassword";
		db.putUser(username, password);
		db.sync();
		user = db.getUser(username);
		assertEquals(user.getUsername(), "katie");
		assertEquals(user.getPassword(), "changepassword");
	}

	public void testGetPutChannel() {
		String username = "katie";
		String channelname = "war";
		String stylesheetURL = "na";
		String set = "/themes/war";
		db.putChannel(username, channelname, stylesheetURL, set, null);
		db.sync();
		Channel channel = db.getChannel(username, channelname);
		assertEquals(channel.getStylesheetURL(), "na");
		assertEquals(channel.getPrimaryKey(), "katie;war");
	}
	
	public void testdeleteChannel(){
		String username = "katie";
		String channelname = "war";
		String stylesheetURL = "na";
		String set = "/themes/war";
		db.putChannel(username, channelname, stylesheetURL, set, null);
		db.sync();
		db.deleteChannel(username, channelname);
		assertEquals(db.getChannel(username, channelname), null);
	}
	
	public void testGetUserChannels(){
		String username = "katie";
		String password = "password";
		db.putUser(username, password);
		db.sync();
		db.putChannel("katie", "war", "na", "na", null);
		db.putChannel("katie", "peace", "na", "na", null);
		db.putChannel("katie", "fruit", "na", "na", null);
		db.putChannel("katie", "fitout", "na", "na", null);
		db.sync();
		List<Channel> list = db.getUserChannels(username);
		List<String> names = new ArrayList<String>();
		for(Channel channel:list){
			names.add(channel.getChannelName());
		}
		assertEquals(names.contains("war"), true);
		assertEquals(names.contains("peace"), true);
		assertEquals(names.contains("fruit"), true);
		assertEquals(names.contains("fitout"), true);
	}

}
