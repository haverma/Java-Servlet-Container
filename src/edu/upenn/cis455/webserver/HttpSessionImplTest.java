package edu.upenn.cis455.webserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpSessionImplTest {
    HttpSessionImpl session = null;
	@Before
	public void setUp() throws Exception {
		ServletContextImpl sc = new ServletContextImpl("test");
		session = new HttpSessionImpl(sc);
	}

	@After
	public void tearDown() throws Exception {
		session = null;
	}

	@Test
	public void testSetAttribute() {
		session.setAttribute("attr1", 1);
		assertEquals("Attribute not set correctly", 1, session.getAttribute("attr1"));
	}

	@Test
	public void testInvalidate() {
		session.invalidate();
		try{
			session.invalidate();
			fail("Cannot invalidate a invalidated session");
		}
		catch(IllegalStateException e){
			//Passed
		}
	}
	
	@Test
	public void testSessionInactiveTime(){
		session.setMaxInactiveInterval(4);
		Date cur_date = new Date();
		session.setLastAccessTime(cur_date.getTime() - 5*1000);//going past the max inactive time
		try{
			session.setAttribute("attr1", 1);
			session.getAttribute("attr1");
			fail("Cannot set or get attribute from invalid session");
		}
		catch(IllegalStateException e){
			//Passed
		}
		
	}

}
