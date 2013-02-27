package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

/**
 * Abstract base class to test {@link List}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractListTest extends AbstractCollectionTest {

	@Override
	protected final <T> Collection<T> createCollection(T... initial) {
		return createList(initial);
	}
	
	protected abstract <T> List<T> createList(T... initial);

	@Test
	public void testOrderedElements() {
		{
			List<String> list = createList("a", "b", "c", "d");
			assertEquals("a", list.get(0));
			assertEquals("b", list.get(1));
			assertEquals("c", list.get(2));
			assertEquals("d", list.get(3));
		}

		{
			List<String> list = createList("d", "c", "b", "a");
			assertEquals("d", list.get(0));
			assertEquals("c", list.get(1));
			assertEquals("b", list.get(2));
			assertEquals("a", list.get(3));
		}
	}
	
	@Test
	public void testSet_not_mutable() {
		if (!supportsMutable()) {
			try {
				List<String> list = createList("a", "b", "c");
				list.set(0, "x");
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testSet_mutable() {
		if (supportsMutable()) {
			List<String> list = createList("a", "b", "c");
			list.set(0, "x");
			list.set(1, "y");
			list.set(2, "z");
			assertEquals("x", list.get(0));
			assertEquals("y", list.get(1));
			assertEquals("z", list.get(2));
		}
	}
	
	@Test
	public void testSet_mutable_left_IndexOutOfBoundsException() {
		if (supportsMutable()) {
			try {
				List<String> list = createList("a", "b", "c");
				list.set(-1, "x");
				fail("expected IndexOutOfBoundsException");
			} catch (IndexOutOfBoundsException exception) {
				// expected
			}
		}
	}
	
	@Test
	public void testSet_mutable_right_IndexOutOfBoundsException() {
		if (supportsMutable()) {
			try {
				List<String> list = createList("a", "b", "c");
				list.set(4, "x");
				fail("expected IndexOutOfBoundsException");
			} catch (IndexOutOfBoundsException exception) {
				// expected
			}
		}
	}
	
	/**
	 * Tests whether the {@link List} under test is equal to another {@link List} class with the same content.
	 */
	@Test
	public void testEquals_List() {
		{
			List<String> list1 = createList("a", "b", "c");
			List<String> list2 = Arrays.asList("a", "b", "c");

			assertEquals(true, list1.equals(list2)); // different class, same content - equals
		}
	}
}
