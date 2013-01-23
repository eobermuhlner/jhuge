package ch.obermuhlner.jhuge.collection;

import java.util.Map;

/**
 * Abstract base class to test a mutable {@link Map}.
 */
public abstract class AbstractMutableMapTest extends AbstractMapTest {

	@Override
	protected <K, V> Map<K, V> createMap(Pair<K, V>... initial) {
		Map<K, V> map = createEmptyMap();
		for (Pair<K, V> pair : initial) {
			map.put(pair.getValue1(), pair.getValue2());
		}
		return map;
	}

	/**
	 * Creates an empty {@link Map} to test.
	 * 
	 * @return the created {@link Map}
	 */
	protected abstract <K, V> Map<K, V> createEmptyMap();

	@Override
	protected boolean supportsMutable() {
		return true;
	}

}
