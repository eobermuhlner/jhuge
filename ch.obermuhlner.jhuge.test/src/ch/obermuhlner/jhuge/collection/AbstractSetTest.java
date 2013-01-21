package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
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
}
