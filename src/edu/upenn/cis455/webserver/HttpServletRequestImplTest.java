package edu.upenn.cis455.webserver;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpServletRequestImplTest {
	HttpServletRequestImpl httpreq;
	@Before
	public void setUp() throws Exception {
		httpreq = new HttpServletRequestImpl();
	}

	@After
	public void tearDown() throws Exception {
		httpreq = null;
	}

	@Test
	public void testSetContentType() {
		//assertEquals("Content Type should be text/html by default", "text/html", httpreq.getContentType());
        httpreq.setContentType("image/png");
        assertEquals("Content Type should be equal to set value", "image/png", httpreq.getContentType());
	}

	@Test
	public void testSetCharacterEncoding() {
		assertEquals("Character Encoding should be ISO-8859-1 by default","ISO-8859-1", httpreq.getCharacterEncoding());
		try {
			httpreq.setCharacterEncoding("UTF-8");
			assertEquals("Character Encoding should be equal to the set value", "UTF-8", httpreq.getCharacterEncoding());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAuthType(){
		assertEquals("Auth Type not correct", "BASIC_AUTH", httpreq.getAuthType());
	}


}
