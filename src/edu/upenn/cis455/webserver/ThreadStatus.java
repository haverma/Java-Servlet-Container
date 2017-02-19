package edu.upenn.cis455.webserver;

//This Class stores the name and status of each thread
public class ThreadStatus {
	//Thread name
	private String thread_name;
	//Thread status
    private String thread_status;
    
    public ThreadStatus(String tname, String tstatus){
        thread_name = tname;
        thread_status = tstatus;
    }
    
	public String getThread_name() {
		return thread_name;
	}
	public void setThread_name(String thread_name) {
		this.thread_name = thread_name;
	}
	public String getThread_status() {
		return thread_status;
	}
	public void setThread_status(String thread_status) {
		this.thread_status = thread_status;
	}
    
    
}
