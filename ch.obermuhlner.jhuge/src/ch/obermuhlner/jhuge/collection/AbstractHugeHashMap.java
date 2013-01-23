package ch.obermuhlner.jhuge.collection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeMapBuilder;
import ch.obermuhlner.jhuge.collection.internal.IntObjectMap;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link Map} that stores key/value pairs in a {@link MemoryManager}.
 * 
 * <p>The implementation mimics a {@link HashMap}.</p>
 * 
 * <p>Access to single entries is O(1):
 * {@link #containsKey(Object)}, {@link #containsValue(Object)}
 * </p>
 * 
 * <p>In order to store the elements in the {@link MemoryManager} they must be serialized and deserialized to read them.
 * This is done by a {@link Converter} which can be specified in the {@link Builder}.</p>
 * 
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public class AbstractHugeHashMap<K, V> extends AbstractMap<K, V> {

	private static final byte[] EMPTY_DATA = new byte[0];

	private final MemoryManager memoryManager;
	private final Converter<K> keyConverter;
	private final Converter<V> valueConverter;

	private final IntObjectMap<long[]> hashCodeMap = new IntObjectMap<long[]>();

	private AbstractHugeHashMap(MemoryManager memoryManager, Converter<K> keyConverter, Converter<V> valueConverter) {
		this.memoryManager = memoryManager;
		this.keyConverter = keyConverter;
		this.valueConverter = valueConverter;
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
	public V put(K key, V value) {
		int hashCode = key == null ? 0 : key.hashCode();
		byte[] keyData = serializeKey(key);
		byte[] valueData = serializeValue(value);
		
		long[] keyValueAddresses = hashCodeMap.get(hashCode);
		if (keyValueAddresses == null) {
			long keyAddress = memoryManager.allocate(keyData);
			long valueAddress = memoryManager.allocate(valueData);
			
			keyValueAddresses = new long[2];
			hashCodeMap.put(hashCode, keyValueAddresses);

			keyValueAddresses[0] = keyAddress; 
			keyValueAddresses[1] = valueAddress;
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
					return oldValue;
				}
			}
			
			long keyAddress = memoryManager.allocate(keyData);
			long valueAddress = memoryManager.allocate(valueData);

			hashCodeMap.put(hashCode, append(keyValueAddresses, keyAddress, valueAddress));
			return null;
		}
	}
	
	private static long[] append(long[] keyValueAddresses, long keyAddress, long valueAddress) {
		long[] result = new long[keyValueAddresses.length + 2];
		System.arraycopy(keyValueAddresses, 0, result, 0, keyValueAddresses.length);
		result[result.length - 2] = keyAddress;
		result[result.length - 1] = valueAddress;
		return result;
	}
	
	@Override
	public V get(Object key) {
		int hashCode = key == null ? 0 : key.hashCode();
		
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
		int hashCode = key == null ? 0 : key.hashCode();
		
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
	public Set<Entry<K, V>> entrySet() {
		return new EntrySet();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{size=" + size() + "}";
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
		return hashCodeMap.isEmpty();
	}
	
	@Override
	public void clear() {
		hashCodeMap.clear();
		memoryManager.reset();
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
	
	private class EntrySet extends AbstractSet<Entry<K, V>> {

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new EntrySetIterator();
		}

		@Override
		public int size() {
			int result = 0;
			for (long[] addresses : hashCodeMap.values()) {
				result += addresses.length / 2;
			}
			return result;
		}
	}
	
	private class EntrySetIterator implements Iterator<Entry<K, V>> {
		private Iterator<Entry<Integer, long[]>> hashCodeMapIterator = hashCodeMap.entrySet().iterator();
		private Entry<Integer, long[]> currentEntry = null;
		private int currentIndex = 0;
		
		@Override
		public boolean hasNext() {
			return (currentEntry != null && currentIndex + 2 < currentEntry.getValue().length) || hashCodeMapIterator.hasNext();
		}
		
		@Override
		public Entry<K, V> next() {
			currentIndex += 2;
			if (currentEntry == null || currentIndex >= currentEntry.getValue().length) {
				currentIndex = 0;
				currentEntry = hashCodeMapIterator.hasNext() ? hashCodeMapIterator.next() : null;
			}
			if (currentEntry == null) {
				throw new NoSuchElementException();
			}
			
			long[] keyValueAddresses = currentEntry.getValue();
			
			long keyAddress = keyValueAddresses[currentIndex+0];
			long valueAddress = keyValueAddresses[currentIndex+1];
			
			HugeMapEntry entry = new HugeMapEntry(getKey(keyAddress), getValue(valueAddress));

			return entry;
		}
		@Override
		public void remove() {
			long[] keyValueAddresses = currentEntry.getValue();
			long keyAddress = keyValueAddresses[currentIndex+0];
			long valueAddress = keyValueAddresses[currentIndex+1];
			memoryManager.free(keyAddress);
			memoryManager.free(valueAddress);
			
			if (currentIndex == 0 && currentEntry.getValue().length == 2) {
				// remove entire entry
				hashCodeMapIterator.remove();
			} else {
				// remove 1 pair from entry
				long[] newKeyValueAddresses = new long[keyValueAddresses.length - 2];
				System.arraycopy(keyValueAddresses, 0, newKeyValueAddresses, 0, currentIndex);
				System.arraycopy(keyValueAddresses, currentIndex+2, newKeyValueAddresses, currentIndex, keyValueAddresses.length - currentIndex - 2);
				currentEntry.setValue(newKeyValueAddresses);
				currentIndex -= 2;
			}
		}
	}
	
	class HugeMapEntry implements Entry<K, V> {

		private final K key;
		private V value;

		public HugeMapEntry(K key, V value) {
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

		@Override
		public V setValue(V value) {
			V oldValue = this.value;
			this.value = value;
			
			put(key, value); // FIXME untested
			
			return oldValue;
		}
		
		@Override
		public String toString() {
			return key + "=" + value;
		}
	}
	
	/**
	 * Builds a {@link AbstractHugeHashMap}.
	 *
	 * @param <K> the type of keys
	 * @param <V> the type of values
	 */
	public static class Builder<K, V> extends AbstractHugeMapBuilder<K, V> {

		private AbstractHugeHashMap<K, V> result;
		
		private boolean built;
		
		private AbstractHugeHashMap<K, V> getMap() {
			if (result == null) {
				result = new AbstractHugeHashMap<K, V>(getMemoryManager(), getKeyConverter(), getValueConverter());
			}
			return result;
		}

		@Override
		public Builder<K, V> classLoader(ClassLoader classLoader) {
			super.classLoader(classLoader);
			return this;
		}
		
		@Override
		public Builder<K, V> key(Class<K> keyClass) {
			super.key(keyClass);
			return this;
		}
		
		@Override
		public Builder<K, V> key(Converter<K> keyConverter) {
			super.key(keyConverter);
			return this;
		}
		
		@Override
		public Builder<K, V> value(Class<V> valueClass) {
			super.value(valueClass);
			return this;
		}
		
		@Override
		public Builder<K, V> value(Converter<V> valueConverter) {
			super.value(valueConverter);
			return this;
		}
		
		@Override
		public Builder<K, V> bufferSize(int bufferSize) {
			super.bufferSize(bufferSize);
			return this;
		}
		
		@Override
		public Builder<K, V> blockSize(int blockSize) {
			super.blockSize(blockSize);
			return this;
		}
		
		@Override
		public Builder<K, V> memoryManager(MemoryManager memoryManager) {
			super.memoryManager(memoryManager);
			return this;
		}
		
		@Override
		public Builder<K, V> faster() {
			super.faster();
			return this;
		}
		
		@Override
		public Builder<K, V> capacity(int capacity) {
			super.capacity(capacity);
			return this;
		}
				
		@Override
		public Builder<K, V> put(K key, V value) {
			getMap().put(key, value);
			return this;
		}
		
		@Override
		public Builder<K, V> putAll(Map<K, V> map) {
			getMap().putAll(map);
			return this;
		}

		@Override
		public AbstractHugeHashMap<K, V> build() {
			if (built) {
				throw new IllegalStateException("Has already been built.");
			}
			built = true;
			return getMap();
		}
	}
}
