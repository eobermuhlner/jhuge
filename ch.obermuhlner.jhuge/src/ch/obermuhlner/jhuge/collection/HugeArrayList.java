package ch.obermuhlner.jhuge.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeListBuilder;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A mutable {@link List} that stores elements in a {@link MemoryManager}.
 * 
 * <p>The implementation mimics an {@link ArrayList}.</p>
 * 
 * <p>Access to single elements is O(1).</p>
 * <p>Removing a single element is O(n).</p>
 * 
 * <p>In order to store the elements in the {@link MemoryManager} they must be serialized and deserialized to read them.
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
 * <p>In normal mode the {@link HugeArrayList} stores everything in the {@link MemoryManager}
 * and therefore typically completely outside of the Java heap.</p>
 * 
 * <p>The following table shows the heap consumption in normal mode when filled with 10000 strings:</p>
<pre>
Class Name                                            | Objects | Shallow Heap
-------------------------------------------------------------------------------
long[]                                                |       1 |          144
java.lang.Object[]                                    |       1 |           56
ch.obermuhlner.jhuge.memory.MemoryMappedFileManager   |       1 |           40
ch.obermuhlner.jhuge.collection.HugeArrayList         |       1 |           24
java.util.ArrayList                                   |       1 |           24
ch.obermuhlner.jhuge.collection.internal.HugeLongArray|       1 |           24
ch.obermuhlner.jhuge.converter.CompactConverter       |       1 |           16
ch.obermuhlner.jhuge.collection.internal.JavaLongArray|       1 |           16
Total: 8 entries                                      |       8 |          344
-------------------------------------------------------------------------------
</pre>
 * 
 * <h3>Heap in faster mode</h3>
 * 
 * <p>In {@link HugeArrayList.Builder#faster() faster} mode the {@link HugeArrayList} stores only the elements in the {@link MemoryManager}.</p>
 * <p>The infrastructure data to quickly access the correct {@link MemoryManager} block of an element
 * is stored in Java objects and occupies Java heap.</p>
 * 
 * <p>The following table shows the heap consumption in faster mode when filled with 10000 strings:</p>
<pre>
Class Name                                            | Objects | Shallow Heap
-------------------------------------------------------------------------------
long[]                                                |       2 |      131,168
java.lang.Object[]                                    |       1 |           56
ch.obermuhlner.jhuge.memory.MemoryMappedFileManager   |       1 |           40
ch.obermuhlner.jhuge.collection.internal.JavaLongArray|       2 |           32
ch.obermuhlner.jhuge.collection.HugeArrayList         |       1 |           24
java.util.ArrayList                                   |       1 |           24
ch.obermuhlner.jhuge.converter.CompactConverter       |       1 |           16
Total: 7 entries                                      |       9 |      131,360
-------------------------------------------------------------------------------
</pre>
 * 
 * <p>The {@link HugeArrayList} in faster mode uses about 8-16 bytes per entry, independent of the size of the elements.</p>
 * 
 * <h3>Heap comparison with ArrayList</h3>
 * 
 * <p>As a comparison the following table shows the heap consumption of a <code>java.util.ArrayList</code> filled with 10000 strings:</p>
<pre>
Class Name         | Objects | Shallow Heap
--------------------------------------------
java.lang.String   |  10,000 |      240,000
char[]             |  10,000 |      239,920
java.lang.Object[] |       1 |       46,496
java.util.ArrayList|       1 |           24
Total: 4 entries   |  20,002 |      526,440
--------------------------------------------
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
 * <img src="doc-files/HugeArrayList [compact]_0.95.png"/>
 * 
 * <h3>Performance in faster mode</h3>
 * 
 * <img src="doc-files/HugeArrayList [fast]_0.95.png"/>
 * 
 * @param <E> the type of elements
 */
public class HugeArrayList<E> extends AbstractHugeArrayList<E> {

	private HugeArrayList(MemoryManager memoryManager, Converter<E> converter, boolean faster, int capacity) {
		super(memoryManager, converter, faster, capacity);
	}

	@Override
	public E set(int index, E element) {
		return setInternal(index, element);
	}
	
	@Override
	public void add(int index, E element) {
		addInternal(index, element);
	}

	@Override
	public E remove(int index) {
		return removeInternal(index);
	}
	@Override
	public void clear() {
		clearInternal();
	}

	/**
	 * Builds a {@link HugeArrayList}.
	 * 
	 * @param <E> the type of elements
	 */
	public static class Builder<E> extends AbstractHugeListBuilder<E> {
		
		private HugeArrayList<E> result;
		
		private boolean built;
		
		private HugeArrayList<E> getList() {
			if (result == null) {
				result = new HugeArrayList<E>(getMemoryManager(), getElementConverter(), isFaster(), getCapacity());
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
			getList().add(element);
			return this;
		}
		
		@Override
		public Builder<E> addAll(Collection<E> elements) {
			getList().addAll(elements);
			return this;
		}
		
		@Override
		public Builder<E> addAll(E... elements) {
			return addAll(Arrays.asList(elements));
		}
		
		@Override
		public HugeArrayList<E> build() {
			if (built) {
				throw new IllegalStateException("Has already been built.");
			}
			built = true;
			return getList();
		}
	}
}
