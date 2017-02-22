package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;

public class BufferedPrintWriter extends PrintWriter {
	// Starting the logger
	static Logger log = Logger.getLogger(BufferedPrintWriter.class.getName());
	private int buffer_size = 1000;
	private boolean committed;
	//storing the cookies
	private List<Cookie> cookies = new ArrayList<Cookie>();
	private int status = 200;
	//Map for headers
	private Map<String, String> headers;
	private PrintStream out = null;
	private StringBuffer buffer = null;
	private HttpServletRequestImpl rq= null;

	BufferedPrintWriter(PrintStream out, List<Cookie> cookies, HttpServletRequestImpl req) {
		super(out);
		this.out = out;
		//Storing the cookies
		this.cookies = cookies;
		this.headers = new HashMap<String, String>();
		//setting the buffer size
		this.buffer = new StringBuffer(buffer_size);
		this.rq = req;
	}
	
	BufferedPrintWriter(PrintStream out, List<Cookie> cookies, HttpServletRequestImpl req, Map<String, String> headers) {
		super(out);
		this.out = out;
		this.cookies = cookies;
		this.headers = headers;
		this.buffer = new StringBuffer(buffer_size);
		//Httpservlet request object
		this.rq = req;
	}


	@Override
	public void close() {
		//flushing and closing the writer
	    flush();
	    super.close();
	}
	
	@Override
	public void write(String s) {
		if (buffer.length() + s.length() >= buffer.capacity())
			flush();
		buffer.append(s);

	}

	@Override
	public void write(char[] buf) {
		write(String.valueOf(buf));
	}

	@Override
	public void write(int val) {
		write(String.valueOf(val));
	}

	@Override
	public void write(String s, int offset, int len) {
		String subseq = s.substring(offset, offset + len);
		write(subseq);
	}

	@Override
	public PrintWriter append(char c){
		write(String.valueOf(c));
		return this;
	} 
	
	@Override
	public PrintWriter append(CharSequence csq){
		write(csq.toString());
		return this;
	}
	
	@Override
	public void println() {
		write("\r\n");
	};
	
	@Override
	public void println(String x) {
		write(x);
		println();
	};
	
	@Override
	public void flush() {
		//log.info("Flushing the buffer");
		//checking the commit flag
		try {
			if (!committed) {
				//writing the request line
				out.print("HTTP/1.1 " + status + " " + ApplicationConstants.ErrorCodes.get(status) + "\r\n");
				// writing headers
				for (Entry<String, String> entry : headers.entrySet()) {
					String h = entry.getKey() + ": " + entry.getValue() + "\r\n";
					out.print(h);
				}
				//adding the session cookie
				if(rq.hasSession()){
					Cookie session_cookie = new Cookie("JSESSIONID", rq.getSession().getId());
					cookies.add(session_cookie);
					HttpSessionImpl session = ApplicationConstants.sessions_table.get(rq.getSession().getId());
					session.setLastAccessTime(new Date().getTime());
				}
				// handle cookie
				if(cookies.size() > 0){
					StringBuilder cookies_value = new StringBuilder();
					for(Cookie cookie_temp : cookies){
						//setting the various attributes
						StringBuilder cookie_entry = new StringBuilder("Set-Cookie: " + cookie_temp.getName() + "=" + cookie_temp.getValue());
						if(cookie_temp.getMaxAge() != -1)
							cookie_entry.append(";Max-Age=" + cookie_temp.getMaxAge());
						if(cookie_temp.getDomain() != null)
							cookie_entry.append(";Domain=" + cookie_temp.getDomain());
						if(cookie_temp.getPath() != null)
							cookie_entry.append(";Path=" + cookie_temp.getPath());
						if(cookie_temp.getComment() != null)
							cookie_entry.append(";Comment=" + cookie_temp.getPath());
						cookies_value.append(cookie_entry);
						cookies_value.append("\r\n");
					}
					out.print(cookies_value.toString());
					
					
				}
				out.print("\r\n");
				//out.write("haha".getBytes());
				//writing the buffer
				out.write(buffer.toString().getBytes());
				committed = true;
			} else {
				//out.write("bobo".getBytes());
				out.write(buffer.toString().getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error while writing response");

		}
		resetBuffer();
	}
    //resetting the buffer for data
	public void resetBuffer() {
		buffer = new StringBuffer(buffer_size);
	}

	//These are the various getter and setter methods
	public int getBuffer_size() {
		return buffer_size;
	}

	public void setBuffer_size(int buffer_size) throws IllegalStateException {
		if (committed)
			throw new IllegalStateException();
		else
			this.buffer_size = buffer_size;
	}

	public boolean isCommitted() {
		return committed;
	}

	public void setCommitted(boolean committed) {
		this.committed = committed;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public void setCookies(List<Cookie> cookies) {
		this.cookies = cookies;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

}
