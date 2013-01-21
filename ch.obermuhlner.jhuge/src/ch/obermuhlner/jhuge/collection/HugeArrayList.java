package ch.obermuhlner.jhuge.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeListBuilder;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link List} that stores elements in a {@link MemoryManager}.
 * 
 * <p>The implementation mimics an {@link ArrayList}.</p>
 * 
 * <p>Access to single elements is O(1).</p>
 * <p>Removing a single elements is O(n).</p>
 * 
 * <p>In order to store the elements in the {@link MemoryManager} they must be serialized and deserialized to read them.
 * This is done by a {@link Converter} which can be specified in the {@link Builder}.</p>
 * 
 * @param <E> the type of elements
 */
public class HugeArrayList<E> extends AbstractList<E> {

	private static final byte[] EMPTY_DATA = new byte[0];

	private final MemoryManager memoryManager;
	
	private final List<Long> addresses = new ArrayList<Long>();

	private final Converter<E> converter;

	private HugeArrayList(MemoryManager memoryManager, Converter<E> converter) {
		this.memoryManager = memoryManager;
		this.converter = converter;
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
		long oldAddress = addresses.remove(index);
		byte[] oldData = memoryManager.read(oldAddress);
		E oldElement = deserializeElement(oldData);
		memoryManager.free(oldAddress);
		
		return oldElement;
	}

	@Override
	public E get(int index) {
		byte[] data = memoryManager.read(addresses.get(index));
		E element = deserializeElement(data);
		return element;
	}

	@Override
	public void clear() {
		addresses.clear();
		memoryManager.reset();
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
		
		private HugeArrayList<E> getList() {
			if (result == null) {
				result = new HugeArrayList<E>(getMemoryManager(), getElementConverter());
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
			return getList();
		}
	}
}
