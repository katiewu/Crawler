package edu.upenn.cis455.storage;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class DataAccessor {
	
	PrimaryIndex<String, User> UserIndex;
	PrimaryIndex<String, Channel> ChannelIndex;
	PrimaryIndex<String, Webpage> WebpageIndex;
	SecondaryIndex<String,String,Channel> ChannelUsernameIndex;
	SecondaryIndex<String,String,Webpage> WebpageTypeIndex;
	
	public DataAccessor(EntityStore store){
		UserIndex = store.getPrimaryIndex(String.class, User.class);
		ChannelIndex = store.getPrimaryIndex(String.class, Channel.class);
		ChannelUsernameIndex = store.getSecondaryIndex(ChannelIndex, String.class, "username");
		WebpageIndex = store.getPrimaryIndex(String.class, Webpage.class);
		WebpageTypeIndex = store.getSecondaryIndex(WebpageIndex, String.class, "type");
	}

	

}
