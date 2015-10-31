package servletcontainer;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class TaskQueue {
	private LinkedList<Task> taskQueue;
	private int taskRecord;
	private static final int SIZE = 1000;
	StopSign stopSign;
	
	public TaskQueue(StopSign stopSign){
		taskQueue = new LinkedList<Task>();
		this.stopSign = stopSign;
		this.taskRecord = 0;
	}
	
	public boolean isEmpty(){
		return taskQueue.isEmpty();
	}
	
	public int getSize(){
		return taskRecord;
	}
	
	public synchronized void insertTask(Task t) {
		while(getSize()>SIZE){
			System.out.println("Queue is full!");
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		taskQueue.add(t);
		taskRecord++;
//		t.initialTask();
//		if(t != null && t.getRequest() != null && t.getRequest().isShutDown()) stopSign.setStopped();
		notify();
	}
	
	public synchronized Task getTask() {
		while(taskQueue.isEmpty()){
			if(stopSign.isStopped()) return null;
			else
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		Task t = taskQueue.pop();
		taskRecord--;
//		t.initialTask();
//		if(t != null && t.getRequest() != null && t.getRequest().isShutDown()){
//			stopSign.setStopped();
//			notifyAll();
//		}
		return t;
	}
}
