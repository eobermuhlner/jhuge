package ch.obermuhlner.jhuge.collection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import ch.obermuhlner.jhuge.collection.internal.HugeIntLongArrayMap;
import ch.obermuhlner.jhuge.collection.internal.IntIterator;
import ch.obermuhlner.jhuge.collection.internal.IntLongArrayMap;
import ch.obermuhlner.jhuge.collection.internal.PrimitiveIntLongArrayMap;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * An abstract base class to simplify implementing a huge {@link Map} that mimics a {@link HashMap}.
 * 
 * <p>The mutating operations are implemented as protected methods with the suffix "Internal".</p>
 * <ul>
 * <li><code>putInternal(K, V)</code></li>
 * <li><code>clearInternal()</code></li>
 * </ul>
 * 
 * <p>Immutable concrete subclasses can call these internal methods to implement a builder.</p>
 * <p>Mutable concrete subclasses can implement the equivalent public methods by calling the internal methods.</p>
 * 
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public abstract class AbstractHugeHashMap<K, V> extends AbstractMap<K, V> {

	private static final byte[] EMPTY_DATA = new byte[0];

	private final MemoryManager memoryManager;
	private final Converter<K> keyConverter;
	private final Converter<V> valueConverter;

	private final IntLongArrayMap hashCodeMap;

	/**
	 * Constructs an {@link AbstractHugeHashMap}.
	 * 
	 * @param memoryManager the {@link MemoryManager}
	 * @param keyConverter the key {@link Converter}
	 * @param valueConverter the value {@link Converter}
	 * @param faster <code>true</code> to trade memory consumption for improved performance
	 * @param capacity the initial capacity
	 */
	protected AbstractHugeHashMap(MemoryManager memoryManager, Converter<K> keyConverter, Converter<V> valueConverter, boolean faster, int capacity) {
		this.memoryManager = memoryManager;
		this.keyConverter = keyConverter;
		this.valueConverter = valueConverter;
		
		hashCodeMap = faster ? new PrimitiveIntLongArrayMap(capacity) : new HugeIntLongArrayMap(memoryManager, capacity);
	}
	
	/**
	 * Returns the {@link MemoryManager}.
	 * 
	 * @return the {@link MemoryManager}
	 */
	MemoryManager getMemoryManager() {
		return memoryManager;
	}
	
	/**
	 * Returns the key {@link Converter}.
	 * 
	 * @return the key {@link Converter}
	 */
	Converter<K> getKeyConverter() {
		return keyConverter;
	}
	
	/**
	 * Returns the value {@link Converter}.
	 * 
	 * @return the value {@link Converter}
	 */
	Converter<V> getValueConverter() {
		return valueConverter;
	}
	
	@Override
	public V get(Object key) {
		int hashCode = hashCode(key);
		
		long[] keyValueAddresses = hashCodeMap.get(hashCode);
		if (keyValueAddresses == null) {
			return null;
		}
		
		for (int i = 0; i < keyValueAddresses.length; i+=2) {
			K matchingKey = getKey(keyValueAddresses[i + 0]);
			if (key == null ? matchingKey == null : key.equals(matchingKey)) {
				return getValue(keyValueAddresses[i + 1]);
			}
		}
		
		return null;
	}
	
	@Override
	public boolean containsKey(Object key) {
		int hashCode = hashCode(key);
		
		long[] keyValueAddresses = hashCodeMap.get(hashCode);
		if (keyValueAddresses == null) {
			return false;
		}

		for (int i = 0; i < keyValueAddresses.length; i+=2) {
			K matchingKey = getKey(keyValueAddresses[i + 0]);
			if (key == null ? matchingKey == null : key.equals(matchingKey)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{size=" + size() + ", table=" + hashCodeMap + "}";
	}
	
	private K getKey(long address) {
		byte[] data = memoryManager.read(address);
		K key = deserializeKey(data);
		return key;
	}
	
	private V getValue(long address) {
		byte[] data = memoryManager.read(address);
		V value = deserializeValue(data);
		return value;
	}
	
	@Override
	public boolean isEmpty() {
		return hashCodeMap.size() == 0;
	}
	
	/**
	 * Puts a key/value pair.
	 * 
	 * <p>This method has the same semantics as {@link #put(Object, Object)}.
	 * Mutable subclasses can implement {@link #put(Object, Object)} by calling this method.</p>
	 * <p>Immutable subclasses can call this method from the builder.</p>
	 * 
	 * @param key the key to add
	 * @param value the value to add
	 * @return the old value, or <code>null</code> if none
	 */
	protected V putInternal(K key, V value) {
		int hashCode = hashCode(key);
		byte[] keyData = serializeKey(key);
		byte[] valueData = serializeValue(value);
		
		long[] keyValueAddresses = hashCodeMap.get(hashCode);
		if (keyValueAddresses == null) {
			long keyAddress = memoryManager.allocate(keyData);
			long valueAddress = memoryManager.allocate(valueData);
			
			keyValueAddresses = new long[2];
			keyValueAddresses[0] = keyAddress; 
			keyValueAddresses[1] = valueAddress;
			hashCodeMap.put(hashCode, keyValueAddresses);
			return null;
		} else {
			for (int i = 0; i < keyValueAddresses.length; i+=2) {
				byte[] oldKeyData = memoryManager.read(keyValueAddresses[i+0]);
				K oldKey = deserializeKey(oldKeyData);
				if (key == null ? oldKey == null : key.equals(oldKey)) {
					byte[] oldValueData = memoryManager.read(keyValueAddresses[i+1]);
					V oldValue = deserializeValue(oldValueData);
					memoryManager.free(keyValueAddresses[i+1]);
					long valueAddress = memoryManager.allocate(valueData);
					keyValueAddresses[i+1] = valueAddress;
					hashCodeMap.put(hashCode, keyValueAddresses);
					return oldValue;
				}
			}
			
			long keyAddress = memoryManager.allocate(keyData);
			long valueAddress = memoryManager.allocate(valueData);

			hashCodeMap.put(hashCode, append(keyValueAddresses, keyAddress, valueAddress));
			return null;
		}
	}
	
	/**
	 * Removes all key/value pairs.
	 * 
	 * <p>This method has the same semantics as {@link #clear()}.
	 * Mutable subclasses can implement {@link #clear()} by calling this method.</p>
	 * <p>Immutable subclasses can call this method from the builder.</p>
	 */
	protected void clearInternal() {
		IntIterator hashCodeMapIterator = hashCodeMap.keySet();
		while (hashCodeMapIterator.hasNext()) {
			int key = hashCodeMapIterator.next();
			long[] addresses = hashCodeMap.get(key);
			for (int j = 0; j < addresses.length; j++) {
				memoryManager.free(addresses[j]);
			}
		}
		
		hashCodeMap.clear();
	}
	
	private static int hashCode(Object object) {
		int h = object == null ? 0 : object.hashCode();
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}

	private static long[] append(long[] keyValueAddresses, long keyAddress, long valueAddress) {
		long[] result = new long[keyValueAddresses.length + 2];
		System.arraycopy(keyValueAddresses, 0, result, 0, keyValueAddresses.length);
		result[result.length - 2] = keyAddress;
		result[result.length - 1] = valueAddress;
		return result;
	}
	
	private byte[] serializeKey(K key) {
		return key == null ? EMPTY_DATA : keyConverter.serialize(key);
	}

	private K deserializeKey(byte[] data) {
		return (data == null || data.length == 0) ? null : keyConverter.deserialize(data);
	}
	
	private byte[] serializeValue(V value) {
		return value == null ? EMPTY_DATA : valueConverter.serialize(value);
	}

	private V deserializeValue(byte[] data) {
		return (data == null || data.length == 0) ? null : valueConverter.deserialize(data);
	}
	
	/**
	 * Abstract base class to simplify implementing the {@link Map#entrySet() entrySet} of an {@link AbstractHugeHashMap}.
	 */
	protected abstract class AbstractEntrySet extends AbstractSet<Entry<K, V>> {
		@Override
		public int size() {
			int result = 0;
			IntIterator keySet = hashCodeMap.keySet();
			while(keySet.hasNext()) {
				int hashCode = keySet.next();
				long[] addresses = hashCodeMap.get(hashCode);
				result += addresses.length / 2;
			}
			return result;
		}
	}
	
	/**
	 * Abstract base class to simplify implementing the iterator of a {@link Map#entrySet() entrySet} of an {@link AbstractHugeHashMap}.
	 */
	protected abstract class AbstractEntrySetIterator implements Iterator<Entry<K, V>> {
		private IntIterator hashCodeMapIterator = hashCodeMap.keySet();
		private int currentHashCode;
		private long[] currentAddresses;
		private int currentIndex = 0;
		
		@Override
		public boolean hasNext() {
			return (currentAddresses != null && currentIndex + 2 < currentAddresses.length) || hashCodeMapIterator.hasNext();
		}
		
		@Override
		public Entry<K, V> next() {
			currentIndex += 2;
			if (currentAddresses == null || currentIndex >= currentAddresses.length) {
				currentIndex = 0;
				if (hashCodeMapIterator.hasNext()) {
					currentHashCode = hashCodeMapIterator.next();
					currentAddresses = hashCodeMap.get(currentHashCode);
				} else {
					currentHashCode = 0;
					currentAddresses = null;
				}
			}
			if (currentAddresses == null) {
				throw new NoSuchElementException();
			}
			
			long keyAddress = currentAddresses[currentIndex+0];
			long valueAddress = currentAddresses[currentIndex+1];
			
			K key = getKey(keyAddress);
			V value = getValue(valueAddress);
			AbstractEntry entry = createEntry(key, value);
			return entry;
		}
		
		/**
		 * Creates an {@link java.util.Map.Entry} with the specified key/value pair.
		 * 
		 * @param key the key
		 * @param value the value
		 * @return the entry
		 */
		protected abstract AbstractEntry createEntry(K key, V value);

		/**
		 * Removes the last retrieved element from the underlying map.
		 * 
		 * <p>This method has the same semantics as {@link #remove()}.
		 * Mutable subclasses can implement {@link #remove()} by calling this method.</p>
		 * <p>Immutable subclasses can call this method from the builder.</p>
		 */
		protected void removeInternal () {
			if (currentAddresses == null) {
				throw new NoSuchElementException();
			}

			long keyAddress = currentAddresses[currentIndex+0];
			long valueAddress = currentAddresses[currentIndex+1];
			memoryManager.free(keyAddress);
			memoryManager.free(valueAddress);
			
			if (currentIndex == 0 && currentAddresses.length == 2) {
				// remove entire entry
				hashCodeMapIterator.remove();
			} else {
				// remove 1 pair from entry
				long[] newKeyValueAddresses = new long[currentAddresses.length - 2];
				System.arraycopy(currentAddresses, 0, newKeyValueAddresses, 0, currentIndex);
				System.arraycopy(currentAddresses, currentIndex+2, newKeyValueAddresses, currentIndex, currentAddresses.length - currentIndex - 2);
				hashCodeMap.put(currentHashCode, newKeyValueAddresses);
				currentAddresses = newKeyValueAddresses;
				currentIndex -= 2;
			}
		}
	}
	
	/**
	 * Abstract base class to simplify implementing an {@link java.util.Map.Entry}.
	 */
	protected abstract class AbstractEntry implements Entry<K, V> {

		private final K key;
		private V value;

		/**
		 * Constructs an entry.
		 * 
		 * @param key the key
		 * @param value the value
		 */
		public AbstractEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		/**
		 * Sets the value of this entry in the underlying map.
		 * 
		 * <p>This method has the same semantics as {@link #setValue(Object)}.
		 * Mutable subclasses can implement {@link #setValue(Object)} by calling this method.</p>
		 * <p>Immutable subclasses can call this method from the builder.</p>
		 * 
		 * @param value the value to set
		 * @return the old value
		 */
		protected V setValueInternal(V value) {
			V oldValue = this.value;
			this.value = value;
			
			put(key, value);
			
			return oldValue;
		}
		
		@Override
		public String toString() {
			return key + "=" + value;
		}
	}
}
