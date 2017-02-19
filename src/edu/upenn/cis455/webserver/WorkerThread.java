package edu.upenn.cis455.webserver;

import java.net.Socket;

public class WorkerThread extends Thread{
	
	private BlockingQueue block_q = null;
	public boolean running = true;
	public ThreadStatus status = null;
	public WorkerThread(BlockingQueue block_q ){
		this.block_q = block_q;
		status = new ThreadStatus(this.getName(), "Waiting");
	}
	
	//This function dequeues the task and runs it
	public void run(){
		//Dequeue the client socket 
		while(running){
		try {
			Socket client_sock = (Socket)block_q.deqeue();
			//System.out.println("Thread working: " + this.getName());
			Runnable task = new RequestProcessor(client_sock, status);
			//Running the task to process the client request
			task.run();
		} catch (InterruptedException e) {
			if(running)
			    System.out.println("The worker thread: " + this.getName() + " was interrupted in WorkerThread Class!");
		}
		}
	}
	
	//This function terminates the worker thread and called when shutdown started
	public void terminateWorker(){
		running = false;
		this.interrupt();	
	}
	

}
