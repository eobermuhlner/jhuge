package ch.obermuhlner.jhuge.collection;

import static ch.obermuhlner.jhuge.collection.HashCodeCollisionsTest.COLLISION_0;
import static ch.obermuhlner.jhuge.collection.HashCodeCollisionsTest.COLLISION_1;
import static ch.obermuhlner.jhuge.collection.HashCodeCollisionsTest.COLLISION_2;
import static ch.obermuhlner.jhuge.collection.HashCodeCollisionsTest.COLLISION_3;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

/**
 * Abstract base class to test {@link Map}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractMapTest {

	private static final boolean DEBUG = false;

	protected abstract <K, V> Map<K, V> createMap(Pair<K, V>... initial);

	protected abstract boolean supportsMutable();

	protected abstract boolean supportsNullKeys();

	protected abstract boolean supportsNullValues();

	@Test
	public void testIsEmpty_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap();
			assertEquals(true, map.isEmpty());
		}
	}

	@Test
	public void testIsEmpty_not_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
			assertEquals(false, map.isEmpty());
		}
	}
	
	@Test
	public void testIsEmpty_mutable() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"));
			
			assertEquals(false, map.isEmpty());
			map.remove(-99);
			assertEquals(false, map.isEmpty());
			map.remove(1);
			assertEquals(true, map.isEmpty());
			map.remove(-99);
			assertEquals(true, map.isEmpty());
		}
	}

	@Test
	public void testSize_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap();
			assertEquals(0, map.size());
		}
	}
	
	@Test
	public void testSize_not_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
			assertEquals(3, map.size());
		}
	}		

	@Test
	public void testSize_collision() {
		{
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = createMap(pair(COLLISION_0, 0), pair(COLLISION_1, 1), pair(COLLISION_2, 2), pair(COLLISION_3, 3));

			assertEquals(4, map.size());
		}
	}

	@Test
	public void testSize_null_value() {
		if (supportsNullValues()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, (String) null));
			assertEquals(1, map.size());
		}
	}

	@Test
	public void testSize_null_key() {
		if (supportsNullKeys()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair((Integer) null, "a"));
			assertEquals(1, map.size());
		}
	}
	
	@Test
	public void testSize_mutable() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"));
			
			assertEquals(1, map.size());
			map.remove(-99);
			assertEquals(1, map.size());
			map.remove(1);
			assertEquals(0, map.size());
			map.remove(-99);
			assertEquals(0, map.size());
		}
	}

	@Test
	public void testGet_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap();

			assertEquals(null, map.get(-99));
		}
	}

	@Test
	public void testGet_not_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));

			assertEquals("a", map.get(1));
			assertEquals("b", map.get(2));
			assertEquals("c", map.get(3));
			assertEquals(null, map.get(-99));
		}
	}

	@Test
	public void testGet_collision() {
		{
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = createMap(pair(COLLISION_0, 0), pair(COLLISION_1, 1), pair(COLLISION_2, 2));

			assertEquals(Integer.valueOf(0), map.get(COLLISION_0));
			assertEquals(Integer.valueOf(1), map.get(COLLISION_1));
			assertEquals(Integer.valueOf(2), map.get(COLLISION_2));
			assertEquals(null, map.get(COLLISION_3));
			assertEquals(null, map.get(-99));
		}
	}

	@Test
	public void testGet_null_key() {
		if (supportsNullKeys()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair((Integer)null, "b"), pair(3, "c"));

			assertEquals("a", map.get(1));
			assertEquals("b", map.get(null));
			assertEquals("c", map.get(3));
			assertEquals(null, map.get(-99));
		}
	}

	@Test
	public void testGet_null_value() {
		if (supportsNullValues()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, (String)null), pair(3, "c"));

			assertEquals("a", map.get(1));
			assertEquals(null, map.get(2));
			assertEquals("c", map.get(3));
			assertEquals(null, map.get(-99));
		}
	}

	@Test
	public void testContainsKey_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap();

			assertEquals(false, map.containsKey(-99));
		}
	}

	@Test
	public void testContainsKey_not_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));

			assertEquals(true, map.containsKey(1));
			assertEquals(true, map.containsKey(2));
			assertEquals(true, map.containsKey(3));
			assertEquals(false, map.containsKey(-99));
		}
	}

	@Test
	public void testContainsKey_collision() {
		{
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = createMap(pair(COLLISION_0, 0), pair(COLLISION_1, 1), pair(COLLISION_2, 2));

			assertEquals(true, map.containsKey(COLLISION_0));
			assertEquals(true, map.containsKey(COLLISION_1));
			assertEquals(true, map.containsKey(COLLISION_2));
			assertEquals(false, map.containsKey(COLLISION_3));
			assertEquals(false, map.containsKey(-99));
		}
	}

	@Test
	public void testContainsKey_null_key() {
		if (supportsNullKeys()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair((Integer)null, "b"), pair(3, "c"));

			assertEquals(true, map.containsKey(1));
			assertEquals(true, map.containsKey(null));
			assertEquals(true, map.containsKey(3));
			assertEquals(false, map.containsKey(-99));
		}
	}

	@Test
	public void testContainsKey_null_value() {
		if (supportsNullValues()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, (String)null), pair(3, "c"));

			assertEquals(true, map.containsKey(1));
			assertEquals(true, map.containsKey(2));
			assertEquals(true, map.containsKey(3));
			assertEquals(false, map.containsKey(-99));
		}
	}

	@Test
	public void testContainsValue_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap();

			assertEquals(false, map.containsValue("x"));
		}
	}

	@Test
	public void testContainsValue_not_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));

			assertEquals(true, map.containsValue("a"));
			assertEquals(true, map.containsValue("b"));
			assertEquals(true, map.containsValue("c"));
			assertEquals(false, map.containsValue("x"));
		}
	}

	@Test
	public void testContainsValue_null_key() {
		if (supportsNullKeys()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair((Integer)null, "b"), pair(3, "c"));

			assertEquals(true, map.containsValue("a"));
			assertEquals(true, map.containsValue("b"));
			assertEquals(true, map.containsValue("c"));
			assertEquals(false, map.containsValue("x"));
		}
	}

	@Test
	public void testContainsValue_null_value() {
		if (supportsNullValues()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, (String)null), pair(3, "c"));

			assertEquals(true, map.containsValue("a"));
			assertEquals(true, map.containsValue(null));
			assertEquals(true, map.containsValue("c"));
			assertEquals(false, map.containsValue("x"));
		}
	}

	@Test
	public void testPut_not_mutable() {
		if (!supportsMutable()) {
			try {
				@SuppressWarnings("unchecked")
				Map<Integer, String> map = createMap();

				map.put(1, "a");
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}

	@Test
	public void testPut_mutable() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap();

			assertEquals(null, map.put(1, "a"));
			assertEquals(1, map.size());
			assertEquals("a", map.get(1));
			assertEquals(null, map.get(2));
			assertEquals(null, map.get(-99));

			assertEquals(null, map.put(2, "b"));
			assertEquals(2, map.size());
			assertEquals("a", map.get(1));
			assertEquals("b", map.get(2));
			assertEquals(null, map.get(-99));

			assertEquals("a", map.put(1, "a-1"));
			assertEquals(2, map.size());
			assertEquals("a-1", map.get(1));
			assertEquals("b", map.get(2));
			assertEquals(null, map.get(-99));
		}
	}

	@Test
	public void testPut_mutable_collision() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = createMap(pair(COLLISION_0, 0), pair(COLLISION_1, 1), pair(COLLISION_2, 2), pair(COLLISION_3, 3));

			assertEquals((Integer)0, map.put(COLLISION_0, 10));
			assertEquals(4, map.size());
			assertEquals(Integer.valueOf(10), map.get(COLLISION_0));
			assertEquals(Integer.valueOf(1), map.get(COLLISION_1));
			assertEquals(Integer.valueOf(2), map.get(COLLISION_2));
			assertEquals(Integer.valueOf(3), map.get(COLLISION_3));

			assertEquals((Integer)1, map.put(COLLISION_1, 11));
			assertEquals(4, map.size());
			assertEquals(Integer.valueOf(10), map.get(COLLISION_0));
			assertEquals(Integer.valueOf(11), map.get(COLLISION_1));
			assertEquals(Integer.valueOf(2), map.get(COLLISION_2));
			assertEquals(Integer.valueOf(3), map.get(COLLISION_3));
			
			assertEquals((Integer)2, map.put(COLLISION_2, 12));
			assertEquals(4, map.size());
			assertEquals(Integer.valueOf(10), map.get(COLLISION_0));
			assertEquals(Integer.valueOf(11), map.get(COLLISION_1));
			assertEquals(Integer.valueOf(12), map.get(COLLISION_2));
			assertEquals(Integer.valueOf(3), map.get(COLLISION_3));

			assertEquals((Integer)3, map.put(COLLISION_3, 13));
			assertEquals(4, map.size());
			assertEquals(Integer.valueOf(10), map.get(COLLISION_0));
			assertEquals(Integer.valueOf(11), map.get(COLLISION_1));
			assertEquals(Integer.valueOf(12), map.get(COLLISION_2));
			assertEquals(Integer.valueOf(13), map.get(COLLISION_3));
		}
	}

	@Test
	public void testPut_mutable_null_key() {
		if (supportsMutable() && supportsNullKeys()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap();
			
			assertEquals(null, map.put(null, "a"));
			assertEquals(1, map.size());
			assertEquals("a", map.get(null));
			assertEquals(null, map.get(-99));
			
			assertEquals("a", map.put(null, "a-1"));
			assertEquals(1, map.size());
			assertEquals("a-1", map.get(null));
			assertEquals(null, map.get(-99));
		}
	}

	@Test
	public void testPut_mutable_null_value() {
		if (supportsMutable() && supportsNullValues()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap();
			
			assertEquals(null, map.put(1, null));
			assertEquals(null, map.put(2, "b"));
			assertEquals(2, map.size());
			assertEquals(null, map.get(1));
			assertEquals("b", map.get(2));
			assertEquals(null, map.get(-99));
			
			assertEquals(null, map.put(1, "a"));
			assertEquals("b", map.put(2, null));
			assertEquals(2, map.size());
			assertEquals("a", map.get(1));
			assertEquals(null, map.get(2));
			assertEquals(null, map.get(-99));
		}
	}

	@Test
	public void testClear_not_mutable() {
		if (!supportsMutable()) {
			try {
				@SuppressWarnings("unchecked")
				Map<Integer, String> map = createMap();

				map.clear();
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}

	@Test
	public void testClear_mutable() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));

			map.clear();
			assertEquals(true, map.isEmpty());
			assertEquals(0, map.size());
			assertEquals(false, map.containsValue("a"));
			assertEquals(false, map.containsValue("b"));
			assertEquals(false, map.containsValue("c"));
			assertEquals(false, map.containsValue("x"));
			
			map.put(1, "a");
			assertEquals(false, map.isEmpty());
			assertEquals(1, map.size());
			assertEquals(true, map.containsValue("a"));
		}
	}

	@Test
	public void testKeySet_not_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
			Set<Integer> keySet = map.keySet();

			assertEquals(3, keySet.size());
			Collection<Integer> expectedKeys = new ArrayList<Integer>(Arrays.asList(1, 2, 3)); 
			for (Integer key : keySet) {
				assertEquals(true, expectedKeys.contains(key));
				expectedKeys.remove(key);
			}
			assertEquals(true, expectedKeys.isEmpty());
		}
	}

	@Test
	public void testKeySet_collision() {
		{
			{
				@SuppressWarnings("unchecked")
				Map<String, Integer> map = createMap(pair(COLLISION_0, 0), pair(COLLISION_1, 1));
				Set<String> keySet = map.keySet();

				assertEquals(2, keySet.size());
				Collection<String> expectedKeys = new ArrayList<String>(Arrays.asList(COLLISION_0, COLLISION_1)); 
				for (String key : keySet) {
					assertEquals(true, expectedKeys.contains(key));
					expectedKeys.remove(key);
				}
				assertEquals(true, expectedKeys.isEmpty());
			}

		}
	}

	@Test
	public void testKeySet_not_mutable() {
		if (!supportsMutable()) {
			try {
				@SuppressWarnings("unchecked")
				Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
				Set<Integer> keySet = map.keySet();

				keySet.remove(2);
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}

	@Test
	public void testKeySet_mutable_remove() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
			Set<Integer> keySet = map.keySet();

			assertEquals(false, keySet.remove(-99));
			assertEquals(3, keySet.size());

			assertEquals(true, keySet.remove(2));
			assertEquals(2, keySet.size());
			for (Integer key : keySet) {
				assertEquals(true, Arrays.asList(1, 3).contains(key));
			}
		}
	}

	@Test
	public void testValues_not_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
			Collection<String> values = map.values();

			assertEquals(3, values.size());
			Collection<String> expectedValues = new ArrayList<String>(Arrays.asList("a", "b", "c")); 
			for (String value : values) {
				assertEquals(true, expectedValues.contains(value));
				expectedValues.remove(value);
			}
			assertEquals(true, expectedValues.isEmpty());
		}
	}

	@Test
	public void testValues_not_mutable() {
		if (!supportsMutable()) {
			try {
				@SuppressWarnings("unchecked")
				Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
				Collection<String> values = map.values();

				values.remove("b");
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}

	@Test
	public void testValues_mutable_remove() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
			Collection<String> values = map.values();

			values.remove("b");
			assertEquals(2, values.size());
			for (String value : values) {
				assertEquals(true, Arrays.asList("a", "c").contains(value));
			}
		}
	}

	@Test
	public void testEntrySet_not_empty() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));

			Set<Entry<Integer, String>> entrySet = map.entrySet();
			assertEquals(3, entrySet.size());
			
			Collection<Integer> expectedKeys = new ArrayList<Integer>(Arrays.asList(1, 2, 3)); 
			Collection<String> expectedValues = new ArrayList<String>(Arrays.asList("a", "b", "c")); 
			for (Entry<Integer, String> entry : entrySet) {
				assertNotNull(entry.toString());
				Integer key = entry.getKey();
				assertEquals(true, expectedKeys.contains(key));
				expectedKeys.remove(key);

				String value = entry.getValue();
				assertEquals(true, expectedValues.contains(value));
				expectedValues.remove(value);
			}
			
			assertEquals(true, expectedKeys.isEmpty());
			assertEquals(true, expectedValues.isEmpty());
		}
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testEntrySet_empty_NoSuchElementException() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap();
			
			Iterator<Entry<Integer, String>> iterator = map.entrySet().iterator();
			
			assertEquals(false, iterator.hasNext());
			iterator.next();
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void testEntrySet_not_empty_NoSuchElementException() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"));
			
			Iterator<Entry<Integer, String>> iterator = map.entrySet().iterator();
			
			assertEquals(true, iterator.hasNext());
			Entry<Integer, String> entry = iterator.next();
			assertEquals(Integer.valueOf(1), entry.getKey());
			assertEquals("a", entry.getValue());
			
			assertEquals(false, iterator.hasNext());
			iterator.next();
		}
	}

	@Test
	public void testEntrySet_collision() {
		{
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = createMap(pair(COLLISION_0, 0), pair(COLLISION_1, 1), pair(COLLISION_2, 2), pair(COLLISION_3, 3));

			Set<Entry<String, Integer>> entrySet = map.entrySet();
			assertEquals(4, entrySet.size());
			
			Collection<String> expectedKeys = new ArrayList<String>(Arrays.asList(COLLISION_0, COLLISION_1, COLLISION_2, COLLISION_3)); 
			Collection<Integer> expectedValues = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3)); 
			for (Entry<String, Integer> entry : entrySet) {
				String key = entry.getKey();
				assertEquals(true, expectedKeys.contains(key));
				expectedKeys.remove(key);

				Integer value = entry.getValue();
				assertEquals(true, expectedValues.contains(value));
				expectedValues.remove(value);
			}
			
			assertEquals(true, expectedKeys.isEmpty());
			assertEquals(true, expectedValues.isEmpty());
		}
	}

	@Test
	public void testEntrySet_not_mutable() {
		if (!supportsMutable()) {
			try {
				@SuppressWarnings("unchecked")
				Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));

				Set<Entry<Integer, String>> entrySet = map.entrySet();
				entrySet.iterator().next().setValue("modified");
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}

	@Test
	public void testEntrySet_mutable_remove() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));

			Set<Entry<Integer, String>> entrySet = map.entrySet();
			Set<Integer> keySet = map.keySet();
			Collection<String> values = map.values();
			Iterator<Entry<Integer, String>> iterator = entrySet.iterator();
			
			int expectedSize = 3;
			while (iterator.hasNext()){
				Entry<Integer, String> entry = iterator.next();
				assertNotNull(entry);
				
				assertEquals(expectedSize, map.size());
				assertEquals(expectedSize, entrySet.size());
				assertEquals(expectedSize, keySet.size());
				assertEquals(expectedSize, values.size());
				
				iterator.remove();
				expectedSize--;
				
				assertEquals(expectedSize, map.size());
				assertEquals(expectedSize, entrySet.size());
				assertEquals(expectedSize, keySet.size());
				assertEquals(expectedSize, values.size());
			}
			
			assertEquals(0, map.size());
			assertEquals(0, entrySet.size());
			assertEquals(0, keySet.size());
			assertEquals(0, values.size());

			assertEquals(true, map.isEmpty());
			assertEquals(true, entrySet.isEmpty());
			assertEquals(true, keySet.isEmpty());
			assertEquals(true, values.isEmpty());
		}
	}

	@Test
	public void testEntrySet_mutable_collision_remove() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<String, Integer> map = createMap(pair(COLLISION_0, 0), pair(COLLISION_1, 1), pair(COLLISION_2, 2), pair(COLLISION_3, 3));
	
			Set<Entry<String, Integer>> entrySet = map.entrySet();
			Set<String> keySet = map.keySet();
			Collection<Integer> values = map.values();
			Iterator<Entry<String, Integer>> iterator = entrySet.iterator();
			
			int expectedSize = 4;
			while (iterator.hasNext()){
				Entry<String, Integer> entry = iterator.next();
				assertNotNull(entry);
				
				assertEquals(expectedSize, map.size());
				assertEquals(expectedSize, entrySet.size());
				assertEquals(expectedSize, keySet.size());
				assertEquals(expectedSize, values.size());
				
				iterator.remove();
				expectedSize--;
				
				assertEquals(expectedSize, map.size());
				assertEquals(expectedSize, entrySet.size());
				assertEquals(expectedSize, keySet.size());
				assertEquals(expectedSize, values.size());
			}
			
			assertEquals(0, map.size());
			assertEquals(0, entrySet.size());
			assertEquals(0, keySet.size());
			assertEquals(0, values.size());

			assertEquals(true, map.isEmpty());
			assertEquals(true, entrySet.isEmpty());
			assertEquals(true, keySet.isEmpty());
			assertEquals(true, values.isEmpty());
		}
	}

	@Test
	public void testEntrySet_mutable_setValue() {
		if (supportsMutable()) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));

			Set<Entry<Integer, String>> entrySet = map.entrySet();
			assertEquals(3, entrySet.size());
			for (Entry<Integer, String> entry : entrySet) {
				if (entry.getKey() == 2) {
					entry.setValue("b-modified");
				}
			}
			assertEquals(3, entrySet.size());
			assertEquals("b-modified", map.get(2));
		}
	}

	@Test
	public void testToString() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));

			assertNotNull(map.toString());
		}
	}

	@Test
	public void testHashCode() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map1 = createMap();
			@SuppressWarnings("unchecked")
			Map<Integer, String> map2 = createMap();
			
			assertEquals(map1.hashCode(), map1.hashCode());
			assertEquals(map1.hashCode(), map2.hashCode());
		}

		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map0 = createMap();
			@SuppressWarnings("unchecked")
			Map<Integer, String> map1 = createMap(pair(1, "a"));
			@SuppressWarnings("unchecked")
			Map<Integer, String> map2 = createMap(pair(1, "a"), pair(2, "b"));
			
			// not really guaranteed, but if this fails then the hashCode() is really badly implemented
			assertEquals(false, map0.hashCode() == map1.hashCode());
			assertEquals(false, map0.hashCode() == map2.hashCode());
			assertEquals(false, map1.hashCode() == map2.hashCode());
		}
	}

	@Test
	public void testEquals() {
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map1 = createMap();
			@SuppressWarnings("unchecked")
			Map<Integer, String> map2 = createMap();
			
			assertEquals(false, map1.equals(null)); // null - not equals
			assertEquals(false, map1.equals("and now something completely different")); // different type - not equals
			assertEquals(true, map1.equals(map2)); // other empty instance - still equals
		}

		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map0 = createMap();
			@SuppressWarnings("unchecked")
			Map<Integer, String> map1 = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
			@SuppressWarnings("unchecked")
			Map<Integer, String> map2 = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
			@SuppressWarnings("unchecked")
			Map<Integer, String> mapDiffKey = createMap(pair(1, "a"), pair(2, "b"), pair(99, "c"));
			@SuppressWarnings("unchecked")
			Map<Integer, String> mapDiffValue = createMap(pair(1, "a"), pair(2, "b"), pair(3, "xxx"));
			
			assertEquals(false, map1.equals(map0)); // different size - not equals
			assertEquals(false, map1.equals(mapDiffKey)); // different key - not equals
			assertEquals(false, map1.equals(mapDiffValue)); // different value - not equals
			assertEquals(true, map1.equals(map2)); // same content - equals
		}
		
		{
			@SuppressWarnings("unchecked")
			Map<Integer, String> map1 = createMap(pair(1, "a"), pair(2, "b"), pair(3, "c"));
			Map<Integer, String> map2 = new MyMap<Integer, String>(map1);

			assertEquals(true, map1.equals(map2)); // different class, same content - equals			
		}
	}
	
	private static class MyMap<K, V> extends HashMap<K, V> {
		private static final long serialVersionUID = 1L;

		public MyMap(Map<? extends K, ? extends V> other) {
			super(other);
		}
	}

	@Test
	public void testRandom() {
		if (supportsMutable()) {
			Random random = new Random(1);
			
			@SuppressWarnings("unchecked")
			Map<String, String> map = createMap();
			
			final int count = 10000;
			for (int i = 0; i < count; i++) {
				String desc = "step=" + i;

				String randomKey = String.valueOf(random.nextInt(100));
				if (supportsNullValues()) {
					if (random.nextInt(100) == 0) {
						randomKey = null;
					}
				}
				String randomValue = String.valueOf(random.nextInt(100));
				if (supportsNullValues()) {
					if (random.nextInt(100) == 0) {
						randomValue = null;
					}
				}

				int operation = random.nextInt(100);
				if (operation <= 1) {
					if (DEBUG) System.out.println(desc + " : clear()");
					map.clear();
					assertEquals(0, map.size());
					assertEquals(true, map.isEmpty());
					
				} else if (operation <= 3) {
					int step = random.nextInt(3) + 3;
					if (DEBUG) System.out.println(desc + " : remove every " + step + " elements from keySet()");
					removeEveryFewElements(map.keySet().iterator(), step);

				} else if (operation <= 6) {
					int step = random.nextInt(3) + 3;
					if (DEBUG) System.out.println(desc + " : remove every " + step + " elements from values()");
					removeEveryFewElements(map.values().iterator(), step);

				} else if (operation <= 9) {
					int step = random.nextInt(3) + 3;
					if (DEBUG) System.out.println(desc + " : remove every " + step + " elements from entrySet()");
					removeEveryFewElements(map.entrySet().iterator(), step);

				} else if (operation <= 15) {
					int step = random.nextInt(3) + 3;
					if (DEBUG) System.out.println(desc + " : set every " + step + " value to " + randomValue + " in entrySet() size " + map.size());
					int index = 0;
					Set<Entry<String, String>> entrySet = map.entrySet();
					for (Entry<String, String> entry : entrySet) {
						if (DEBUG) System.out.println(desc + " : entry " + entry.getKey() + " = " + entry.getValue() + " in entrySet()");
						if (index % step == 0) {
							if (DEBUG) System.out.println(desc + " : set #" + index + " value to " + randomValue + " in entrySet()");
							entry.setValue(randomValue);
							assertEquals(randomValue, entry.getValue());
							assertEquals(randomValue, map.get(entry.getKey()));
						}
						index++;
					}

				} else if (operation <= 95) {
					if (DEBUG) System.out.println(desc + " : put " + randomKey + "=" + randomValue);
					map.put(randomKey, randomValue);
					assertEquals(true, map.containsKey(randomKey));
					assertEquals(true, map.containsValue(randomValue));
					assertEquals(false, map.isEmpty());
					
				} else if (operation <= 100) {
					if (DEBUG) System.out.println(desc + " : remove " + randomKey);
					map.remove(randomKey);
				}
			}
		}
	}

	private static <E> void removeEveryFewElements(Iterator<E> iterator, int step) {
		int index = 0;
		while (iterator.hasNext()) {
			iterator.next();
			if (index % step == 0) {
				iterator.remove();
			}
			index++;
		}
	}
	
	public static <T1, T2> Pair<T1, T2> pair(T1 value1, T2 value2) {
		return new Pair<T1, T2>(value1, value2);
	}

	public static class Pair<T1, T2> {
		private final T1 value1;
		private final T2 value2;

		public Pair(T1 value1, T2 value2) {
			this.value1 = value1;
			this.value2 = value2;
		}

		public T1 getValue1() {
			return value1;
		}

		public T2 getValue2() {
			return value2;
		}

		@Override
		public String toString() {
			return "(" + value1 + "," + value2 + ")";
		}
	}
}
