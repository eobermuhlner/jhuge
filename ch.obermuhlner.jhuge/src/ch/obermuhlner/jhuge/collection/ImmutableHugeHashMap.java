package ch.obermuhlner.jhuge.collection;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeMapBuilder;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * An immutable {@link Map} that stores elements in a {@link MemoryManager}.
 * 
 * <p>The mutating operations throw {@link UnsupportedOperationException}:</p>
 * <ul>
 * <li> {@link #put(Object, Object)}</li>
 * <li> {@link #putAll(Map)}</li>
 * <li> {@link #remove(Object)}</li>
 * <li> {@link #clear()}</li>
 * <li> {@link java.util.Map.Entry#setValue(Object)} on the entries in the {@link #entrySet() entrySet}</li>
 * <li> {@link Iterator#remove()} when iterating over the {@link #entrySet() entrySet}</li>
 * </ul>
 * 
 * <p>In order to create an {@link ImmutableHugeHashMap} you must add the elements in the {@link Builder}.</p>
 * 
 * <p>Other than the mutating operations all semantics, memory consumption and performance are identical to {@link HugeHashMap}.</p>
 * 
 * @param <K> the type of keys
 * @param <V> the type of values
 * @see HugeHashMap
 */
public class ImmutableHugeHashMap<K, V> extends AbstractHugeHashMap<K, V> {

	private int hashCode;

	/**
	 * Initializes the hashCode.
	 * 
	 * Until this method is called the hashCode() and equals() methods are not valid and may not be called.
	 * Once the Builder is finished with adding all entries he calls initializeHashCode() before this instance is given to the client code.
	 */
	private ImmutableHugeHashMap(MemoryManager memoryManager, Converter<K> keyConverter, Converter<V> valueConverter, boolean faster, int capacity) {
		super(memoryManager, keyConverter, valueConverter, faster, capacity);
	}

	private void initializeHashCode() {
		hashCode = super.hashCode();
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof ImmutableHugeArrayList) {
			// if it is same class as this then we know that hashCode() is cheap (already initialized) -> fast test for not-equal
			ImmutableHugeHashMap<?, ?> other = (ImmutableHugeHashMap<?, ?>) object;
			if (hashCode() != other.hashCode()) {
				return false;
			}
		}
		
		return super.equals(object);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
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
			throw new UnsupportedOperationException();
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
	 * Builds an {@link ImmutableHugeHashMap}.
	 *
	 * @param <K> the type of keys
	 * @param <V> the type of values
	 */
	public static class Builder<K, V> extends AbstractHugeMapBuilder<K, V> {

		private ImmutableHugeHashMap<K, V> result;
		
		private boolean built;
		
		private ImmutableHugeHashMap<K, V> getMap() {
			if (result == null) {
				result = new ImmutableHugeHashMap<K, V>(getMemoryManager(), getKeyConverter(), getValueConverter(), isFaster(), getCapacity());
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
		public Builder<K, V> compressKey() {
			super.compressKey();
			return this;
		}
		
		@Override
		public Builder<K, V> compressValue() {
			super.compressValue();
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
			getMap().putInternal(key, value);
			return this;
		}
		
		@Override
		public Builder<K, V> putAll(Map<K, V> map) {
			AbstractHugeHashMap<K, V> hugeMap = getMap();
			for (Entry<K, V> entry : map.entrySet()) {
				hugeMap.putInternal(entry.getKey(), entry.getValue());
			}
			return this;
		}

		@Override
		public ImmutableHugeHashMap<K, V> build() {
			if (built) {
				throw new IllegalStateException("Has already been built.");
			}
			built = true;
			
			ImmutableHugeHashMap<K, V> map = getMap();
			map.initializeHashCode();
			return map;
		}
	}
}
