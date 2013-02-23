package ch.obermuhlner.jhuge.collection.internal;

/**
 * Tests {@link PrimitiveIntLongArrayMap}.
 */
public class PrimitiveIntLongArrayMapTest extends AbstractIntLongArrayMapTest {

	@Override
	protected IntLongArrayMap createIntLongArrayMap() {
		return new PrimitiveIntLongArrayMap();
	}

}
