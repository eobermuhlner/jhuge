package ch.obermuhlner.jhuge.collection.internal;

/**
 * Tests {@link JavaIntLongArrayMap}.
 */
public class JavaIntLongArrayMapTest extends AbstractIntLongArrayMapTest {

	@Override
	protected IntLongArrayMap createIntLongArrayMap() {
		return new JavaIntLongArrayMap();
	}

}
