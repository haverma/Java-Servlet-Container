package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Todd J. Green
 */
class HttpServletRequestImpl implements HttpServletRequest {
	//private Map<String, String> params = null;
	private Properties m_params = new Properties();
	private Properties m_props = new Properties();
	private HttpSessionImpl m_session = null;
	private String m_method;
	private Map<String, Object> parsed_request = null;
	private Map<String, Object> parsed_headers = null;
	private String servlet_path = null;
	private String request_uri = null;
	private String remote_host = null;
	private String remote_addr = null;
	private int remote_port = 0;
	private Map<String, Object> request_line;
	private String character_encoding = null;
	private String content_type = "text/html";
	private Locale locale = null;
	private ServletContextImpl sc = null;

	
	HttpServletRequestImpl() {
		//this.params = new HashMap<String, String>();
	}
	
	HttpServletRequestImpl(HttpSessionImpl session,  Map<String, Object> parsed_request, Map<String, Object> parsed_header, String servlet_path, ServletContextImpl sc) {
		//this.params = new HashMap<String, String>();
		m_session = session;
		this.parsed_request = parsed_request;
		this.parsed_headers = parsed_header;
		this.servlet_path = servlet_path;
		request_line = (Map<String, Object>)parsed_request.get("requestline");
		this.request_uri = (String)request_line.get("request_uri");
		m_method = (String)request_line.get("method");
		this.sc = sc;
		content_type = (String)parsed_header.get("content-type");
		
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		// TODO Auto-generated method stub
		return "BASIC_AUTH";
	}
	
	public void setLocale(Locale locale){
		this.locale = locale;
	}

	public void setContentType(String content_type) {
		// TODO Auto-generated method stub
		this.content_type = content_type;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		Cookie[] cookies = null;
		if(parsed_request.containsKey("cookies")){
			Map<String, String> cookies_map = (Map<String, String>) parsed_request.get("cookies");
		    cookies = new Cookie[cookies_map.size()];
		    int i= 0;
		    for(Entry<String, String> entry : cookies_map.entrySet()){
		    	Cookie cookie = new Cookie(entry.getKey(), entry.getValue());
		    	cookies[i++] = cookie;		    }
		    
		}
		return cookies;
	}
	
	
	
	//Parse and return date formats
	public Date parseDate(String date){
		Map<String, Object> request_line = (Map<String, Object>)parsed_request.get("requestline");
		String version = (String)request_line.get("version");
		//if the request is 1.1 date should be always of 1st type
    	if(version.equals("1.1")){
    		try{
    			SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
    			Date parsed_date = sdf.parse(date);
    			return parsed_date;
    		}
    		catch(Exception e){
    			return null;
    		}
 
    	}
    	//else date can be of any of the three types
    	else{
			String[] date_formats = new String[] {"EEE, d MMM yyyy HH:mm:ss z", "EEEE, d-MMM-yy HH:mm:ss z", "EEE MMM d HH:mm:ss yyyy"};
			for(int i=0;i<date_formats.length;i++){
				try{
					final SimpleDateFormat sdf = new SimpleDateFormat(date_formats[i]);
					//Check for the date format with no time zone value
					if(i == 2)
					    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
					Date parsed_date = sdf.parse(date);
					return parsed_date;
				}
				catch(Exception e){
					continue;
				}
			}
			//return null if does not match any type
			return null;
    	}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String arg0) {
		// TODO Auto-generated method stub
		String date_string = null;
		if(parsed_headers.containsKey(arg0)){
    		date_string = (String) parsed_headers.get(arg0.toLowerCase());
    		Date req_date = parseDate(date_string);
    		if(req_date == null){
    			throw new IllegalArgumentException();
    		}
    		else{
    			return req_date.getTime();
    		}
    		
    	}
		//cannot find the field in the headers
		else{
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		return (String)parsed_headers.get(arg0.toLowerCase());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration getHeaders(String arg0) {
		// TODO Auto-generated method stub
		List<String> headers = new ArrayList<String>();
		headers.add((String)parsed_headers.get(arg0.toLowerCase()));
		return Collections.enumeration(headers);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration getHeaderNames() {
		// TODO Auto-generated method stub
		return Collections.enumeration(parsed_headers.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String arg0) throws NumberFormatException {
		// TODO Auto-generated method stub
		//casting the value to integer
		if(parsed_headers.get(arg0).toString() == null) return -1;
		int value = Integer.valueOf(parsed_headers.get(arg0.toLowerCase()).toString());
		return value;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return m_method;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		// TODO Auto-generated method stub
		//this method returns the path info.. as described in the servlet spec ... see page 39
		Integer offset = this.request_uri.indexOf(this.servlet_path);
		String result = this.request_uri.substring(offset+this.servlet_path.length());
		if(result.isEmpty()) return null;
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		// TODO Auto-generated method stub
		//change this for a multiple application E.C
		//check
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		// TODO Auto-generated method stub
		return (String)parsed_request.get("query_string");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		// check
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		//check
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return this.request_uri;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer("http://localhost:" + HttpServer.port + this.request_uri);
		return sb;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		// TODO Auto-generated method stub
		return servlet_path;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean arg0) {
		if (arg0) {
			if (! hasSession()) {
				m_session = new HttpSessionImpl(sc);
				ApplicationConstants.sessions_table.put(m_session.getId(),m_session);
			}
		} else {
			if (! hasSession()) {
				return null;
			}
		}
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		if(character_encoding == null) return "ISO-8859-1";
		else return character_encoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		character_encoding = arg0;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		// TODO Auto-generated method stub
		Integer content_length = null;
		try{
			content_length = Integer.valueOf(parsed_headers.get("content-length").toString());
		}
		catch(NumberFormatException e){
			return -1;
		}
		return content_length == null? -1 : content_length;
				
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		return content_type;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String arg0) {
		List<String> values = (List<String>) m_params.get(arg0);
		if(values == null) return null;
		//get the topmost value in the list
		return values.get(values.size()-1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration<String> getParameterNames() {
		Set<String> param_names = new HashSet<String>();
		while(m_params.keys().hasMoreElements()){
			String param = (String) m_params.keys().nextElement();
			System.out.println(param);
			param_names.add(param);
			}
		 final Enumeration<String> p_enum = Collections.enumeration(param_names);
		 return p_enum;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String arg0) {
		//gets array of values for given parameter
		List<String> values = (List<String>) m_params.get(arg0);
		return (String[]) values.toArray();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map<String, List<String>> getParameterMap() {
		// TODO Auto-generated method stub
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (final String name: m_params.stringPropertyNames())
		    map.put(name, (List<String>)m_params.get(name));;
		return map;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		// TODO Auto-generated method stub
		return "HTTP/" + request_line.get("version");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		// TODO Auto-generated method stub
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		// TODO Auto-generated method stub
		
		if(parsed_headers.get("host") == null) return "localhost";
		else{
			 String teest = (String)parsed_headers.get("host");
			String[] tokens = teest.split(":");
			if(tokens.length == 2) return tokens[0];
			else{
				return teest;
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		// TODO Auto-generated method stub
		if(parsed_headers.get("host") == null) return HttpServer.port;
		else{
			 String teest = (String)parsed_headers.get("host");
			String[] tokens = teest.split(":");
			if(tokens.length == 2) return Integer.parseInt(tokens[1]);
			else{
				return HttpServer.port;
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		String message_body = (String)parsed_request.get("message-body");
		InputStream is = new ByteArrayInputStream(message_body.getBytes());

		// read into BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		return br;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return this.remote_addr;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return this.remote_host;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		m_props.remove(arg0);

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return this.locale;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		// TODO Auto-generated method stub
		if(ApplicationConstants.useHTTPs) return true;
		else return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	//check
	@Deprecated
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		/*if(arg0.startsWith("/")) arg0 = arg0.substring(1);
		String full_path =  HttpServer.work_dir + File.separator + arg0;
		File file = new File(full_path);
		if(file.exists()) return full_path;
		else */
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return this.remote_port;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		// TODO Auto-generated method stub
		return "localhost";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return "127.0.0.1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return HttpServer.port;
	}

	void setMethod(String method) {
		m_method = method;
	}
	
	
	void setParameter(String key, String value) {
		if(m_params.get(key) == null){
			List<String> values = new ArrayList<>();
			m_params.put(key, values);
		}
		//adding the parameter value to the list of values
		List<String> param_values = (List<String>) m_params.get(key);
		param_values.add(value);
	}
	
	void clearParameters() {
		m_params.clear();
	}
	
	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}
	
	public void setRemoteHost(String host){
		this.remote_host = host;
	}
	
	public void setRemotePort(int port){
		this.remote_port = port;
	}
	
	public void setRemoteAddr(String addr){
		this.remote_addr = addr;
	}
		
}
