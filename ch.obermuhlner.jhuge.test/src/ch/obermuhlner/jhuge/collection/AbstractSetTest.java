package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Abstract base class to test {@link Set}.
 */
public abstract class AbstractSetTest extends AbstractCollectionTest {

	@Override
	protected final <T> Collection<T> createCollection(T... initial) {
		return createSet(initial);
	}
	
	/**
	 * Creates a {@link Set} filled with the specified initial elements to test.
	 * 
	 * @param initial the initial elements to be added to the created {@link Set}
	 * @return the created {@link Set}
	 */
	protected abstract <T> Set<T> createSet(T... initial);

	/**
	 * Tests whether duplicate elements are handled correctly in a {@link Set}.
	 */
	@Test
	public void testDuplicates() {
		{
			Set<String> set = createSet("a", "b", "c", "a", "a", "b");

			assertEquals(3, set.size());
			assertEquals(true, set.contains("a"));
			assertEquals(true, set.contains("b"));
			assertEquals(true, set.contains("c"));
			assertEquals(false, set.contains("x"));
		}
	}
	/**
	 * Tests whether duplicate <code>null</code> elements are handled correctly in a {@link Set}.
	 */
	@Test
	public void testDuplicates_null() {
		if (supportsNullValues()) {
			Set<String> set = createSet(null, null, null);

			assertEquals(1, set.size());
			assertEquals(true, set.contains(null));
			assertEquals(false, set.contains("x"));
		}
	}
	
	/**
	 * Tests whether the {@link Set} under test is equal to another {@link Set} class with the same content.
	 */
	@Test
	public void testEquals_Set() {
		{
			Set<String> set1 = createSet("a", "b", "c");
			Set<String> set2 = new MySet<String>(set1);

			assertEquals(true, set1.equals(set2)); // different class, same content - equals
		}
	}
	
	private static class MySet<E> extends HashSet<E> {

		private static final long serialVersionUID = -3103809171238407075L;

		public MySet(Collection<? extends E> collection) {
			super(collection);
		}
	}
}
