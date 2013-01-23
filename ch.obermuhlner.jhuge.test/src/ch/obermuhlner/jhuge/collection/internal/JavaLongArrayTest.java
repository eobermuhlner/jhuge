package ch.obermuhlner.jhuge.collection.internal;

/**
 * Tests {@link JavaLongArray}.
 */
public class JavaLongArrayTest extends AbstractLongArrayTest {

	@Override
	protected LongArray createLongArray(int capacity) {
		return new JavaLongArray(capacity);
	}

}
