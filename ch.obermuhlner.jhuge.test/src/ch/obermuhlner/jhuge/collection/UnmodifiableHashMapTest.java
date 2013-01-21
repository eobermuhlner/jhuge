package ch.obermuhlner.jhuge.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests {@link HashMap} made unmodifiable using {@link Collections#unmodifiableMap(Map)}.
 */
public class UnmodifiableHashMapTest extends AbstractMapTest {

	@Override
	protected <K, V> Map<K, V> createMap(Pair<K, V>... initial) {
		Map<K, V> map = new HashMap<K, V>();
		for (Pair<K, V> pair : initial) {
			map.put(pair.getValue1(), pair.getValue2());
		}

		return Collections.unmodifiableMap(map);
	}

	@Override
	protected boolean supportsMutable() {
		return false;
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
