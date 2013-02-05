package ch.obermuhlner.jhuge.collection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeMapBuilder;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link Map} that stores key/value pairs in a {@link MemoryManager}.
 * 
 * <p>The implementation mimics a {@link HashMap}.</p>
 * 
 * <p>Access to single entries through the key is O(1):
 * {@link #containsKey(Object)}}, {@link #get(Object)}}
 * </p>
 * 
 * <p>In order to store the elements in the {@link MemoryManager} they must be serialized and deserialized to read them.
 * This is done by a {@link Converter} which can be specified in the {@link Builder}.
 * The default {@link Converter} can handle instances of all serializable classes.</p>
 * 
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public class HugeHashMap<K, V> extends AbstractHugeHashMap<K, V> {

	private HugeHashMap(MemoryManager memoryManager, Converter<K> keyConverter, Converter<V> valueConverter, boolean faster, int capacity) {
		super(memoryManager, keyConverter, valueConverter, faster, capacity);
	}

	@Override
	public V put(K key, V value) {
		return putInternal(key, value);
	}

	@Override
	public void clear() {
		clearInternal();
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	private class EntrySet extends AbstractEntrySet {
		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new EntrySetIterator();
		}
	}
	
	private class EntrySetIterator extends AbstractEntrySetIterator {
		@Override
		public void remove() {
			removeInternal();
		}
		
		@Override
		protected AbstractEntry createEntry(K key, V value) {
			return new HugeMapEntry(key, value);
		}
	}
	
	class HugeMapEntry extends AbstractEntry {
		public HugeMapEntry(K key, V value) {
			super(key, value);
		}

		@Override
		public V setValue(V value) {
			return setValueInternal(value);
		}
	}
	
	/**
	 * Builds a {@link HugeHashMap}.
	 *
	 * @param <K> the type of keys
	 * @param <V> the type of values
	 */
	public static class Builder<K, V> extends AbstractHugeMapBuilder<K, V> {

		private HugeHashMap<K, V> result;
		
		private boolean built;
		
		private HugeHashMap<K, V> getMap() {
			if (result == null) {
				result = new HugeHashMap<K, V>(getMemoryManager(), getKeyConverter(), getValueConverter(), isFaster(), getCapacity());
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
		public HugeHashMap<K, V> build() {
			if (built) {
				throw new IllegalStateException("Has already been built.");
			}
			built = true;
			return getMap();
		}
	}
}
