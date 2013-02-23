package ch.obermuhlner.jhuge.collection.internal;

/**
 * Tests {@link PrimitiveIntArray}.
 */
public class PrimitiveIntArrayTest extends AbstractIntArrayTest {

	@Override
	protected IntArray createIntArray(int capacity) {
		return new PrimitiveIntArray(capacity);
	}

}
