package test.edu.upenn.cis455.hw1;

import static org.junit.Assert.assertEquals;

import java.io.PrintStream;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.webserver.HttpServletRequestImpl;
import edu.upenn.cis455.webserver.HttpServletResponseImpl;
import junit.framework.TestCase;

public class HttpServletResponseImplTest  extends TestCase {
    HttpServletResponseImpl response = null;
	@Before
	public void setUp() throws Exception {
		PrintStream out = System.out;//this is a mock outputstream
		response = new HttpServletResponseImpl(out, new HashMap<String, Object>(),  new HttpServletRequestImpl());
	}

	@After
	public void tearDown() throws Exception {
		response = null;
	}

	@Test
	public void testSetContentType() {
		assertEquals("Content Type should be text/html by default", "text/html", response.getContentType());
        response.setContentType("image/png");
        assertEquals("Content Type should be equal to set value", "image/png", response.getContentType());
	}

	
	@Test
	public void testSetBufferSize() {
		response.setBufferSize(4);
		assertEquals("Buffer Size not same as value set", 4, response.getBufferSize());
	}
	
	@Test
	public void testSetCharacterEncoding() {
		assertEquals("Character encoding should be ISO-8859-1 by default", "ISO-8859-1", response.getCharacterEncoding());
        response.setContentType("UTF-8");
        assertEquals("Character Encoding should be equal to set value", "UTF-8", response.getContentType());
	}

	

}
