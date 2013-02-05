package ch.obermuhlner.jhuge.collection.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ch.obermuhlner.jhuge.memory.DummyMemoryManager;

/**
 * Tests {@link HugeLongArray}.
 */
@SuppressWarnings("javadoc")
public class HugeLongArrayTest extends AbstractLongArrayTest {

	@Override
	protected LongArray createLongArray(int capacity) {
		return new HugeLongArray(new DummyMemoryManager());
	}
	
	@Test
	public void testSetSize() {
		HugeLongArray hugeLongArray = new HugeLongArray(new DummyMemoryManager(), 8);
		
		hugeLongArray.add(1000);
		hugeLongArray.add(1001);
		hugeLongArray.add(1002);
		hugeLongArray.add(1003);
		assertEquals(4, hugeLongArray.size());
		
		hugeLongArray.setSize(2);
		assertEquals(2, hugeLongArray.size());
		assertEquals(1000, hugeLongArray.get(0));
		assertEquals(1001, hugeLongArray.get(1));
		try {
			hugeLongArray.get(2);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException exception) {
			// expected
		}
		
		hugeLongArray.setSize(6);
		assertEquals(6, hugeLongArray.size());
		assertEquals(1000, hugeLongArray.get(0));
		assertEquals(1001, hugeLongArray.get(1));
		assertEquals(0, hugeLongArray.get(2));
		assertEquals(0, hugeLongArray.get(3));
		assertEquals(0, hugeLongArray.get(4));
		assertEquals(0, hugeLongArray.get(5));
		try {
			hugeLongArray.get(6);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException exception) {
			// expected
		}
		
		hugeLongArray.setSize(10);
		assertEquals(10, hugeLongArray.size());
		assertEquals(1000, hugeLongArray.get(0));
		assertEquals(1001, hugeLongArray.get(1));
		assertEquals(0, hugeLongArray.get(2));
		assertEquals(0, hugeLongArray.get(3));
		assertEquals(0, hugeLongArray.get(4));
		assertEquals(0, hugeLongArray.get(5));
		assertEquals(0, hugeLongArray.get(6));
		assertEquals(0, hugeLongArray.get(7));
		assertEquals(0, hugeLongArray.get(8));
		assertEquals(0, hugeLongArray.get(9));
		try {
			hugeLongArray.get(10);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException exception) {
			// expected
		}
	}
}
