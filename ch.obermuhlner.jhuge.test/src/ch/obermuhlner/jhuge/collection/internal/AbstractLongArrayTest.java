package ch.obermuhlner.jhuge.collection.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Abstract base class to test {@link LongArray}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractLongArrayTest {

	protected LongArray createLongArray() {
		return createLongArray(8);
	}
	
	protected abstract LongArray createLongArray(int capacity);
	
	@Test
	public void testSize() {
		LongArray array = createLongArray();
		
		assertEquals(0, array.size());
		
		array.add(1);
		array.add(2);
		array.add(3);
		
		assertEquals(3, array.size());
	}
	
	@Test
	public void testGet() {
		LongArray array = createLongArray();

		array.add(1);
		array.add(2);
		array.add(3);

		assertEquals(1, array.get(0));
		assertEquals(2, array.get(1));
		assertEquals(3, array.get(2));
	}

	@Test
	public void testAdd_index() {
		LongArray array = createLongArray();

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
		LongArray array = createLongArray();

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
		LongArray array = createLongArray();

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
		LongArray array = createLongArray();

		array.add(1000);
		array.add(1001);
		array.add(1002);

		assertEquals(0, array.indexOf(1000));
		assertEquals(1, array.indexOf(1001));
		assertEquals(2, array.indexOf(1002));
		assertEquals(-1, array.indexOf(-99));
	}
	
	@Test
	public void testClear() {
		LongArray array = createLongArray();

		array.add(1);
		array.add(2);
		array.add(3);

		array.clear();
		assertEquals(0, array.size());
	}

	@Test
	public void testGet_IndexOutOfBoundsException() {
		LongArray array = createLongArray();

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
		LongArray array = createLongArray();

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
		LongArray array = createLongArray();

		assertNotNull(array.toString());
	}
	
	@Test
	public void testLarge() {
		LongArray array = createLongArray();
		
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
