package test.edu.upenn.cis455.hw1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.webserver.RequestProcessor;
import junit.framework.TestCase;



public class RequestProcessorTest extends TestCase{
    RequestProcessor rp = null;
	@Before
	public void setUp() throws Exception {
		rp = new RequestProcessor();
	}

	@After
	public void tearDown() throws Exception {
		rp = null;
	}

	@Test
	public void testGetDateString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
		try {
			Date date = sdf.parse("20/02/2017 21:00:00 GMT");
			String date_string = rp.getDateString(date);
			assertEquals("Date not properly parsed", "Mon, 20 Feb 2017 21:00:00 GMT", date_string);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			fail("Error in parsing the date");
		}
		
	}

	@Test
	public void testDecodeQueryString() {
		String encoded_string = "a=1%202&b=3%20%%202";
	    String decoded_string = rp.decodeQueryString(encoded_string);
	    assertEquals("Query String not decoded properly", "a=1 2&b=3 % 2",decoded_string);
		
	}


}
