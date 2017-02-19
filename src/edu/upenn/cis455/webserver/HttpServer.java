package edu.upenn.cis455.webserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


class HttpServer {

	public static Integer port = 0;
	public static ServletContextImpl sc = null;
	public static String work_dir = "";
	public static boolean server_running = true;
	public static ThreadPool pool = null;
    public static ServerSocket server_sock_plain = null;
    public static ServerSocket server_sock_ssl = null;
    public static String webxml = null;
    //map to store the servlet names and the corresponding HTTPServlets objects
    public static Map<String, HttpServlet> servlet_map = new HashMap<String, HttpServlet>();
    
    private static void usage() {
		System.err.println("usage: java TestHarness <path to web.xml> " 
				+ "[<GET|POST> <servlet?params> ...]");
	}
	public static void main(String args[]) {
		
		// Checking the right number of parameters
		if (args.length != 3 && args.length%2 != 0) {
			usage();
		}
		//assigning port and work dir
		else if (args.length == 2) {
			try{
				port = Integer.valueOf(args[0].trim());
				//Checking the port to be valid
				if(port < 1){
					System.out.println("Error: Port should be positive !");
					System.exit(1);
				}
				if(args[1].charAt(args[1].length()-1) == '/') args[1] = args[1].substring(0, args[1].length()-1); 		
				work_dir = args[1];
			}
			catch(NumberFormatException e){
				//Arguments are not correct
				System.out.println("Run the jar with the arguments: Port Root_Web_Directory (Port should be a Positive Number)");
				System.exit(1);
			}

		} else if(args.length == 0) {
			//Printing the server information
			System.out.println("Harsh Verma");
			System.out.println("hverma");
			System.exit(1);
		}
		
		else{
			try{
				port = Integer.valueOf(args[0].trim());
				//Checking the port to be valid
				if(port < 1){
					System.out.println("Error: Port should be positive !");
					System.exit(1);
				}
				if(args[1].charAt(args[1].length()-1) == '/') args[1] = args[1].substring(0, args[1].length()-1); 		
				work_dir = args[1];
				webxml = args[2];
				Handler h = parseWebdotxml(webxml);
				loadServlets(h);
				startServer();
				System.out.println("done");
				
				
				
			}
			catch(Exception e){
				//Arguments are not correct
				System.out.println("Run the jar with the arguments: Port Root_Web_Directory (Port should be a Positive Number)");
				System.exit(1);
			}
		}
		
		
       
		
	}
	
	private static void loadServlets(Handler h){
		//Creating the servlet context
		sc = createContext(h);
		try {
			HttpServer.servlet_map = createServlets(h, sc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//function to create the servlet context given the handler
	private static ServletContextImpl createContext(Handler h) {
		ServletContextImpl fc = new ServletContextImpl(h.display_name);
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		}
		return fc;
	}
	
	public static HashMap<String,HttpServlet> createServlets(Handler h, ServletContextImpl fc) throws Exception{
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (Entry<String, String> entry : h.m_servlet_url.entrySet()) {
			String servletName = entry.getValue();
			ServletConfigImpl config = new ServletConfigImpl(servletName, fc);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(entry.getKey(), servlet);
		}
		return servlets;
	}
	
	
	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	static class Handler extends DefaultHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				m_state = m_state == 5 ? 6 : 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 10) ? 11 : 21;
			}
			else if (qName.compareTo("servlet-mapping") == 0){
				m_state = 5;
			}
			else if (qName.compareTo("url-pattern") == 0){
				m_state = 7;
			}
			else if (qName.compareTo("load-on-startup") == 0){
				load_startup.add(m_servletName);
			}
			else if (qName.compareTo("display-name") == 0){
				m_state = 9;
			}
		}
		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 1) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 6) {
				m_servlet_url_name = value;
				m_state = 0;
			}else if (m_state == 7) {
				m_servlet_url.put(value, m_servlet_url_name);
				m_servlet_url_name = null;
				m_state = 0;
			}
			else if (m_state == 9) {
				display_name = value;
				m_state = 0;
			}
			else if (m_state == 2) {
				m_servlets.put(m_servletName, value);
				m_state = 0;
			}
			else if (m_state == 10 || m_state == 20) {
				m_paramName = value;
			} else if (m_state == 11) {
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 21) {
				if (m_paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = m_servletParams.get(m_servletName);
				if (p == null) {
					p = new HashMap<String,String>();
					m_servletParams.put(m_servletName, p);
				}
				p.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			}
		}
		private int m_state = 0;
		private String m_servletName;
		private String m_paramName;
		private String m_servlet_url_name;
		HashMap<String,String> m_servlets = new HashMap<String,String>();
		HashMap<String,String> m_contextParams = new HashMap<String,String>();
		HashMap<String,String> m_servlet_url = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();
		Set<String> load_startup = new HashSet<String>();
		String display_name = null;
	}
	
	public static void startServer(){
		try{
		//Initiating the Blocking queue
	    BlockingQueue bq = new BlockingQueue(20000);
	    //Initiating the thread pool
	    ThreadPool pool = new ThreadPool(10, bq);
	    HttpServer.pool = pool;
		server_sock_plain = new ServerSocket(port, 60000);
	    
		//Main while loop to accept connections
		while (server_running) {
			Socket client_sock = null;
            try {
            	//accepting the client socket and enquing the client socket
				client_sock = server_sock_plain.accept();
				bq.enqueue(client_sock);		
				
				
			} 
            catch (InterruptedException e){
    			System.out.println("The waiting thread was interrupted: "
    					+ e.getMessage());
    			e.getStackTrace();
    		}  
		}
	}
		catch (IOException e) {
			if(!server_running){
				System.out.println("Server Shutdown successfully");
			}
			else{
			    System.out.println("The main server socket failed with error: "
							+ e.getMessage());
			    e.printStackTrace();
			}
		}
	}
	
	//This function gets called while shutdown by one of the worker threads
	public static void closeSocket() throws IOException{
		//Close the corresponding socket
		if(ApplicationConstants.useHTTPs)
			server_sock_ssl.close();
		else
			server_sock_plain.close();
	}

}
