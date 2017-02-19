package edu.upenn.cis455.webserver;
import javax.servlet.*;
import java.util.*;

/**
 * @author Nick Taylor
 */
//this class is used to pass infotmation to the servlet
class ServletConfigImpl implements ServletConfig {
	private String name;
	private ServletContextImpl context;
	private HashMap<String,String> initParams;
	
	public ServletConfigImpl(String name, ServletContextImpl context) {
		this.name = name;
		this.context = context;
		initParams = new HashMap<String,String>();
	}

	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public ServletContext getServletContext() {
		return context;
	}
	
	public String getServletName() {
		return name;
	}

	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
