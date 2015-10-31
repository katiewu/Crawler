package servletcontainer;

public class StopSign {
	boolean isStopped;
	
	public StopSign(){
		isStopped = false;
	}
	
	public void setStopped(){
		isStopped = true;
	}
	public boolean isStopped(){
		return isStopped;
	}
}
