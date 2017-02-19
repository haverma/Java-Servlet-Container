package edu.upenn.cis455.webserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//This class contains all the constants and Error codes and their messages
public class ApplicationConstants {

	public static final Map<Integer, String> ErrorCodes;
	public static final Map<String, String> MimeTypes;
	public static final Set<String> special_urls;
	public static Map<String, HttpSessionImpl> sessions_table;
	//change this to true to make it an HTTPs server
	public static final boolean useHTTPs = false;
	
	static {
		ErrorCodes = new HashMap<Integer, String>();
		sessions_table = new HashMap<String, HttpSessionImpl>();
		// Add the error codes: 405, 417, 500
		ErrorCodes.put(200, "OK");
		ErrorCodes.put(400, "Bad Request");
		ErrorCodes.put(501, "Not Implemented");
		ErrorCodes.put(414, "Request URI too long");
		ErrorCodes.put(403, "Forbidden");
		ErrorCodes.put(505, "HTTP version not supported");
		ErrorCodes.put(404, "Not Found");
		ErrorCodes.put(304, "Not Modified");
		ErrorCodes.put(412, "Precondition Failed");
		ErrorCodes.put(415, "Unsupported Media Type");
		ErrorCodes.put(417, "Expectation Failed");
		ErrorCodes.put(500, "Internal Server Error");
		MimeTypes = new HashMap<String, String>();
		MimeTypes.put("jpg", "image/png");
		MimeTypes.put("htm", "text/html");
		MimeTypes.put("html", "text/html");
		MimeTypes.put("png", "image/png");
		MimeTypes.put("gif", "image/gif");
		MimeTypes.put("txt", "txt/plain");
		special_urls = new HashSet<String>();
		special_urls.add("/control");
		special_urls.add("/shutdown");
		
	}

}
