package ch.obermuhlner.jhuge.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests {@link HashMap}.
 */
public class HashMapTest extends AbstractMutableMapTest {

	@Override
	protected <K, V> Map<K, V> createEmptyMap() {
		return new HashMap<K, V>();
	}

	@Override
	protected boolean supportsNullKeys() {
		return true;
	}

	@Override
	protected boolean supportsNullValues() {
		return true;
	}
}
