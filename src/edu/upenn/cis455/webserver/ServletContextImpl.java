package edu.upenn.cis455.webserver;

import javax.servlet.*;

import org.apache.tools.ant.types.FlexInteger;

import java.io.File;
import java.util.*;

/**
 * @author Nick Taylor
 */
class ServletContextImpl implements ServletContext {
	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	private String display_name;
	
	public ServletContextImpl(String display_name) {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
		this.display_name = display_name;
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public Enumeration getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public ServletContext getContext(String name) {
		//todo
		return null;
	}
	
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public int getMajorVersion() {
		return 2;
	}
	
	public String getMimeType(String file) {
		//excluded
		return null;
	}
	
	public int getMinorVersion() {
		return 4;
	}
	
	public RequestDispatcher getNamedDispatcher(String name) {
		// excluded
		return null;
	}
	
	public String getRealPath(String path) {
		//not sure Check
		if(path.startsWith("/")) path = path.substring(1);
		String full_path = HttpServer.work_dir + File.separator + path;
		File file = new File(full_path);
		if(file.exists()) return full_path;
		else return null;
	}
	
	public RequestDispatcher getRequestDispatcher(String name) {
		//excluded
		return null;
	}
	
	public java.net.URL getResource(String path) {
		return null;
	}
	
	public java.io.InputStream getResourceAsStream(String path) {
		//excluded
		return null;
	}
	
	public java.util.Set getResourcePaths(String path) {
		//excluded
		return null;
	}
	
	public String getServerInfo() {
		return "Harsh Verma Server 1.0";
	}
	
	//deprecated
	public Servlet getServlet(String name) {
		return null;
	}
	
	public String getServletContextName() {
		return this.display_name;
	}
	
	//deprecated
	public Enumeration getServletNames() {
		return null;
	}
	
	//deprecated
	public Enumeration getServlets() {
		return null;
	}
	
	//deprecated
	public void log(Exception exception, String msg) {
		log(msg, (Throwable) exception);
	}
	
	public void log(String msg) {
		//excluded
		System.err.println(msg);
	}
	
	
	public void log(String message, Throwable throwable) {
		//excluded
		System.err.println(message);
		throwable.printStackTrace(System.err);
	}
	
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
