package edu.upenn.cis455.webserver;

import org.apache.log4j.Logger;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
public class TestLog {
	/* Get actual class name to be printed on */
	 static Logger log = Logger.getLogger(TestLog.class.getName());
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		log.debug("Hello this is a debug message");
	    log.info("Hello this is an info message");
	    log.error("Hello this is an error message");

	}

}
