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
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

/**
 * Abstract base class to test {@link Collection}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractCollectionTest {

	private static final boolean DEBUG = false;
	
	protected abstract <T> Collection<T> createCollection(T... initial);

	protected abstract boolean supportsMutable();

	protected abstract boolean supportsNullValues();

	@Test
	public void testSize_empty() {
		{
			Collection<String> collection = createCollection();
			assertEquals(0, collection.size());
		}
	}		

	@Test
	public void testSize_not_empty() {
		{
			Collection<String> collection = createCollection("a", "b", "c");
			assertEquals(3, collection.size());
		}
	}		

	@Test
	public void testSize_collision() {
		{
			Collection<String> collection = createCollection(COLLISION_0, COLLISION_1, COLLISION_2);
			assertEquals(3, collection.size());
		}
	}		

	@Test
	public void testSize_null_value() {
		if (supportsNullValues()) {
			Collection<String> collection = createCollection("a", null, "c");
			assertEquals(3, collection.size());
		}
	}

	@Test
	public void testContains_empty() {
		{
			Collection<String> collection = createCollection();
			assertEquals(false, collection.contains("x"));
		}
	}

	@Test
	public void testContains_not_empty() {
		{
			Collection<String> collection = createCollection("a", "b", "c");
			assertEquals(true, collection.contains("a"));
			assertEquals(true, collection.contains("b"));
			assertEquals(true, collection.contains("c"));
			assertEquals(false, collection.contains("x"));
		}
	}

	@Test
	public void testContains_collision() {
		{
			Collection<String> collection = createCollection(COLLISION_0, COLLISION_1, COLLISION_2);
			assertEquals(true, collection.contains(COLLISION_0));
			assertEquals(true, collection.contains(COLLISION_1));
			assertEquals(true, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains(COLLISION_3));
			assertEquals(false, collection.contains("x"));
		}
	}

	@Test
	public void testContains_null_value() {
		if (supportsNullValues()) {
			Collection<String> collection = createCollection("a", null, "c");
			assertEquals(true, collection.contains("a"));
			assertEquals(true, collection.contains(null));
			assertEquals(true, collection.contains("c"));
			assertEquals(false, collection.contains("x"));
		}
	}
	
	@Test
	public void testAdd_not_mutable() {
		if (!supportsMutable()) {
			try {
				Collection<String> collection = createCollection();
				collection.add("a");
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testAdd_mutable() {
		if (supportsMutable()) {
			Collection<String> collection = createCollection();

			collection.add("a");
			assertEquals(1, collection.size());
			assertEquals(true, collection.contains("a"));
			assertEquals(false, collection.contains("b"));
			assertEquals(false, collection.contains("c"));
			assertEquals(false, collection.contains("x"));

			collection.add("b");
			assertEquals(2, collection.size());
			assertEquals(true, collection.contains("a"));
			assertEquals(true, collection.contains("b"));
			assertEquals(false, collection.contains("c"));
			assertEquals(false, collection.contains("x"));

			collection.add("c");
			assertEquals(3, collection.size());
			assertEquals(true, collection.contains("a"));
			assertEquals(true, collection.contains("b"));
			assertEquals(true, collection.contains("c"));
			assertEquals(false, collection.contains("x"));
		}
	}

	@Test
	public void testAdd_mutable_null_value() {
		if (supportsMutable() && supportsNullValues()) {
			Collection<String> collection = createCollection();

			collection.add(null);
			assertEquals(1, collection.size());
			assertEquals(true, collection.contains(null));
		}
	}

	@Test
	public void testAdd_mutable_collision() {
		if (supportsMutable()) {
			Collection<String> collection = createCollection();

			collection.add(COLLISION_0);
			assertEquals(1, collection.size());
			assertEquals(true, collection.contains(COLLISION_0));
			assertEquals(false, collection.contains(COLLISION_1));
			assertEquals(false, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains(COLLISION_3));
			assertEquals(false, collection.contains("x"));

			collection.add(COLLISION_1);
			assertEquals(2, collection.size());
			assertEquals(true, collection.contains(COLLISION_0));
			assertEquals(true, collection.contains(COLLISION_1));
			assertEquals(false, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains(COLLISION_3));
			assertEquals(false, collection.contains("x"));

			collection.add(COLLISION_2);
			assertEquals(3, collection.size());
			assertEquals(true, collection.contains(COLLISION_0));
			assertEquals(true, collection.contains(COLLISION_1));
			assertEquals(true, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains(COLLISION_3));
			assertEquals(false, collection.contains("x"));
		}
	}

	@Test
	public void testRemove_not_mutable() {
		if (!supportsMutable()) {
			try {
				Collection<String> collection = createCollection("a");
				collection.remove("a");
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testRemove_not_mutable_null_value() {
		if (!supportsMutable() && supportsNullValues()) {
			try {
				Collection<String> collection = createCollection((String) null);
				collection.remove(null);
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testRemove_mutable() {
		if (supportsMutable()) {
			Collection<String> collection = createCollection("a", "b", "c");

			// remove "x"
			assertEquals(false, collection.remove("x"));
			assertEquals(3, collection.size());

			// remove "a"
			assertEquals(true, collection.remove("a"));
			assertEquals(2, collection.size());
			assertEquals(false, collection.contains("a"));
			assertEquals(true, collection.contains("b"));
			assertEquals(true, collection.contains("c"));
			assertEquals(false, collection.contains("x"));

			assertEquals(false, collection.remove("a"));
			assertEquals(2, collection.size());
			assertEquals(false, collection.contains("a"));
			assertEquals(true, collection.contains("b"));
			assertEquals(true, collection.contains("c"));
			assertEquals(false, collection.contains("x"));

			// remove "b"
			assertEquals(true, collection.remove("b"));
			assertEquals(1, collection.size());
			assertEquals(false, collection.contains("a"));
			assertEquals(false, collection.contains("b"));
			assertEquals(true, collection.contains("c"));
			assertEquals(false, collection.contains("x"));

			assertEquals(false, collection.remove("b"));
			assertEquals(1, collection.size());
			assertEquals(false, collection.contains("a"));
			assertEquals(false, collection.contains("b"));
			assertEquals(true, collection.contains("c"));
			assertEquals(false, collection.contains("x"));

			// remove "c"
			assertEquals(true, collection.remove("c"));
			assertEquals(0, collection.size());
			assertEquals(false, collection.contains("a"));
			assertEquals(false, collection.contains("b"));
			assertEquals(false, collection.contains("c"));
			assertEquals(false, collection.contains("x"));

			assertEquals(false, collection.remove("c"));
			assertEquals(0, collection.size());
			assertEquals(false, collection.contains("a"));
			assertEquals(false, collection.contains("b"));
			assertEquals(false, collection.contains("c"));
			assertEquals(false, collection.contains("x"));

			// remove "x"
			assertEquals(false, collection.remove("x"));
			assertEquals(0, collection.size());
		}
	}
	
	
	@Test
	public void testRemove_mutable_collision() {
		if (supportsMutable()) {
			Collection<String> collection = createCollection(COLLISION_0, COLLISION_1, COLLISION_2);

			// remove "x"
			assertEquals(false, collection.remove("x"));
			assertEquals(3, collection.size());

			// remove COLLISION_0
			assertEquals(true, collection.remove(COLLISION_0));
			assertEquals(2, collection.size());
			assertEquals(false, collection.contains(COLLISION_0));
			assertEquals(true, collection.contains(COLLISION_1));
			assertEquals(true, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains("x"));

			assertEquals(false, collection.remove(COLLISION_0));
			assertEquals(2, collection.size());
			assertEquals(false, collection.contains(COLLISION_0));
			assertEquals(true, collection.contains(COLLISION_1));
			assertEquals(true, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains("x"));

			// remove COLLISION_1
			assertEquals(true, collection.remove(COLLISION_1));
			assertEquals(1, collection.size());
			assertEquals(false, collection.contains(COLLISION_0));
			assertEquals(false, collection.contains(COLLISION_1));
			assertEquals(true, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains("x"));

			assertEquals(false, collection.remove(COLLISION_1));
			assertEquals(1, collection.size());
			assertEquals(false, collection.contains(COLLISION_0));
			assertEquals(false, collection.contains(COLLISION_1));
			assertEquals(true, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains("x"));

			// remove COLLISION_2
			assertEquals(true, collection.remove(COLLISION_2));
			assertEquals(0, collection.size());
			assertEquals(false, collection.contains(COLLISION_0));
			assertEquals(false, collection.contains(COLLISION_1));
			assertEquals(false, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains("x"));

			assertEquals(false, collection.remove(COLLISION_2));
			assertEquals(0, collection.size());
			assertEquals(false, collection.contains(COLLISION_0));
			assertEquals(false, collection.contains(COLLISION_1));
			assertEquals(false, collection.contains(COLLISION_2));
			assertEquals(false, collection.contains("x"));

			// remove COLLISION_3 (not found)
			assertEquals(false, collection.remove(COLLISION_3));
			assertEquals(0, collection.size());

			// remove "x" (not found)
			assertEquals(false, collection.remove("x"));
			assertEquals(0, collection.size());
		}
	}
	
	@Test
	public void testRemove_mutable_null_value() {
		if (supportsNullValues() && supportsMutable()) {
			Collection<String> collection = createCollection("a", null, "c");
			
			assertEquals(true, collection.remove(null));
			assertEquals(2, collection.size());
			
			assertEquals(false, collection.remove(null));
			assertEquals(2, collection.size());
		}
	}

	@Test
	public void testClear_not_mutable() {
		if (!supportsMutable()) {
			try {
				Collection<String> collection = createCollection("a");
				collection.clear();
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testClear_mutable() {
		if (supportsMutable()) {
			Collection<String> collection = createCollection("a", "b", "c");
			
			collection.clear();
			assertEquals(0, collection.size());
			assertEquals(false, collection.contains("a"));
			assertEquals(false, collection.contains("b"));
			assertEquals(false, collection.contains("c"));
			assertEquals(false, collection.contains("x"));
		}
	}
	
	@Test
	public void testIterator_not_empty() {
		{
			Collection<String> collection = createCollection("a", "b", "c");
			Iterator<String> iterator = collection.iterator();
			
			Collection<String> expectedElements = new ArrayList<String>(Arrays.asList("a", "b", "c"));
			String element;
			
			assertNotNull(iterator.toString());
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			
			assertNotNull(iterator.toString());
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			
			assertNotNull(iterator.toString());
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			
			assertNotNull(iterator.toString());
			assertEquals(false, iterator.hasNext());
			assertEquals(true, expectedElements.isEmpty());
			
			try {
				iterator.next();
				fail("expected NoSuchElementException");
			} catch (NoSuchElementException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testIterator_collision_not_empty() {
		{
			Collection<String> collection = createCollection(COLLISION_0, COLLISION_1, COLLISION_2);
			Iterator<String> iterator = collection.iterator();
			
			Collection<String> expectedElements = new ArrayList<String>(Arrays.asList(COLLISION_0, COLLISION_1, COLLISION_2));
			String element;
			
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			
			assertEquals(false, iterator.hasNext());
			assertEquals(true, expectedElements.isEmpty());
			
			try {
				iterator.next();
				fail("expected NoSuchElementException");
			} catch (NoSuchElementException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testIterator_not_mutable() {
		if (!supportsMutable()) {
			Collection<String> collection = createCollection("a", "b", "c");
			Iterator<String> iterator = collection.iterator();
			
			iterator.hasNext();
			iterator.next();
			try {
				iterator.remove();
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testIterator_mutable_remove() {
		if (supportsMutable()) {
			Collection<String> collection = createCollection("a", "b", "c");
			Iterator<String> iterator = collection.iterator();
			
			Collection<String> expectedElements = new ArrayList<String>(Arrays.asList("a", "b", "c"));
			String element;
			
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			iterator.remove();
			assertEquals(2, collection.size());
			
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			iterator.remove();
			assertEquals(1, collection.size());
			
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			iterator.remove();
			assertEquals(0, collection.size());

			assertEquals(false, iterator.hasNext());
			assertEquals(true, expectedElements.isEmpty());
			
			try {
				iterator.next();
				fail("expected NoSuchElementException");
			} catch (NoSuchElementException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testIterator_mutable_collision_remove() {
		if (supportsMutable()) {
			Collection<String> collection = createCollection(COLLISION_0, COLLISION_1, COLLISION_2);
			Iterator<String> iterator = collection.iterator();
			
			Collection<String> expectedElements = new ArrayList<String>(Arrays.asList(COLLISION_0, COLLISION_1, COLLISION_2));
			String element;
			
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			iterator.remove();
			assertEquals(2, collection.size());
			
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			iterator.remove();
			assertEquals(1, collection.size());
			
			assertEquals(true, iterator.hasNext());
			element = iterator.next();
			assertEquals(true, expectedElements.remove(element));
			iterator.remove();
			assertEquals(0, collection.size());

			assertEquals(false, iterator.hasNext());
			assertEquals(true, expectedElements.isEmpty());
			
			try {
				iterator.next();
				fail("expected NoSuchElementException");
			} catch (NoSuchElementException exception) {
				// expected
			}
		}
	}

	@Test
	public void testHashCode() {
		{
			Collection<String> collection = createCollection();
			
			assertEquals(collection.hashCode(), collection.hashCode());
			assertEquals(collection.hashCode(), createCollection().hashCode());
		}

		{
			Collection<String> collection0 = createCollection();
			Collection<String> collection1 = createCollection("a");
			Collection<String> collection2 = createCollection("a", "b");
			
			// not really guaranteed, but if this fails then the hashCode() is really badly implemented
			assertEquals(false, collection0.hashCode() == collection1.hashCode());
			assertEquals(false, collection0.hashCode() == collection2.hashCode());
			assertEquals(false, collection1.hashCode() == collection2.hashCode());
		}
	}

	@Test
	public void testEquals() {
		{
			Collection<String> collection = createCollection();
			
			assertEquals(false, collection.equals(null)); // null - not equals
			assertEquals(false, collection.equals("and now something completely different")); // different type - not equals
			assertEquals(true, collection.equals(createCollection())); // other empty instance - still equals
		}

		{
			Collection<String> collection1 = createCollection("a", "b", "c");
			Collection<String> collection2 = createCollection("a", "b", "c");
			Collection<String> collectionDiff = createCollection("a", "b", "xxx");

			assertEquals(false, collection1.equals(createCollection())); // different size - not equals
			assertEquals(false, collection1.equals(collectionDiff)); // different content - not equals
			assertEquals(true, collection1.equals(collection2)); // same content - equals
		}
	}
	
	@Test
	public void testToString() {
		Collection<String> collection = createCollection("a", "b", "c");

		assertNotNull(collection.toString());
	}

	@Test
	public void testLarge() {
		{
			Set<String> testdata = createTestData(1000);

			Collection<String> collection = createCollection(testdata.toArray(new String[0]));

			assertEquals(testdata.size(), collection.size());

			for (String string : testdata) {
				assertEquals(string, true, collection.contains(string));
				assertEquals("unknown:" + string, false, collection.contains("unknown:" + string));
			}
		}
	}

	@Test
	public void testRandom() {
		if (supportsMutable()) {
			Random random = new Random(1);
			
			Collection<String> collection = createCollection();
			
			final int count = 10000;
			for (int i = 0; i < count; i++) {
				String desc = "step=" + i;
				
				String r = String.valueOf(random.nextInt(100));
				if (supportsNullValues()) {
					if (random.nextInt(100) == 0) {
						r = null;
					}
				}

				int operation = random.nextInt(100);
				if (operation <= 1) {
					if (DEBUG) System.out.println(desc + " : clear()");
					collection.clear();
					assertEquals(desc, 0, collection.size());
					assertEquals(desc, true, collection.isEmpty());

				} else if (operation <= 5) {
					int step = random.nextInt(3) + 3;
					if (DEBUG) System.out.println(desc + " : remove every " + step);
					removeEveryFewElements(collection.iterator(), step);


				} else if (operation <= 80) {
					if (DEBUG) System.out.println(desc + " : add(" + r + ")");
					collection.add(r);
					assertEquals(desc, true, collection.contains(r));
					assertEquals(desc, false, collection.isEmpty());
					
				} else if (operation <= 100) {
					if (DEBUG) System.out.println(desc + " : remove(" + r + ")");
					boolean found = collection.contains(r);
					int oldSize = collection.size();
					assertEquals(desc, found, collection.remove(r));
					int newSize = found ? oldSize - 1 : oldSize;
					assertEquals(desc, newSize, collection.size());
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

	@Test
	public void testContainsWithCollisions() {
		final HashCodeCollision<String> a = new HashCodeCollision<String>("a");
		final HashCodeCollision<String> b = new HashCodeCollision<String>("b");
		final HashCodeCollision<String> c = new HashCodeCollision<String>("c");
		final HashCodeCollision<String> d = new HashCodeCollision<String>("d");
		final HashCodeCollision<String> e = new HashCodeCollision<String>("e");
		final HashCodeCollision<String> x = new HashCodeCollision<String>("x");
		
		@SuppressWarnings("unchecked")
		Collection<HashCodeCollision<String>> collection = createCollection(a, b, c, d, e);
		assertEquals(true, collection.contains(a));
		assertEquals(true, collection.contains(b));
		assertEquals(true, collection.contains(c));
		assertEquals(true, collection.contains(d));
		assertEquals(true, collection.contains(e));
		assertEquals(false, collection.contains(x));
	}
	
	private static Set<String> createTestData(int count) {
		HashSet<String> set = new HashSet<String>();
		
		for (int i = 0; i < count; i++) {
			set.add("string" + i);
		}
		
		return set;
	}
}
