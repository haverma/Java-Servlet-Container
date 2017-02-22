package test.edu.upenn.cis455.hw1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.webserver.BlockingQueue;
import junit.framework.TestCase;

public class BlockingQueueTest extends TestCase{
    BlockingQueue bq = null;
	@Before
	public void setUp() throws Exception {
		bq = new BlockingQueue(4);
	}

	@After
	public void tearDown() throws Exception {
		bq = null;
	}

	@Test
	public void testEnqueue() {
		try {
			bq.enqueue(new Object());
			assertEquals("Enque not successful", 1, bq.getSize());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail("Enque not successful");
		}
	}

	@Test
	public void testDeqeue() {
		try {
			bq.enqueue(new Object());
			bq.deqeue();
			assertEquals("Deque not successful", 0, bq.getSize());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail("Deque not successful");
		}
	}

}
