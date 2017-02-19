package edu.upenn.cis455.webserver;

import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueue {
   
	
	private Queue<Object> blocking_queue;
	//This keeps the maximum number of elements in the queue
	int max_capacity;
	
	
	public BlockingQueue(int capacity){
		// The Blocking being initialized
		blocking_queue = new LinkedList<Object>();
		
		max_capacity = capacity;	
	}
	
	synchronized public void enqueue(Object socket) throws InterruptedException{
		// Thread will wait if the queue is full currently
		while(blocking_queue.size() == max_capacity){
			wait();
		}
		//If the thread was empty then wake up the consumer threads
		if(blocking_queue.size() == 0) notifyAll();
		
		blocking_queue.offer(socket);
	}
	
	synchronized public Object deqeue() throws InterruptedException{
		while(blocking_queue.isEmpty()){
			wait();
		}
		// wake the producer threads if the queue was full
		if(blocking_queue.size() == max_capacity) notifyAll();
		
		return blocking_queue.poll();
	} 
}
