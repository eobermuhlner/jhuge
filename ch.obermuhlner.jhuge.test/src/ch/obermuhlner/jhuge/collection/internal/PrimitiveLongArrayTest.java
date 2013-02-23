package ch.obermuhlner.jhuge.collection.internal;

/**
 * Tests {@link PrimitiveLongArray}.
 */
public class PrimitiveLongArrayTest extends AbstractLongArrayTest {

	@Override
	protected LongArray createLongArray(int capacity) {
		return new PrimitiveLongArray(capacity);
	}

}
