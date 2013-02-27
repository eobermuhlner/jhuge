package ch.obermuhlner.jhuge.collection.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static ch.obermuhlner.jhuge.converter.AbstractSerializableConverterTest.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Test;

/**
 * Abstract base class to test {@link IntLongArrayMap}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractIntLongArrayMapTest {

	private static final boolean DEBUG = false;
	
	private static final long[] ARRAY_0 = { };
	private static final long[] ARRAY_1 = { 1L };
	private static final long[] ARRAY_2 = { 2L, 22L };
	private static final long[] ARRAY_3 = { 3L, 33L, 333L };
	
	/**
	 * Creates the {@link IntLongArrayMap} to test.
	 * 
	 * @return the created {@link IntLongArrayMap}
	 */
	protected abstract IntLongArrayMap createIntLongArrayMap();
	
	@Test
	public void testSize() {
		IntLongArrayMap map = createIntLongArrayMap();
		
		assertEquals(0, map.size());
		
		map.put(0, ARRAY_0);
		map.put(1, ARRAY_1);
		map.put(2, ARRAY_2);
		map.put(3, ARRAY_3);

		assertEquals(4, map.size());

		map.remove(2);
		
		assertEquals(3, map.size());
		
		map.put(1, ARRAY_3);
		
		assertEquals(3, map.size());
		
		map.clear();
		
		assertEquals(0, map.size());
	}
	
	@Test
	public void testContainsKey() {
		IntLongArrayMap map = createIntLongArrayMap();
		
		map.put(0, ARRAY_0);
		map.put(1, ARRAY_1);
		map.put(2, ARRAY_2);
		
		assertEquals(true, map.containsKey(0));
		assertEquals(true, map.containsKey(1));
		assertEquals(true, map.containsKey(2));
		assertEquals(false, map.containsKey(3));
		assertEquals(false, map.containsKey(-1));
		assertEquals(false, map.containsKey(Integer.MIN_VALUE));
		assertEquals(false, map.containsKey(Integer.MAX_VALUE));
	}
	
	@Test
	public void testGet() {
		IntLongArrayMap map = createIntLongArrayMap();
		
		map.put(0, ARRAY_0);
		map.put(1, ARRAY_1);
		map.put(2, ARRAY_2);
		
		assertArrayEquals(ARRAY_0, map.get(0));
		assertArrayEquals(ARRAY_1, map.get(1));
		assertArrayEquals(ARRAY_2, map.get(2));
		assertEquals(null, map.get(3));
		assertEquals(null, map.get(-1));
		assertEquals(null, map.get(Integer.MIN_VALUE));
		assertEquals(null, map.get(Integer.MAX_VALUE));
	}
	
	@Test
	public void testPut() {
		IntLongArrayMap map = createIntLongArrayMap();
		
		map.put(1, ARRAY_0);

		assertEquals(1, map.size());
		assertEquals(true, map.containsKey(1));
		assertArrayEquals(ARRAY_0, map.get(1));

		map.put(1, ARRAY_1);
		
		assertEquals(1, map.size());
		assertEquals(true, map.containsKey(1));
		assertArrayEquals(ARRAY_1, map.get(1));
		
		map.put(1, ARRAY_2);
		
		assertEquals(1, map.size());
		assertEquals(true, map.containsKey(1));
		assertArrayEquals(ARRAY_2, map.get(1));
	}
	
	@Test
	public void testRemove() {
		IntLongArrayMap map = createIntLongArrayMap();
		
		map.put(0, ARRAY_0);
		map.put(1, ARRAY_1);
		map.put(2, ARRAY_2);
		map.put(3, ARRAY_3);

		map.remove(1);
		
		assertEquals(3, map.size());
		assertEquals(true, map.containsKey(0));
		assertEquals(false, map.containsKey(1));
		assertEquals(true, map.containsKey(2));
		assertEquals(true, map.containsKey(3));

		map.remove(0);
		
		assertEquals(2, map.size());
		assertEquals(false, map.containsKey(0));
		assertEquals(false, map.containsKey(1));
		assertEquals(true, map.containsKey(2));
		assertEquals(true, map.containsKey(3));

		map.remove(3);
		
		assertEquals(1, map.size());
		assertEquals(false, map.containsKey(0));
		assertEquals(false, map.containsKey(1));
		assertEquals(true, map.containsKey(2));
		assertEquals(false, map.containsKey(3));

		map.remove(2);
		
		assertEquals(0, map.size());
		assertEquals(false, map.containsKey(0));
		assertEquals(false, map.containsKey(1));
		assertEquals(false, map.containsKey(2));
		assertEquals(false, map.containsKey(3));

		map.remove(0);
		assertEquals(0, map.size());
		map.remove(1);
		assertEquals(0, map.size());
		map.remove(2);
		assertEquals(0, map.size());
		map.remove(3);
		assertEquals(0, map.size());
	}
	
	@Test
	public void testClear() {
		IntLongArrayMap map = createIntLongArrayMap();
		
		map.put(1, ARRAY_1);
		map.put(2, ARRAY_2);
		map.put(3, ARRAY_3);

		map.clear();

		assertEquals(0, map.size());
		assertEquals(false, map.containsKey(1));
		assertEquals(false, map.containsKey(2));
		assertEquals(false, map.containsKey(3));
	}
	
	@Test
	public void testKeySet() {
		IntLongArrayMap map = createIntLongArrayMap();
		
		map.put(1, ARRAY_1);
		map.put(202, ARRAY_2);
		map.put(3003, ARRAY_3);

		Collection<Integer> expectedKeys = new ArrayList<Integer>(Arrays.asList(1, 202, 3003));
		IntIterator keySet = map.keySet();
		
		assertEquals(true, keySet.hasNext());
		int key = keySet.next();
		assertEquals(true, expectedKeys.remove(key));
		
		assertEquals(true, keySet.hasNext());
		key = keySet.next();
		assertEquals(true, expectedKeys.remove(key));
		
		assertEquals(true, keySet.hasNext());
		key = keySet.next();
		assertEquals(true, expectedKeys.remove(key));
		
		assertEquals(true, expectedKeys.isEmpty());
		assertEquals(false, keySet.hasNext());
		
		try {
			keySet.next();
			fail("expected NoSuchElementException");
		} catch (NoSuchElementException exception) {
			// expected
		}
	}

	
	@Test
	public void testKeySet_remove() {
		IntLongArrayMap map = createIntLongArrayMap();
		
		map.put(1, ARRAY_1);
		map.put(202, ARRAY_2);
		map.put(3003, ARRAY_3);

		Collection<Integer> expectedKeys = new ArrayList<Integer>(Arrays.asList(1, 202, 3003));
		IntIterator keySet = map.keySet();
		
		assertEquals(true, keySet.hasNext());
		int key = keySet.next();
		assertEquals(true, expectedKeys.remove(key));
		keySet.remove();
		assertEquals(false, map.containsKey(key));
		assertEquals(2, map.size());
		
		assertEquals(true, keySet.hasNext());
		key = keySet.next();
		assertEquals(true, expectedKeys.remove(key));
		keySet.remove();
		assertEquals(false, map.containsKey(key));
		assertEquals(1, map.size());
		
		assertEquals(true, keySet.hasNext());
		key = keySet.next();
		assertEquals(true, expectedKeys.remove(key));
		keySet.remove();
		assertEquals(false, map.containsKey(key));
		assertEquals(0, map.size());
		
		assertEquals(false, keySet.hasNext());
		assertEquals(true, expectedKeys.isEmpty());
		
		try {
			keySet.next();
			fail("expected NoSuchElementException");
		} catch (NoSuchElementException exception) {
			// expected
		}
	}

	@Test
	public void testToString() {
		IntLongArrayMap map = createIntLongArrayMap();

		assertNotNull(map.toString());
	}
	
	@Test
	public void testHashCode() {
		IntLongArrayMap map0 = createIntLongArrayMap();

		IntLongArrayMap map1 = createIntLongArrayMap();
		map1.put(1, ARRAY_1);

		// not really guaranteed, but if this fails then the hashCode() is really badly implemented
		assertEquals(false, map0.hashCode() == map1.hashCode());
	}
	

	@Test
	public void testEquals() {
		{
			IntLongArrayMap map = createIntLongArrayMap();

			assertEquals(false, map.equals(null)); // null - not equals
			assertEquals(false, map.equals("and now something completely different")); // different type - not equals
			assertEquals(true, map.equals(createIntLongArrayMap())); // other empty instance - still equals
		}
		
		{
			IntLongArrayMap map1 = createIntLongArrayMap();
			map1.put(1, ARRAY_1);
			map1.put(202, ARRAY_2);
			map1.put(3003, ARRAY_3);

			IntLongArrayMap map2 = createIntLongArrayMap();
			map2.put(1, ARRAY_1);
			map2.put(202, ARRAY_2);
			map2.put(3003, ARRAY_3);

			IntLongArrayMap mapDiffKey = createIntLongArrayMap();
			mapDiffKey.put(1, ARRAY_1);
			mapDiffKey.put(202, ARRAY_2);
			mapDiffKey.put(-3333, ARRAY_3); // different from map1!

			IntLongArrayMap mapDiffValue = createIntLongArrayMap();
			mapDiffValue.put(1, ARRAY_1);
			mapDiffValue.put(202, ARRAY_2);
			mapDiffValue.put(3003, ARRAY_0); // different from map1!

			assertEquals(false, map1.equals(createIntLongArrayMap())); // different size - not equals
			assertEquals(false, map1.equals(mapDiffKey)); // different key - not equals
			assertEquals(false, map1.equals(mapDiffValue)); // different value - not equals
			assertEquals(true, map1.equals(map2)); // same content - equals
		}
	}

	@Test
	public void testLarge() {
		final int n = 1000;
		IntLongArrayMap map = createIntLongArrayMap();

		final long[][] values = {
				ARRAY_0, ARRAY_1, ARRAY_2, ARRAY_3
		};
		
		for (int i = 0; i < n; i++) {
			int key = i;
			long[] value = values[i % values.length];
			
			map.put(i, value);
			assertEquals(true, map.containsKey(key));
			assertArrayEquals(value, map.get(key));
			assertEquals(i + 1, map.size());
		}
		
		map.clear();
		assertEquals(0, map.size());
		for (int i = 0; i < n; i++) {
			int key = i;
			assertEquals(false, map.containsKey(key));
			assertEquals(null, map.get(key));
		}
	}
	
	
	@Test
	public void testRandom() {
		Random random = new Random(1);

		IntLongArrayMap map = createIntLongArrayMap();

		final long[][] values = {
				ARRAY_0, ARRAY_1, ARRAY_2, ARRAY_3
		};

		final int n = 10000;
		for (int i = 0; i < n; i++) {
			String desc = "step=" + i;

			int randomKey = random.nextInt(i / 10 + 1);
			long[] randomValue = values[random.nextInt(values.length)]; 
			
			int operation = random.nextInt(100);
			if (operation <= 60) {
				if (DEBUG) System.out.println(desc + " put " + randomKey + " " + randomValue);
				map.put(randomKey, randomValue);
				
			} else if (operation <= 100) {
				if (DEBUG) System.out.println(desc + " remove " + randomKey);
				map.remove(randomKey);
			}
		}
	}
}
