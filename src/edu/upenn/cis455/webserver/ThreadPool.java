package edu.upenn.cis455.webserver;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
	
   //total number of threads	
   private int no_threads = 0;	
   public boolean pool_stopped = false;
   
   public List<WorkerThread> pool = null; 
   
   //initiating the threads
   public ThreadPool(int no_threads, BlockingQueue bq){
	  pool = new ArrayList<WorkerThread>();
	  this.no_threads = no_threads;
	  //adding and starting the threads
	  for(int i= 0;i<no_threads; i++){
		  WorkerThread wt = new WorkerThread(bq);
		  wt.setName("Thread " + i);
		  pool.add(wt);
		  wt.start();
	  }
   }
   
   //This method is called when Shut down is called
   public void stopAllThreads(){
	   if(!pool_stopped){
		   pool_stopped = true;
		   //stopping all the threads
	       for(WorkerThread wt : this.pool){
		       wt.terminateWorker();
	       }
	   }
   }
}
