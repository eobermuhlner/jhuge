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
 * <p>Important:
 * Changes to the keys or values outside of the huge collection are <strong>not</strong> automatically reflected by the serialized form in the collection.
 * In this case you must replace the stored object with the modified object.</p>
 * 
 * <h2>Heap Consumption</h2>
 * 
 * <h3>Heap in normal mode</h3>
 * 
 * <p>In normal mode the {@link HugeHashMap} stores everything in the {@link MemoryManager}
 * and therefore typically completely outside of the Java heap.</p>
 * 
 * <p>The following table shows the heap consumption in normal mode when filled with 10000 String to String entries:</p>
<pre>
Class Name                                                  | Objects | Shallow Heap
-------------------------------------------------------------------------------------
long[]                                                      |       1 |        2,064
java.lang.Object[]                                          |       1 |           56
ch.obermuhlner.jhuge.memory.MemoryMappedFileManager         |       1 |           40
ch.obermuhlner.jhuge.collection.HugeHashMap                 |       1 |           32
ch.obermuhlner.jhuge.collection.internal.HugeIntLongArrayMap|       1 |           32
ch.obermuhlner.jhuge.converter.CompactConverter             |       2 |           32
java.util.ArrayList                                         |       1 |           24
ch.obermuhlner.jhuge.collection.internal.HugeLongArray      |       1 |           24
ch.obermuhlner.jhuge.collection.internal.JavaLongArray      |       1 |           16
Total: 9 entries                                            |      10 |        2,320
-------------------------------------------------------------------------------------
</pre>
 * 
 * <p>The {@link HugeHashMap} in normal mode will always use pretty much the same amount of Java heap no matter how many entries it contains.</p>
 * 
 * <h3>Heap in faster mode</h3>
 * 
 * <p>In {@link HugeHashMap.Builder#faster() faster} mode the {@link HugeHashMap} stores only the keys and values in the {@link MemoryManager}.</p>
 * <p>The infrastructure data to quickly access the correct {@link MemoryManager} block of a key or value
 * is stored in Java objects and occupies Java heap.</p>
 * 
 * <p>The following table shows the heap consumption in faster mode when filled with 10000 String to String entries:</p>
<pre>
Class Name                                                  | Objects | Shallow Heap
-------------------------------------------------------------------------------------
long[]                                                      |  10,001 |      320,080
java.util.HashMap$Entry                                     |  10,000 |      240,000
java.lang.Integer                                           |  10,000 |      160,000
java.util.HashMap$Entry[]                                   |       1 |       65,552
java.lang.Object[]                                          |       1 |           56
ch.obermuhlner.jhuge.memory.MemoryMappedFileManager         |       1 |           40
java.util.HashMap                                           |       1 |           40
ch.obermuhlner.jhuge.collection.HugeHashMap                 |       1 |           32
ch.obermuhlner.jhuge.converter.CompactConverter             |       2 |           32
java.util.ArrayList                                         |       1 |           24
ch.obermuhlner.jhuge.collection.internal.JavaIntLongArrayMap|       1 |           16
ch.obermuhlner.jhuge.collection.internal.JavaLongArray      |       1 |           16
Total: 12 entries                                           |  30,011 |      785,888
-------------------------------------------------------------------------------------
</pre>
 * 
 * <p>The {@link HugeHashMap} in faster mode uses about 72 bytes per entry, independent of the size of keys and values.</p>
 * 
 * <h3>Heap comparison with HashMap</h3>
 * 
 * <p>As a comparison the following table shows the heap consumption of a <code>java.util.HashMap</code> filled with 10000 String to String entries:</p>
<pre>
Class Name               | Objects | Shallow Heap
--------------------------------------------------
char[]                   |  20,000 |      551,920
java.lang.String         |  20,000 |      480,000
java.util.HashMap$Entry  |  10,000 |      240,000
java.util.HashMap$Entry[]|       1 |       65,552
java.util.HashMap        |       1 |           40
Total: 5 entries         |  50,002 |    1,337,512
--------------------------------------------------
</pre>
 * 
 * <p>The maps where filled with relatively small Strings. A real world example would probably store larger objects and use therefore more Java heap.</p>
 * <p>Keys: <code>"key" + i</code></p>
 * <p>Values: <code>"X" + value</code></p>
 * 
 * <h2>Performance</h2>
 * 
 * Performance was measured on a
<pre>
Intel(R) Core(TM) i7 CPU       M 620  2.67GHz (4 CPUs), ~2.7GHz
</pre>
 * running a
<pre>
Java(TM) SE Runtime Environment (build 1.6.0_23-b05)
Java HotSpot(TM) 64-Bit Server VM (build 19.0-b09, mixed mode)
</pre>
 * 
 * <h3>Performance in normal mode</h3>
 * 
 * <img src="doc-files/HugeHashMap [compact]_0.95.png"/>
 * 
 * <h3>Performance in faster mode</h3>
 * 
 * <img src="doc-files/HugeHashMap [fast]_0.95.png"/>
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
		public Builder<K, V> compressKey() {
			super.compressKey();
			return this;
		}
		
		@Override
		public AbstractHugeMapBuilder<K, V> compressValue() {
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
