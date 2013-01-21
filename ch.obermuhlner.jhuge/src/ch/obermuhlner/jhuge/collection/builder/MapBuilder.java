package ch.obermuhlner.jhuge.collection.builder;

import java.util.Map;

/**
 * Builds a {@link Map}.
 * 
 * <p>The concrete implementations of this API are typically inner classes of the collection class they are building.</p>
 *
 * @param <K> the type of the key in the {@link Map}
 * @param <V> the type of the value in the {@link Map}
 */
public interface MapBuilder<K, V> {

	/**
	 * Puts a single key/value pair.
	 * 
	 * @param key the key to add (may be <code>null</code> if the {@link Map} being built supports <code>null</code> keys)
	 * @param value the value to add (may be <code>null</code> if the {@link Map} being built supports <code>null</code> values)
	 * @return the builder, so that calls can be chained
	 */
	MapBuilder<K, V> put(K key, V value);

	/**
	 * Puts all key/value pairs of a {@link Map}.
	 * 
	 * @param map the {@link Map} to add
	 * @return the builder, so that calls can be chained
	 */
	MapBuilder<K, V> putAll(Map<K, V> map);

	/**
	 * Returns the built {@link Map}.
	 * 
	 * @return the {@link Map} containing all the key/value pairs added to it
	 */
	Map<K, V> build();
}
