package ch.obermuhlner.jhuge.collection.internal;

/**
 * Provides a map where the keys are <code>int</code> and the values are <code>long[]</code>.
 */
public interface IntLongArrayMap {

	/**
	 * Puts a key/value pair into the map.
	 * 
	 * @param key the key to put
	 * @param value the value to put
	 */
	void put(int key, long[] value);

	/**
	 * Returns whether the map contains the specified key.
	 * 
	 * @param key key to search for
	 * @return <code>true</code> if the map contains the key, <code>false</code> otherwise
	 */
	boolean containsKey(int key);

	/**
	 * Returns the value of the specified key.
	 * 
	 * @param key the key to search for
	 * @return the value of the specified key, or <code>null</code> if not found
	 */
	long[] get(int key);
	
	/**
	 * Removes the specified key/value pair.
	 * 
	 * @param key the key of the pair to remove
	 */
	void remove(int key);
	
	/**
	 * Removes all key/value pairs from the map.
	 */
	void clear();

	/**
	 * Returns the number of key/value pairs in the map.
	 *  
	 * @return the number of key/value pairs in the map
	 */
	int size();

	/**
	 * Returns an iterator over the keys in the map.
	 * 
	 * @return the {@link IntIterator} over the keys
	 */
	IntIterator keySet();
}
