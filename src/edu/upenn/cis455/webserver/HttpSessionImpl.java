package edu.upenn.cis455.webserver;

import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.log4j.Logger;

/**
 * @author Todd J. Green
 */
//This class implements the session interface from java servlets
public class HttpSessionImpl implements HttpSession {
	//attribute to hold the session variables
	Map<String, Object> attributes = new HashMap<String, Object>();
	Date created_time = null;
	long last_access_time;
	UUID session_id;
	int max_inactive_time = 1800;
	boolean session_valid;
	boolean is_new;
	ServletContext sc = null;
	
	
	static Logger log = Logger.getLogger(HttpServletResponseImpl.class.getName());
	

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 */
	
	//zero argument constructor
	public HttpSessionImpl(ServletContext sc){
		created_time = new Date();
		last_access_time = created_time.getTime();
		is_new = true;
		session_valid = true;
		session_id = UUID.randomUUID();
		this.sc = sc;
		if(HttpServer.session_timeout_global != -1) max_inactive_time = HttpServer.session_timeout_global*60;
	}
	
	public long getCreationTime() {
		// TODO Auto-generated method stub
		if(isValid()){
			return created_time.getTime();
		}
		else
			throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	public String getId() {
		// TODO Auto-generated method stub
		return session_id.toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return last_access_time;
	}
	
	//setting the last access time
	public void setLastAccessTime(long lastaccess){
		last_access_time = lastaccess;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		//returns the servlet context
		return sc;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int arg0) {
		//sets the max interval
		// TODO Auto-generated method stub
		if(isValid()){
			max_inactive_time = arg0;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return max_inactive_time;
	}

	//this is deprecated
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		if(isValid())
		    return attributes.get(arg0);
		else
			throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	//deprecated
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		if(isValid()){
			Enumeration<String> attr_names = Collections.enumeration(attributes.keySet());
			return attr_names;
		}
		else{
			throw new IllegalStateException();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */
	//deprecated
	public String[] getValueNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		if(isValid())
		    attributes.put(arg0, arg1);
		else
			throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	//deprecated
	public void putValue(String arg0, Object arg1) {
		//deprecated
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		if(isValid())
			attributes.remove(arg0);
		else
			throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	//deprecated
	public void removeValue(String arg0) {
	//depecated
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	public void invalidate() {
		if(isValid()){
			ApplicationConstants.sessions_table.remove(getId());
			session_valid = false;
			log.info("Session is invalidated");
		}
		else
			throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	public boolean isNew() {
		// TODO Auto-generated method stub
		if(isValid())
			return is_new;
		else
			throw new IllegalStateException();
	}

	boolean isValid() {
		long diff_time = new Date().getTime() - last_access_time;
		if(diff_time > max_inactive_time*1000){
			log.info("Session is invaliated");
			session_valid = false;
			ApplicationConstants.sessions_table.remove(getId());
		}
		return session_valid;
	}
}
