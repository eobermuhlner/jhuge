package ch.obermuhlner.jhuge.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeListBuilder;
import ch.obermuhlner.jhuge.collection.internal.HugeLongArray;
import ch.obermuhlner.jhuge.collection.internal.JavaLongArray;
import ch.obermuhlner.jhuge.collection.internal.LongArray;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link List} that stores elements in a {@link MemoryManager}.
 * 
 * <p>The implementation mimics an {@link ArrayList}.</p>
 * 
 * <p>Access to single elements is O(1).</p>
 * <p>Removing a single element is O(n).</p>
 * 
 * <p>In order to store the elements in the {@link MemoryManager} they must be serialized and deserialized to read them.
 * This is done by a {@link Converter} which can be specified in the {@link Builder}.</p>
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
Class Name                                            |   Objects | Shallow Heap
---------------------------------------------------------------------------------
                                                      |           |             
java.util.HashMap$Entry                               |         7 |          168
java.lang.Long                                        |         6 |           96
java.util.HashMap$Entry[]                             |         1 |           80
java.lang.Object[]                                    |         1 |           56
ch.obermuhlner.jhuge.collection.internal.LongIntMap   |         1 |           40
java.lang.Integer                                     |         2 |           32
ch.obermuhlner.jhuge.memory.MemoryMappedFileManager   |         1 |           32
java.util.ArrayList                                   |         1 |           24
ch.obermuhlner.jhuge.collection.internal.HugeLongArray|         1 |           24
ch.obermuhlner.jhuge.collection.HugeArrayList         |         1 |           24
ch.obermuhlner.jhuge.converter.CompactConverter       |         1 |           16
java.util.HashMap$EntrySet                            |         1 |           16
Total: 12 entries                                     |        24 |          608
---------------------------------------------------------------------------------
</pre>
 * 
 * <h3>Heap in faster mode</h3>
 * 
 * <p>In {@link HugeArrayList.Builder#faster() faster} mode the {@link HugeArrayList} stores only the elements in the {@link MemoryManager}.</p>
 * <p>The infrastructure data to quickly access the correct {@link MemoryManager} block of an element
 * is stored in Java objects and occupies Java heap.</p>
 * 
 * <p>The following table shows the heap consumption in normal mode when filled with 10000 strings:</p>
<pre>
Class Name                                            |   Objects | Shallow Heap
---------------------------------------------------------------------------------
                                                      |           |             
long[]                                                |         1 |      131,088
java.util.HashMap$Entry[]                             |         1 |           80
java.lang.Object[]                                    |         1 |           56
ch.obermuhlner.jhuge.collection.internal.LongIntMap   |         1 |           40
ch.obermuhlner.jhuge.memory.MemoryMappedFileManager   |         1 |           32
java.util.ArrayList                                   |         1 |           24
java.util.HashMap$Entry                               |         1 |           24
ch.obermuhlner.jhuge.collection.HugeArrayList         |         1 |           24
java.lang.Integer                                     |         1 |           16
java.lang.Long                                        |         1 |           16
ch.obermuhlner.jhuge.converter.CompactConverter       |         1 |           16
ch.obermuhlner.jhuge.collection.internal.JavaLongArray|         1 |           16
java.util.HashMap$EntrySet                            |         1 |           16
Total: 13 entries                                     |        13 |      131,448
---------------------------------------------------------------------------------
</pre>
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
public class HugeArrayList<E> extends AbstractList<E> {

	private static final byte[] EMPTY_DATA = new byte[0];

	private final MemoryManager memoryManager;
	
	private final LongArray addresses;

	private final Converter<E> converter;

	private HugeArrayList(MemoryManager memoryManager, Converter<E> converter, boolean faster, int capacity) {
		this.memoryManager = memoryManager;
		this.converter = converter;
		this.addresses = faster ? new JavaLongArray(capacity) : new HugeLongArray(memoryManager, capacity);
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
	 * Returns the element {@link Converter}.
	 * 
	 * @return the element {@link Converter}
	 */
	Converter<E> getElementConverter() {
		return converter;
	}
	
	@Override
	public E set(int index, E element) {
		byte[] data = serializeElement(element);
		long address = memoryManager.allocate(data);

		long oldAddress = addresses.set(index, address);
		byte[] oldData = memoryManager.read(oldAddress);
		E oldElement = deserializeElement(oldData);
		memoryManager.free(oldAddress);
		
		return oldElement;
	}

	@Override
	public void add(int index, E element) {
		byte[] data = serializeElement(element);
		long address = memoryManager.allocate(data);
		
		addresses.add(index, address);
	}

	@Override
	public E remove(int index) {
		long oldAddress1 = addresses.remove(index);
		long oldAddress = oldAddress1;
		byte[] oldData = memoryManager.read(oldAddress);
		E oldElement = deserializeElement(oldData);
		memoryManager.free(oldAddress);
		
		return oldElement;
	}

	@Override
	public E get(int index) {
		long address = addresses.get(index);
		byte[] data = memoryManager.read(address);
		E element = deserializeElement(data);
		return element;
	}

	@Override
	public void clear() {
		for (int i = 0; i < addresses.size(); i++) {
			long address = addresses.get(i);
			memoryManager.free(address);
		}
		addresses.clear();
		
		//memoryManager.compact();
	}
	
	@Override
	public int size() {
		return addresses.size();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{size=" + size() + "}";
	}
	
	private byte[] serializeElement(E element) {
		return element == null ? EMPTY_DATA : converter.serialize(element);
	}

	private E deserializeElement(byte[] data) {
		return (data == null || data.length == 0) ? null : converter.deserialize(data);
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
