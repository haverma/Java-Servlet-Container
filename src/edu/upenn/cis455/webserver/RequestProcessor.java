package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

public class RequestProcessor implements Runnable {
	//Starting the logger
	static Logger log = Logger.getLogger(RequestProcessor.class.getName());
	//Client sock is the sock for the client
	private Socket client_sock = null;
	private String raw_request = null;
	//map containing the parsed request
	private Map<String, Object> parsed_request;
	private Map<String, Object> parsed_headers;
	//map for response
	private Map<String, Object> response_data;
	private String date_curr = null;
	private ThreadStatus threadStatus = null;
	private String server_name = "localhost";
	private String method_name = "";
	private Map<String, List<String>> parameter_values;
	private String remote_addr = null;
	private String host_name = null;
	private int port;
    //supported methods
	private Set<String> methods = new HashSet<String>(Arrays.asList("GET",
			"HEAD", "POST"));
	
	//This constructor is for testting
	public RequestProcessor(){
		
	}

	public RequestProcessor(Socket sock, ThreadStatus status) {
		client_sock = sock;
		String remote_addr_whole = sock.getRemoteSocketAddress().toString();
		if(remote_addr_whole.startsWith("/")) remote_addr_whole = remote_addr_whole.substring(1);
		remote_addr = remote_addr_whole.substring(0, remote_addr_whole.lastIndexOf(":"));
		port = Integer.parseInt(remote_addr_whole.substring(remote_addr_whole.lastIndexOf(":") + 1, remote_addr_whole.length()));
		host_name = ((InetSocketAddress)client_sock.getRemoteSocketAddress()).getAddress().getHostName();
		
		response_data = new HashMap<String, Object>();
		parsed_request = new HashMap<String, Object>();
		parsed_headers = new HashMap<String, Object>();
		threadStatus = status;
		parameter_values = new HashMap<String, List<String>>();
		final Date currentTime = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"EEE, d MMM yyyy HH:mm:ss z");

		// Convert in GMT time.
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		date_curr = sdf.format(currentTime);
		
		response_data.put("Date", date_curr);
		response_data.put("Server", "Harsh/1.0.0 (Ubuntu64)");
		response_data.put("Connection", "close");
		
		//Client socket being set to a timeout of 10 second 
//		 try { 
//			 client_sock.setSoTimeout(10000); 
//		 } 
//		 catch (SocketException e) {
//			 System.out.println("Connection timeout while serving the client: " +
//					 	client_sock.getInetAddress()); 
//		 }
		 

	}
	

	// This function reads from the client socket and processes it appropriately
	public void run() {
		try {
			PrintStream out = new PrintStream(client_sock.getOutputStream(),
					true);

			//reading from socket
			BufferedReader in = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String inputLine;
			while (true){
				inputLine = in.readLine();
			    if(!(inputLine == null) && !inputLine.equals(""))
			        sb.append(inputLine + "\n");
			    else
			    	break;
			}
			//in.close();
			
			raw_request = sb.toString();
			// PrintWriter out = new PrintWriter(client_sock.getOutputStream(),
			// true);
			// out.println(new Date().toString());
			
			//process it
			process_request(raw_request, in, out);
            in.close();
            out.flush();
            out.close();
			client_sock.close();
			//System.out.println("served the request");
			//set state after processing request
			threadStatus.setThread_status("Waiting");
			

		} catch (IOException e) {
			try {
				client_sock.close();
				//set status
				threadStatus.setThread_status("Waiting");
				//System.out.println("Inside catch");
			} catch (IOException e1) {
				//set status
			    threadStatus.setThread_status("Waiting");
		
			}
		
		}

	}
	
	//get the message body
	public void populate_message_body(BufferedReader in, PrintStream out){
		if(method_name.equals("POST")){
			//POST request should have content-length
			Integer content_length = null;
			try{
				content_length = Integer.valueOf(parsed_headers.get("content-length").toString());
			}
			catch(NumberFormatException e){
				log.error("invalid HTTP request, Content Length is invalid");
			}
			if(content_length == null){
				//check what to do
				response_data.put("Error", 400);
				out.print(formErrorResponse(response_data));
				log.error("Invalid HTTP POST request received");
				return;				
			}
			char[] buffer = new char[content_length];
			int charsIn;
			//reading the message body
			try {
				charsIn = in.read(buffer, 0, content_length);
				StringBuilder data = new StringBuilder(charsIn);
				data.append(buffer, 0, charsIn);
				parsed_request.put("message-body", data);
			} catch (IOException e) {
				log.error("Error while reading the HTTP POST request");
			}

			
		}
	}
	
	//get date in GMT format like Sun, 21 Apr 2015 22:13:45 GMT
	public String getDateString(Date input_date){
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"EEE, d MMM yyyy HH:mm:ss z");

		// Give it to me in GMT time.
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(input_date);
		
	}

	// Method to parse the whole request and return status
	public void process_request(String request_body, BufferedReader in, PrintStream out) {
        //Check to handle empty requests (See if it can be removed) 
		if(request_body.isEmpty()){
        	return;
        }
		String request_line = request_body.substring(0,
				request_body.indexOf("\n"));
		String headers = request_body.substring(request_body.indexOf("\n") + 1);

		Map<String, Object> request_line_map = new HashMap<String, Object>();
		// parsing the first line
		if (!parse_request_line(request_line, request_line_map)) {
			out.print(formErrorResponse(response_data));
			return;

		}
		parsed_request.put("requestline", request_line_map);
		//parse headers
		if(!parse_headers(headers)){
			out.print(formErrorResponse(response_data));
			return;
		}
		parseCookies();
		populate_message_body(in, out);
		//serve request using servlets or static resources
		if(!requestDispatcher(out)){
			out.print(formErrorResponse(response_data));
			return;
		}
		return;

	}
	
	//This method dispatches the requests to servlets or static files 
	public boolean requestDispatcher(PrintStream out){
		Map<String, Object> request_line = (Map<String, Object>)parsed_request.get("requestline");
		String request_uri = (String)request_line.get("request_uri");
		String[] tokens = request_uri.split("\\?");
		String query_string = null;
		//adding the request URI
		Map<String, Object> request_line1 = (Map<String, Object>)parsed_request.get("requestline");
		request_line1.put("request_uri", tokens[0]);
			
		if(tokens.length == 2)
			query_string = tokens[1];
		//query_string = decodeQueryString(query_string);
		//getting paramters from query string
		if(query_string != null)
			extractParams(query_string);
		
		//getting parameters from POST body
		if(method_name.equals("POST") && parsed_request.get("message-body") != null){
			if(parsed_headers.containsKey("content-type") 
					&& parsed_headers.get("content-type").equals("application/x-www-form-urlencoded")){
				String message_body = parsed_request.get("message-body").toString();
				if(message_body.length()>0){
					extractParams(message_body);
				}
			}
		}
		parsed_request.put("query_string", query_string);
		HttpServlet servlet_req = null;
		StringBuilder servlet_path = new StringBuilder();
		servlet_req = HttpServer.servlet_map.get(tokens[0]);
		if(servlet_req != null) servlet_path.append(tokens[0]);
		if(servlet_req == null){
			servlet_req = getMatchingServlet(tokens[0], servlet_path);
		}
		if(servlet_req != null){
			//invoking the servlets with instance
			//check here todo how to pass the output stream for response
			//log.info("Invoking the servlet with path " + servlet_path.toString());
		    return invokeServlet(servlet_req, servlet_path, out);
		}
		//none of the url-patterns matched
		else{
			return serve_request(out);
		}
	}
	
	//extracting the parameters
	public void extractParams(String param_string){
		//parsing and extracting the arguments from the query string or body
		String[] tokens = param_string.split("\\&");
		for(String token: tokens){
			int equal_index = token.indexOf("=");
			String param_name = null;
			String param_value = null;
			//equals is the first character
			if(equal_index ==  0) continue;
			//equals is not present
			else if(equal_index == -1){
				param_name = decodeQueryString(token);
				param_value = "";
			}
			//equals is the last character
			else if(equal_index == token.length()-1){
				param_name = decodeQueryString(token.substring(0, token.length()-1));
				param_value = "";
			}
			//equals is present in middle somewhere
			else if(equal_index >0 && equal_index < token.length()-1){
				param_name = decodeQueryString(token.substring(0, equal_index));
				param_value = decodeQueryString(token.substring(equal_index+1, token.length()));
			}
			if(param_name == null)continue;
			
			if(parameter_values.get(param_name)== null){
				List<String> param_values = new ArrayList<String>();
				parameter_values.put(param_name, param_values);
			}
			parameter_values.get(param_name).add(param_value);
				
			
				
		}
	}
	
	
	//method to get the matching url with wild-cards
	public HttpServlet getMatchingServlet(String url, StringBuilder s_path){
		HttpServlet result = null;
		for(String url_match : HttpServer.servlet_map.keySet()){
			//matching the wild card URLs
			if(url_match.contains("*")){
				String url_substr = url_match.substring(0, url_match.indexOf("*"));
				if(url_substr.endsWith("/")){
					url_substr = url_substr.substring(0, url_substr.length()-1);
				}
				if(url.startsWith(url_substr.toLowerCase())){
					result = HttpServer.servlet_map.get(url_match);
					s_path.append(url_substr);
				}
				
			}
		}
		return result;
	}
	
	//method to remove special characters from the url  string
	public String decodeQueryString(String query_string){
		StringBuilder result = new StringBuilder();
		for(int i=0; i< query_string.length();){
			//reading the next ascii after the %
			if(query_string.charAt(i) == '%'){
				try{
					//skipping % if not followed by valid ASCII
					if(i > query_string.length()-3){
						result.append(query_string.charAt(i));
						i++;
					}
					//else take the ascii character
					int ascii = Integer.parseInt(query_string.substring(i+1, i+3), 16);
					char ascii_char = (char) ascii;
					result.append(ascii_char);
					i+=3;
					continue;
				}
				//invalid ascii after percent
				catch(Exception e){
					result.append(query_string.charAt(i));
					log.warn("Percent not followed by valid Character ASCII");
					i++;
					continue;
				}
			}
			else if(query_string.charAt(i) == '+'){
				result.append(" ");
				i++;
			}
			else{
				result.append(query_string.charAt(i));
				i++;
			}
		}
		return result.toString();
	}
	
	//Method to call the requested servlet
	public boolean invokeServlet(HttpServlet servlet, StringBuilder servlet_path, PrintStream out){
		String path_trunc = truncatePattern(servlet_path);
		HttpSessionImpl session = null;
		Map<String, String> cookies = (Map<String, String>) parsed_request.get("cookies");
		if(cookies != null && cookies.containsKey("JSESSIONID")){
			String sessionid = (String)cookies.get("JSESSIONID");
			if(ApplicationConstants.sessions_table.containsKey(sessionid)){
				session = ApplicationConstants.sessions_table.get(sessionid);
				//session.setLastAccessTime(new Date().getTime());
			}
			
		}
		HttpServletRequestImpl servlet_request = new HttpServletRequestImpl(session, parsed_request, parsed_headers, path_trunc, HttpServer.sc);
		servlet_request.setRemoteAddr(remote_addr);
		servlet_request.setRemoteHost(host_name);
		servlet_request.setRemotePort(this.port);
		//initializing the parameters
		for(Entry<String, List<String>> params : parameter_values.entrySet()){
			for(String value : params.getValue()){
				servlet_request.setParameter(params.getKey(), value);
			}
			
		}
		HttpServletResponseImpl servlet_response = new HttpServletResponseImpl(out, parsed_request, servlet_request);
		try {
			//calling the servlet with the request and response objects
			servlet.service(servlet_request, servlet_response);
			servlet_response.flushBuffer();;
		} catch (ServletException | IOException e) {
			// TODO Auto-generated catch block
			log.error("Error in Service Method while invoking servlet" + e.getMessage());
			response_data.put("Error", 500);
			return false;
		}
		return true;
	}
	
	public String truncatePattern(StringBuilder input){
		//handle the URL pattern with wild card
		if(!(input.charAt(0)=='/'))input.insert(0, "/");
		if(input.toString().equals("/*")) return "";
		if(input.toString().endsWith("/")) return input.toString().substring(0, input.length()-1);
		if(input.toString().endsWith("/*")) return input.toString().substring(0, input.length()-2);
		else return input.toString();
	}
	
	
	//Not using this function anymore as just has to ensure host is present in the host headers
	/*public boolean checkHostAddress(String host_address){
		if(HttpServer.port == 80){
			if(host_address.toLowerCase().startsWith("http://" + server_name +":" + HttpServer.port) || host_address.toLowerCase().startsWith("http://" + server_name))
			    return true;
			else if(host_address.toLowerCase().startsWith(server_name +":" + HttpServer.port) || host_address.toLowerCase().startsWith(server_name))
				return true;
			else
				return false;
		}
		else{
			if(host_address.toLowerCase().startsWith("http://" + server_name +":"  + HttpServer.port))
			    return true;
			else if(host_address.toLowerCase().startsWith(server_name + ":" +  HttpServer.port))
				return true;
			else
				return false;
		}
	}*/
     
	//parse and fill the headers map
	public boolean parse_headers(String headers_text) {
		String[] header_lines = headers_text.split("\n");
		for (int i=0;i<header_lines.length;i++) {
			String header = header_lines[i];
			String[] tokens = header.split(":", 2);
			if (tokens.length == 2){
				String key = tokens[0].trim();
				StringBuilder value = new StringBuilder(tokens[1].trim());
				//parsing multiline headers
				while(i<header_lines.length-1 && header_lines[i+1].length()>0 && Character.isWhitespace(header_lines[i+1].charAt(0))){
					value = value.append(" " + header_lines[i+1].trim());
					i++;
				}
				String value_s = value.toString();
				//putting the header keys and values
				if(key.equalsIgnoreCase("set-cookie") || key.equalsIgnoreCase("cookie"))
					parsed_headers.put("cookie", value_s);
				else
					parsed_headers.put(key.toLowerCase(), value_s.toLowerCase());
			}
		}
		//Check if host is present in the headers
		Map<String, Object> request_line = (Map<String, Object>)parsed_request.get("requestline");
		String version = (String)request_line.get("version");
		if(version.equals("1.1")){
			if(!parsed_headers.containsKey("host")){ 
				response_data.put("Error", 400);
				return false;
			}
			else
			{
				return true;
			}
			//Not checking the host header value
			/*else{
				String host =  (String) parsed_request.get("host");
				if(!checkHostAddress(host)){
					response_data.put("Error", 400);
					return false;
				}
			}*/
		}
		return true;
	}
	
	public void parseCookies(){
		//this method parses and extracts the cookie from the request
		if(parsed_headers.containsKey("cookie")){
			try{
				Map<String, String> cookies_vals = new HashMap<String, String>();
				String cookies = (String)parsed_headers.get("cookie");
				//cookies with ; as delimeter
				String[] tokens = cookies.split(";");
				for(String token: tokens){
					String[] cookie_token = token.split("=");
					cookies_vals.put(cookie_token[0].trim(), cookie_token[1].trim());
					if(cookie_token[0].equals("JSESSIONID")){
						HttpSessionImpl session_obj = ApplicationConstants.sessions_table.get(cookie_token[1]);
						if(session_obj != null){
							//if the session object was found then make it as old
							session_obj.is_new = false;
						}
					}
				}
				parsed_request.put("cookies", cookies_vals);
			}
			catch(Exception e){
				log.error("Parsing cookie error, " + e.getMessage());
			}
			
		}
	}
	
	//Validating if the uri is valid and start with / in case of relative and http:// for absolute
	public boolean validate_uri(String uri, Map<String, Object> request_line_map){
		String version = (String)request_line_map.get("version");
		if(version.equals("1.0")){
			if (!(uri.startsWith("/"))) {
				response_data.put("Error", 400);
				return false;
			} else if (uri.contains("..")) {
				response_data.put("Error", 403);
				return false;
			} else {
				// request uri is valid
				request_line_map.put("request_uri", uri);
				threadStatus.setThread_status(uri);
				return true;
			}
		}
		else{
			//checking the URL format for absolute URL
			String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	        if(IsMatch(uri,regex)){
	            Pattern pat = Pattern.compile("(?:([^:\\/?#]+):)?(?:\\/\\/([^\\/?#]*))?([^?#]*)(?:\\?([^#]*))?(?:#(.*))?");
	            //getting the path after domain name
	    	    String request_uri = getPathURI(uri, pat);
	    	    //appending the slash
	    	    if(request_uri.equals("")) request_uri = "/";
	    	    if (request_uri.contains("..")) {
					response_data.put("Error", 403);
					return false;
	    	    }
	    	    request_line_map.put("request_uri", request_uri);
				threadStatus.setThread_status(request_uri);
	        	return true;
	        }
	        else{
	        	// if the URL is not absolute
	        	if (!(uri.startsWith("/"))) {
					response_data.put("Error", 400);
					return false;
				} else if (uri.contains("..")) {
					response_data.put("Error", 403);
					return false;
				} else {
					// request uri is valid
					request_line_map.put("request_uri", uri);
					threadStatus.setThread_status(uri);
					return true;
				}
	        }
	        }
		
	}
	  //get path from the URL
	  public String getPathURI(String url, Pattern p) {
	        Matcher m = p.matcher(url);

	        if (m.find()) {
	            return m.group(3);
	        }
	        return null;
	    }

   //matching the URL pattern
	private static boolean IsMatch(String s, String pattern) {
        try {
            Pattern pat = Pattern.compile(pattern);
            Matcher matcher = pat.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
        return false;
        }
    } 

	// Method to parse the first line of the request
	public boolean parse_request_line(String request_line,
			Map<String, Object> request_line_map) {

		String[] tokens = request_line.split("\\s+");
		// checking if the request line has three components
		if (tokens.length != 3) {
			response_data.put("Error", 400);
			return false;
		}
		// only GET, POST and HEAD will be valid
		if (!methods.contains(tokens[0])) {
			response_data.put("Error", 501);
			return false;
		} else {
			request_line_map.put("method", tokens[0]);
			method_name = tokens[0];
		}

		// Request URI should start with a backslash
	

		String[] version_tokens = tokens[2].split("/");
		if (version_tokens.length != 2) {
			response_data.put("Error", 400);
			return false;
		}
		if (!version_tokens[0].equals("HTTP")) {
			response_data.put("Error", 400);
			return false;
		}
		if (!version_tokens[1].equals("1.0")
				&& !version_tokens[1].equals("1.1")) {
			response_data.put("Error", 505);
			return false;
		} else {
			request_line_map.put("version", version_tokens[1]);
		}

		return validate_uri(tokens[1].toLowerCase(), request_line_map);
	}

	// Following method is called in casse of error HTTP response
	public String formErrorResponse(Map<String, Object> response_map) {

		String lf = System.getProperty("line.separator");
		StringBuilder response = new StringBuilder();
		// getting the error code
		Integer error_code = (Integer) response_map.get("Error");
		// Integer error_code = 400;
		String html = new String(
				"<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>"
						+ error_code + " "
						+ ApplicationConstants.ErrorCodes.get(error_code)
						+ "</title></head><body><h1>" + error_code + " "
						+ ApplicationConstants.ErrorCodes.get(error_code)
						+ "</h1></body></html>");
		response.append("HTTP/1.1 " + error_code + " "
				+ ApplicationConstants.ErrorCodes.get(error_code));
		response.append(lf);
		response.append("Server: Harsh/1.0.0 (Ubuntu64)" + lf);
		response.append("Date: " + date_curr + lf);
		response.append("Content-Type: text/html" + lf);
		response.append("Content-Length: " + html.length() + lf);
		response.append("Connection: close" + lf);
		response.append(lf);
		//no body for 304
		if(error_code != 304 && !method_name.equals("HEAD")) 
			response.append(html);	
		return response.toString();

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
	
	//method for if modified since
    public boolean modifiedSince(File resource){
    	String date_string = null;
    	if(parsed_headers.containsKey("if-modified-since")){
    		date_string = (String) parsed_headers.get("if-modified-since");
    	}
    	else{
    		return true;
    	}
    	
    	//Parsing the date
		Date req_date = parseDate(date_string);
		if(req_date == null){
			response_data.put("Error", 400);
			return false;
		}
		Date curr_date = new Date();
		if(req_date.after(curr_date)) return true;
		Date modified_date = new Date(resource.lastModified());
		//checking if modified date is after the requested date
		if(modified_date.after(req_date)) return true;
		else{
			response_data.put("Error", 304);
			return false;
		}
		
    	
    }
    
  //method for if unmodified since
    public boolean unModifiedSince(File resource){
    	String date_string = null;
    	if(parsed_headers.containsKey("if-unmodified-since")){
    		date_string = (String) parsed_headers.get("if-unmodified-since");
    	}
    	else{
    		return true;
    	}
    	
		Date req_date = parseDate(date_string);
		if(req_date == null){
			response_data.put("Error", 400);
			return false;
		}
		Date curr_date = new Date();
		if(req_date.after(curr_date)) return true;
		Date modified_date = new Date(resource.lastModified());
		//checking if modified date is after the requested date
		if(modified_date.after(req_date)){
			response_data.put("Error", 412);
			return false;
		}
		else{
			return true;
		}
    	
    }
    
    //handling the 100 continue
    public boolean process_100_continue(PrintStream out){
    	if(!parsed_headers.containsKey("expect")){
    		return true;
    	}
    	Map<String, Object> request_line = (Map<String, Object>)parsed_request.get("requestline");
		String version = (String)request_line.get("version");
		//sending 417 if receive from 1.0 client
    	if(!version.equals("1.1")){
    		response_data.put("Error", 417);
			return false;
    	}
    	String lf = System.getProperty("line.separator");
    	
    	//checking for the Expect header value
		String value = (String) parsed_headers.get("expect");
		if(value.equals("100-continue")){
			//sending the 100 continue response before the actual response
			out.print("HTTP/1.1 100 Continue" + lf);
			out.print(lf);
			out.flush();
			return true;
		}
    	//if expect contains anything else than 100-continue for 1.1 clients send 417
		response_data.put("Error", 417);
		return false;
    }
    
    
	// Serve the file or dir contents
	public boolean serve_request(PrintStream out) {
		if(!process_100_continue(out)){
			return false;
		}
		//getting the map for request line
		Map<String, Object> request_line = (Map<String, Object>)parsed_request.get("requestline");
		String request_uri = (String)request_line.get("request_uri");
		//replacing the %20 with spaces
		request_uri = request_uri.replaceAll("%20", " ");
		request_uri = request_uri.split("\\?")[0];
		String path = HttpServer.work_dir + request_uri;
		
		//checking if the request is for shutdown or control
		if(ApplicationConstants.special_urls.contains(request_uri.toLowerCase())){
			return serve_special_requests(request_uri, out);
		}
		//String path = uri.split("\\?")[0];
		File resource = new File(path);

		// checking if the resource exists
		if (!resource.exists()) {
			log.error("Not able to find the requested resouce");
			response_data.put("Error", 404);
			return false;
		}
		
		//checking the if modified since and if unmodified since headers and sending the apt response
        if(!modifiedSince(resource)){
        	return false;
        }
        if(!unModifiedSince(resource)){
        	return false;
        }
        //checking and assigning the correct mime type
		if (resource.isFile()) {
			//if file does not have any extension, send content type as octet stream
			if (!resource.getName().contains(".")) {
				response_data.put("Content-Type", "application/octet-stream");
				return serve_file(resource, out);
			}
			
			//finding the extension
			String format = path.substring(path.lastIndexOf('.') + 1);
			if (ApplicationConstants.MimeTypes
					.containsKey(format.toLowerCase())) {
				//returning the correct mime type
				String mimetype = ApplicationConstants.MimeTypes.get(format
						.toLowerCase());
				response_data.put("Content-Type", mimetype);
				return serve_file(resource, out);
			} else {
				//if MIME type does not matches anything then send octet stream content type
				response_data.put("Content-Type", "application/octet-stream");
				return serve_file(resource, out);
				//response_data.put("Error", 415);
				//return false;
			}
		} else {
			//serve the dir
			serve_dir(resource, out, request_uri);
		}
		return true;

	}
	
	
	//function to server the files
	public boolean serve_file(File file, PrintStream out){
		Path path = file.toPath();
		try {
			//reading the bytes from the file
			if(!file.canRead()){
				response_data.put("Error", 403);
				return false;
			}
			byte[] file_bytes = Files.readAllBytes(path);
			String lf = System.getProperty("line.separator");
			StringBuilder response = new StringBuilder();
			
			//Building the response
			Integer status_code = 200;
			response.append("HTTP/1.1 " + status_code + " "
					+ ApplicationConstants.ErrorCodes.get(status_code));
			response.append(lf);
			response.append("Server: Harsh/1.0.0 (Ubuntu64)" + lf);
			response.append("Date: " + date_curr + lf);
			response.append("Content-Type: " + response_data.get("Content-Type") + lf);
			response.append("Content-Length: " + file_bytes.length + lf);
			response.append("Last-Modified: " + getDateString(new Date(file.lastModified())) + lf);
			response.append("Connection: close" + lf);
	
			//Sending back the result to the server
			out.print(response.toString());
			out.print(lf);
			//Not sending the request body in case of HEAD
			if(method_name.equals("HEAD")) return true;
			out.write(file_bytes);
			return true;
			
		} catch (IOException e) {
			log.error("Error while reading the file");
			response_data.put("Error", 500);
			return false;
		}
	}

	//serving the list of entities in a directory
	public void serve_dir(File dir, PrintStream out, String request_uri) {
		StringBuilder html = new StringBuilder(
				"<!DOCTYPE html><html><head><title>" +dir.getName() + "</title></head><body><h2>Files and Directories(Directories end with /)</h2><ul style=\"list-style-type:circle\">");
		//Getting the list of files
		File[] files = dir.listFiles();
		String sep = "";
		if(!request_uri.equals("/")) sep = "/";		
		//forming the response
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				html.append("<li><a href=\"" + request_uri + sep + files[i].getName() + "\">" + files[i].getName() +  "</a></li>");
			} else if (files[i].isDirectory()) {
				html.append("<li><a href=\"" + request_uri + sep + files[i].getName() + "\">" + files[i].getName() + "/" + "</a></li>");
			}
		}
		html.append("</ul></body></html>");
		String lf = System.getProperty("line.separator");
		StringBuilder response = new StringBuilder();
		
		//setting up the headers
		Integer status_code = 200;
		response.append("HTTP/1.1 " + status_code + " "
				+ ApplicationConstants.ErrorCodes.get(status_code));
		response.append(lf);
		response.append("Server: Harsh/1.0.0 (Ubuntu64)" + lf);
		response.append("Date: " + date_curr + lf);
		response.append("Content-Type: text/html" + lf);
		response.append("Content-Length: " + html.length() + lf);
		response.append("Last-Modified: " + getDateString(new Date(dir.lastModified())) + lf);
		response.append("Connection: close" + lf);
		response.append(lf);
		//Not sending the request body in case of HEAD
		if(!method_name.equals("HEAD"))
			response.append(html);
		//Sending back the result to the server
		out.print(response.toString());
		return;
		
	}
	
	public boolean serve_special_requests(String request_uri, PrintStream out){
		
        StringBuilder html = null;

		if(request_uri.equalsIgnoreCase("/control")){
			html = new StringBuilder("<!DOCTYPE html><html><head><title>Control Panel</title></head><body><h2>Server of Harsh Verma (hverma)</h2><table><tbody><tr><th>Thread Name</th><th>Status</th></tr>");
			List<ThreadStatus> thread_list = getThreadStatus();
			for(ThreadStatus thread_stat: thread_list){
				html.append("<tr><td>" + thread_stat.getThread_name() + "</td><td>" + thread_stat.getThread_status() + "</td></tr>");
			}
			html.append("</tbody></table><br/><form action=\"/shutdown\"><input type=\"submit\" value=\"Shutdown\"></form>");	
			String logcontent = readLogFiles();
			html.append("<h3>Logs Recorded:</h3><p>" + logcontent + "</p>" + "</body></html>");
		}
		else{
			try {
				//System.out.println("Thread doing the shutdown is: " + Thread.currentThread().getName());
				destroyServlets();
				HttpServer.pool.stopAllThreads();
				//shutting down the main socket thread
				HttpServer.server_running = false;
				HttpServer.closeSocket();
				html = new StringBuilder(
						"<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>Server shut down</title></head><body><h1>Server shut down successful</h1></body></html>");
				//System.out.println("Server is shut down successfully");
			} catch (IOException e) {
				System.out.println("Error while performing shutdown: "+ e.getMessage());
			}
		}
		if(!method_name.equals("HEAD"))
		    response_data.put("body", html);
		response_data.put("Content-Type", "text/html");
		response_data.put("Content-Length", html.length());
		response_data.put("Status", 200);
		String response = form_response(response_data);
		out.print(response);
		return true;
	}
	
	public String readLogFiles(){
		BufferedReader br = null;
		FileReader filereader = null;
		StringBuilder sb = new StringBuilder();
		try{
			String filepath = ApplicationConstants.logfile + "/log/log.out";
			filereader = new FileReader(filepath);
			br = new BufferedReader(filereader);
			String line = null;
			while(true){
				line = br.readLine();
				if(line == null) break;
				sb.append(line);
				sb.append("<br>");
				
			}
		}
		catch(Exception e){
			log.error("Error reading the log file " + e.getMessage());
		}
		finally{
			try {
				br.close();
				filereader.close();
			} catch (IOException e) {
				log.error("Error while closing the log file");
			}
			
		}
		return sb.toString();
	}
	
	
	//this method destroys the sevlets
	public void destroyServlets(){
		for(Entry<String, HttpServlet> entry : HttpServer.servlet_map.entrySet()){
			//destroying everything
			entry.getValue().destroy();
		}
	}
	
	//Getting the thread status
	public List<ThreadStatus> getThreadStatus(){
		List<ThreadStatus> result = new ArrayList<ThreadStatus>();
		ThreadPool curr_pool = HttpServer.pool;
		//collect name and status of all threads
		for(WorkerThread wt: curr_pool.pool){
			ThreadStatus ts= new ThreadStatus(wt.status.getThread_name(), wt.status.getThread_status());
			result.add(ts);
		}
		return result;
	}
	
	//form the response from the response map
	public String form_response(Map<String, Object> response_data){
		String lf = System.getProperty("line.separator");
		StringBuilder response = new StringBuilder();
		response.append("HTTP/1.1 " + response_data.get("Status") + " "+ ApplicationConstants.ErrorCodes.get(response_data.get("Status")));
		response_data.remove("Status");
		response.append(lf);
		for(Entry<String, Object> entry : response_data.entrySet()){
			if(!entry.getKey().equals("body")){
				response.append(entry.getKey() + ": " + entry.getValue() + lf);
			}
			
		}
		response.append(lf);
		if(response_data.containsKey("body"))
	 	    response.append(response_data.get("body"));
		return response.toString();
	}

}
