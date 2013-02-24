package ch.obermuhlner.jhuge.collection.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Abstract base class to test {@link IntArray}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractIntArrayTest {

	protected IntArray createIntArray() {
		return createIntArray(8);
	}
	
	protected abstract IntArray createIntArray(int capacity);
	
	@Test
	public void testSize() {
		IntArray array = createIntArray();
		
		assertEquals(0, array.size());
		
		array.add(1);
		array.add(2);
		array.add(3);
		
		assertEquals(3, array.size());
	}
	
	@Test
	public void testGet() {
		IntArray array = createIntArray();

		array.add(1);
		array.add(2);
		array.add(3);

		assertEquals(1, array.get(0));
		assertEquals(2, array.get(1));
		assertEquals(3, array.get(2));
	}

	@Test
	public void testAdd_index() {
		IntArray array = createIntArray();

		array.add(1);
		array.add(2);
		array.add(3);

		array.add(1, 99);
		
		assertEquals(4, array.size());
		assertEquals(1, array.get(0));
		assertEquals(99, array.get(1));
		assertEquals(2, array.get(2));
		assertEquals(3, array.get(3));
	}

	@Test
	public void testRemove() {
		IntArray array = createIntArray();

		array.add(1);
		array.add(2);
		array.add(3);

		assertEquals(2, array.remove(1));
		assertEquals(2, array.size());
		assertEquals(1, array.get(0));
		assertEquals(3, array.get(1));
	}

	@Test
	public void testSet() {
		IntArray array = createIntArray();

		array.add(1);
		array.add(2);
		array.add(3);

		assertEquals(2, array.set(1, 100));
		assertEquals(3, array.size());
		assertEquals(1, array.get(0));
		assertEquals(100, array.get(1));
		assertEquals(3, array.get(2));
	}
	
	@Test
	public void testIndexOf() {
		IntArray array = createIntArray();

		array.add(1000);
		array.add(1001);
		array.add(1002);

		assertEquals(0, array.indexOf(1000));
		assertEquals(1, array.indexOf(1001));
		assertEquals(2, array.indexOf(1002));
		assertEquals(-1, array.indexOf(-99));
	}
	
	@Test
	public void testToArray() {
		IntArray array = createIntArray();

		array.add(1000);
		array.add(1001);
		array.add(1002);

		int[] snapshot = array.toArray();
		assertEquals(3, snapshot.length);
		assertEquals(1000, snapshot[0]);
		assertEquals(1001, snapshot[1]);
		assertEquals(1002, snapshot[2]);
		
		snapshot[0] = 99;
		snapshot[1] = 99;
		snapshot[2] = 99;
		
		assertEquals(1000, array.get(0));
		assertEquals(1001, array.get(1));
		assertEquals(1002, array.get(2));
	}
	
	@Test
	public void testClear() {
		IntArray array = createIntArray();

		array.add(1);
		array.add(2);
		array.add(3);

		array.clear();
		assertEquals(0, array.size());
	}

	@Test
	public void testGet_IndexOutOfBoundsException() {
		IntArray array = createIntArray();

		try {
			array.get(0);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException exception) {
			// expected
		}

		array.add(1);
		array.add(2);
		array.add(3);

		try {
			array.get(-1);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException exception) {
			// expected
		}

		try {
			array.get(3);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException exception) {
			// expected
		}
	}
	
	@Test
	public void testAdd_IndexOutOfBoundsException() {
		IntArray array = createIntArray();

		try {
			array.add(1, 99);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException exception) {
			// expected
		}

		array.add(0, 1);
		array.add(1, 2);
		array.add(2, 3);

		try {
			array.add(-1, 99);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException exception) {
			// expected
		}

		try {
			array.add(4, 99);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException exception) {
			// expected
		}
	}

	
	@Test
	public void testToString() {
		IntArray array = createIntArray();
		
		assertNotNull(array.toString());
	}
	
	@Test
	public void testLarge() {
		IntArray array = createIntArray();
		
		for (int i = 0; i < 1000; i++) {
			array.add(i);
			assertEquals(i + 1, array.size());
			assertEquals(i, array.get(i));
		}
		
		assertEquals(1000, array.size());
		for (int i = 0; i < 1000; i++) {
			assertEquals(i, array.get(i));
		}
	}
}
