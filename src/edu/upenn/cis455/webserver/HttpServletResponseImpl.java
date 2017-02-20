package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author tjgreen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//check when to write and flush the bufferedprintwriter
public class HttpServletResponseImpl implements HttpServletResponse {

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	private Map<String, String> headers = new HashMap<String, String>();
	private List<Cookie> cookies = new ArrayList<Cookie>();
	private PrintStream out;
	private int status_code = 200;
	private BufferedPrintWriter printout = null;
	Map<String, Object> parsed_request = null;
	private Locale locale;

	
	public HttpServletResponseImpl(PrintStream out, Map<String, Object> httprequest, HttpServletRequestImpl req) {
		// TODO Auto-generated constructor stub
		this.out = out;
		this.printout = new BufferedPrintWriter(out, cookies, req, headers);
		this.parsed_request = httprequest;
	}
	
	
	public HttpServletResponseImpl() {
		// TODO Auto-generated constructor stub
		//This will not work
	}
	public void addCookie(Cookie arg0) {
		// TODO Auto-generated method stub
		cookies.add(arg0);
        
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String arg0) {
		// TODO Auto-generated method stub
		return headers.containsKey(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted()){
			throw new IllegalStateException();
		}
		String html = new String(
				"<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>"
						+ arg0 + " "
						+ arg1
						+ "</title></head><body><h1>" + arg0 + " "
						+ ApplicationConstants.ErrorCodes.get(arg0)
						+ "</h1></body></html>");
		this.status_code = arg0;
		headers.put("content-type", "text/html");
		headers.put("content-length", String.valueOf(html.length()));
	    this.printout.resetBuffer();
	    printout.headers = headers;
		printout.flush();
		return;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted()){
			throw new IllegalStateException();
		}
		String html = new String(
				"<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>"
						+ arg0 + " "
						+ ApplicationConstants.ErrorCodes.get(arg0)
						+ "</title></head><body><h1>" + arg0 + " "
						+ ApplicationConstants.ErrorCodes.get(arg0)
						+ "</h1></body></html>");
		this.status_code = arg0;
		headers.put("content-type", "text/html");
		headers.put("content-length", String.valueOf(html.length()));
	    this.printout.resetBuffer();
	    printout.headers = headers;
		printout.flush();
		return;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String arg0) throws IOException {
		printout.setStatus(302);
		Map<String, Object> request_line = (Map<String, Object>)parsed_request.get("requestline");
		String request_uri = (String)request_line.get("request_uri");
		if (!isCommitted()) {
			if(arg0.toLowerCase().startsWith("http://"))
			{
				headers.put("location", arg0);
			}
			else if(arg0.startsWith("/"))
			{
				headers.put("location", "http://localhost:" + HttpServer.port + arg0);
			}
			else
			{
				headers.put("location","http://localhost:" + HttpServer.port + request_uri + "/" + arg0); 
			}
			printout.headers = headers;
			printout.flush();
		} else {
			throw new IllegalStateException();
		}
	    
	}

	//get date in GMT format like Sun, 21 Apr 2015 22:13:45 GMT
		public String getDateString(Date input_date){
			final SimpleDateFormat sdf = new SimpleDateFormat(
					"EEE, d MMM yyyy HH:mm:ss z", Locale.US);

			// Give it to me in GMT time.
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			return sdf.format(input_date);
			
		}
		
		
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		Date date = new Date(arg1);
		String dateString = getDateString(date);
		headers.put(arg0.toLowerCase(), dateString);
		return;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		Date date = new Date(arg1);
		String dateString = getDateString(date);
		addHeader(arg0.toLowerCase(), dateString);
		return;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
        this.headers.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		String key = arg0.toLowerCase();
		if(headers.get(key) == null){
			headers.put(arg0, arg1);
		}
		else{
			//adding the value to the list
			String value = headers.get(key);
			headers.put(arg0.toLowerCase(), value + ", " + arg1);
		}
		return;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
        setHeader(arg0, String.valueOf(arg1));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
        addHeader(arg0, String.valueOf(arg1));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub
		status_code = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	//deprecated
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return "ISO-8859-1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		if(headers.containsKey("Content-Type"))
		    return headers.get("Content-Type");
		else
			return "text/html";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		return printout;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		headers.put("Character-Encoding", arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
        setHeader("Content-Length", String.valueOf(arg0));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		setHeader("Content-Type", arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) throws IllegalStateException{
		// TODO Auto-generated method stub
        printout.setBuffer_size(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return printout.getBuffer_size();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
        printout.flush();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		// TODO Auto-generated method stub
		if(!isCommitted())
             printout.resetBuffer();
		else throw new IllegalStateException();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return this.printout.isCommitted();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub
		if(!isCommitted()){
            printout.resetBuffer();
            status_code = 200;
            headers = new HashMap<String, String>();
		}
		else throw new IllegalStateException();

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		this.locale = arg0;
 
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return this.locale;
	}

}
