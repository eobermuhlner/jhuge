package ch.obermuhlner.jhuge.collection.internal;

/**
 * Tests {@link JavaIntArray}.
 */
public class JavaIntArrayTest extends AbstractIntArrayTest {

	@Override
	protected IntArray createIntArray(int capacity) {
		return new JavaIntArray(capacity);
	}

}
