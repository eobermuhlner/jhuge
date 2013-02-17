package ch.obermuhlner.jhuge.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeSetBuilder;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link Set} that stores elements in a {@link MemoryManager}.
 * 
 * <p>The implementation mimics a {@link HashSet}.</p>
 * 
 * <p>Access to single elements is O(1).</p>
 * 
 * <p>In order to store the keys and values in the {@link MemoryManager} they must be serialized and deserialized to read them.
 * This is done by a {@link Converter} which can be specified in the {@link Builder}.
 * The default {@link Converter} can handle instances of all serializable classes.</p>
 * 
 * <p>Important:
 * Changes to the elements outside of the huge collection are <strong>not</strong> automatically reflected by the serialized form in the collection.
 * In this case you must replace the stored element with the modified element.</p>
 * 
 * <h2>Heap Consumption</h2>
 * 
 * <h3>Heap in normal mode</h3>
 * 
 * <p>In normal mode the {@link HugeHashSet} stores everything in the {@link MemoryManager}
 * and therefore typically completely outside of the Java heap.</p>
 * 
 * <p>The following table shows the heap consumption in normal mode when filled with 10000 strings:</p>
<pre>
Class Name                                                  | Objects | Shallow Heap
-------------------------------------------------------------------------------------
long[]                                                      |       1 |        1,040
java.lang.Object[]                                          |       1 |           56
ch.obermuhlner.jhuge.memory.MemoryMappedFileManager         |       1 |           40
ch.obermuhlner.jhuge.collection.internal.HugeIntLongArrayMap|       1 |           32
ch.obermuhlner.jhuge.collection.HugeHashSet                 |       1 |           24
java.util.ArrayList                                         |       1 |           24
ch.obermuhlner.jhuge.collection.internal.HugeLongArray      |       1 |           24
ch.obermuhlner.jhuge.collection.internal.JavaLongArray      |       1 |           16
ch.obermuhlner.jhuge.converter.CompactConverter             |       1 |           16
Total: 9 entries                                            |       9 |        1,272
-------------------------------------------------------------------------------------
</pre>
 * 
 * <h3>Heap in faster mode</h3>
 * 
 * <p>In {@link HugeHashSet.Builder#faster() faster} mode the {@link HugeHashSet} stores only the elements in the {@link MemoryManager}.</p>
 * <p>The infrastructure data to quickly access the correct {@link MemoryManager} block of an element
 * is stored in Java objects and occupies Java heap.</p>
 * 
 * <p>The following table shows the heap consumption in faster mode when filled with 10000 strings:</p>
<pre>
Class Name                                                  | Objects | Shallow Heap
-------------------------------------------------------------------------------------
long[]                                                      |  10,001 |      240,080
java.util.HashMap$Entry                                     |  10,000 |      240,000
java.lang.Integer                                           |  10,000 |      160,000
java.util.HashMap$Entry[]                                   |       1 |       65,552
java.lang.Object[]                                          |       1 |           56
ch.obermuhlner.jhuge.memory.MemoryMappedFileManager         |       1 |           40
java.util.HashMap                                           |       1 |           40
ch.obermuhlner.jhuge.collection.HugeHashSet                 |       1 |           24
java.util.ArrayList                                         |       1 |           24
ch.obermuhlner.jhuge.collection.internal.JavaIntLongArrayMap|       1 |           16
ch.obermuhlner.jhuge.collection.internal.JavaLongArray      |       1 |           16
ch.obermuhlner.jhuge.converter.CompactConverter             |       1 |           16
Total: 12 entries                                           |  30,010 |      705,864
-------------------------------------------------------------------------------------
</pre>
 * 
 * <p>The {@link HugeHashSet} in faster mode uses about 64 bytes per entry, independent of the size of the elements.</p>
 * 
 * <h3>Heap comparison with HashSet</h3>
 * 
 * <p>As a comparison the following table shows the heap consumption of a <code>java.util.HashSet</code> filled with 10000 strings:</p>
<pre>
Class Name               | Objects | Shallow Heap
--------------------------------------------------
java.util.HashMap$Entry  |  10,000 |      240,000
java.lang.String         |  10,000 |      240,000
char[]                   |  10,000 |      239,920
java.util.HashMap$Entry[]|       1 |       65,552
java.util.HashMap        |       1 |           40
java.util.HashSet        |       1 |           16
Total: 6 entries         |  30,003 |      785,528
--------------------------------------------------
</pre>
 * 
 * <p>The collections where filled with relatively small Strings. A real world example would probably store larger objects and use therefore more Java heap.</p>
 * <p>Elements: <code>"X" + value</code></p>
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
 * <img src="doc-files/HugeHashSet [compact]_0.95.png"/>
 * 
 * <h3>Performance in faster mode</h3>
 * 
 * <img src="doc-files/HugeHashSet [fast]_0.95.png"/>
 * 
 * @param <E> the type of elements
 */
public class HugeHashSet<E> extends AbstractHugeHashSet<E> {

	private HugeHashSet(MemoryManager memoryManager, Converter<E> converter, boolean faster, int capacity) {
		super(memoryManager, converter, faster, capacity);
	}

	@Override
	public boolean add(E element) {
		return addInternal(element);
	}
	
	@Override
	public boolean remove(Object element) {
		return removeInternal(element);
	}

	@Override
	public void clear() {
		clearInternal();
	}
	@Override
	public Iterator<E> iterator() {
		return new HugeHashSetIterator();
	}

	private class HugeHashSetIterator extends AbstractHugeHashSetIterator {
		@Override
		public void remove() {
			removeInternal();
		}
	}
	
	/**
	 * Builds a {@link HugeHashSet}.
	 *
	 * @param <E> the type of elements
	 */
	public static class Builder<E> extends AbstractHugeSetBuilder<E> {
		
		private HugeHashSet<E> result;
		
		private boolean built;
		
		private HugeHashSet<E> getSet() {
			if (result == null) {
				result = new HugeHashSet<E>(getMemoryManager(), getElementConverter(), isFaster(), getCapacity());
			}
			return result;
		}
		
		@Override
		public Builder<E> classLoader(ClassLoader classLoader) {
			super.classLoader(classLoader);
			return this;
		}
		
		@Override
		public Builder<E> element(Class<E> elementClass) {
			super.element(elementClass);
			return this;
		}
		
		@Override
		public Builder<E> element(Converter<E> elementConverter) {
			super.element(elementConverter);
			return this;
		}
		
		@Override
		public Builder<E> compressElement() {
			super.compressElement();
			return this;
		}
		
		@Override
		public Builder<E> bufferSize(int bufferSize) {
			super.bufferSize(bufferSize);
			return this;
		}
		
		@Override
		public Builder<E> blockSize(int blockSize) {
			super.blockSize(blockSize);
			return this;
		}
		
		@Override
		public Builder<E> memoryManager(MemoryManager memoryManager) {
			super.memoryManager(memoryManager);
			return this;
		}
		
		@Override
		public Builder<E> faster() {
			super.faster();
			return this;
		}
		
		@Override
		public Builder<E> capacity(int capacity) {
			super.capacity(capacity);
			return this;
		}
				
		@Override
		public Builder<E> add(E element) {
			getSet().add(element);
			return this;
		}
		
		@Override
		public Builder<E> addAll(Collection<E> elements) {
			for (E element : elements) {
				add(element);
			}
			return this;
		}
		
		@Override
		public Builder<E> addAll(E... elements) {
			for (E element : elements) {
				add(element);
			}
			return this;
		}

		@Override
		public HugeHashSet<E> build() {
			if (built) {
				throw new IllegalStateException("Has already been built.");
			}
			built = true;
			return getSet();
		}
	}	
}
