package edu.upenn.cis455.webserver;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class DemoServlet2 extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>Simple Servlet 2</TITLE></HEAD><BODY>");
		out.println("<P>Hello! 2</P>");
		out.println("</BODY></HTML>");	
		//response.sendRedirect("/session1");
		
	}
}
