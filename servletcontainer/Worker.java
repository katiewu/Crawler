package servletcontainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.servlet.ServletException;

public class Worker extends Thread{
	private final TaskQueue taskQueue;
	private Task currenttask;
	StopSign stopSign;
	
	public Worker(TaskQueue taskQueue, StopSign stopSign){
		this.taskQueue = taskQueue;
		this.stopSign = stopSign;
	}
	
	public Task currentTask(){
		return currenttask;
	}
	
	public void run(){
		while(true){
//			try {
				Task t = taskQueue.getTask();
//				System.out.println(getName()+" request: "+t.getRequest());
				if(t == null) break;
				this.currenttask = t;
				try {
					t.process();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				if(stopSign.isStopped()){
//					System.out.println(getName()+" get stop sign");
					break;
				}
//				sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
//		System.out.println(getName()+"end run");
		
		
	}

}

