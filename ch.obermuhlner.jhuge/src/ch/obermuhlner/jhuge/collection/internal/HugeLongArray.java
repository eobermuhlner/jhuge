package ch.obermuhlner.jhuge.collection.internal;

import java.nio.ByteBuffer;

import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link LongArray} implementation that stores the values in a {@link MemoryManager}.
 */
public class HugeLongArray extends AbstractLongArray {

	private static final int ELEMENT_SIZE = 8;
	
	private final MemoryManager memoryManager;

	private long address;
	
	private int size;

	/**
	 * Constructs a {@link HugeLongArray}.
	 * 
	 * @param memoryManager the {@link MemoryManager} to store the values
	 */
	public HugeLongArray(MemoryManager memoryManager) {
		this(memoryManager, 8);
	}
	
	/**
	 * Constructs a {@link HugeLongArray} with the specified initial capacity.
	 * 
	 * @param memoryManager the {@link MemoryManager} to store the values
	 * @param capacity the initial capacity
	 */
	public HugeLongArray(MemoryManager memoryManager, int capacity) {
		this.memoryManager = memoryManager;
		
		address = memoryManager.allocate(capacity * ELEMENT_SIZE);
	}
	
	@Override
	public long set(int index, long value) {
		checkSize(index);
		
		byte[] data = memoryManager.read(address);
		ByteBuffer wrap = ByteBuffer.wrap(data);
		wrap.position(index * ELEMENT_SIZE);
		long oldValue = wrap.getLong();
		wrap.position(index * ELEMENT_SIZE);
		wrap.putLong(value);

		memoryManager.write(address, data);
		return oldValue;
	}

	@Override
	public void add(int index, long value) {
		checkSizeIncludingRightBound(index);
		
		addInternal(index, value);
	}

	@Override
	public void add(long value) {
		addInternal(size, value);
	}

	private void addInternal(int index, long value) {
		byte[] data = memoryManager.read(address);
		if (data.length > size * ELEMENT_SIZE) {
			// move in block
			System.arraycopy(data, index * ELEMENT_SIZE, data, index * ELEMENT_SIZE + ELEMENT_SIZE, (size - index) * ELEMENT_SIZE);
			ByteBuffer wrap = ByteBuffer.wrap(data);
			wrap.position(index * ELEMENT_SIZE);
			wrap.putLong(value);
			memoryManager.write(address, data);
		} else {
			// copy into new block
			byte[] newData = new byte[data.length * 2];
			System.arraycopy(data, 0, newData, 0, index * ELEMENT_SIZE);
			System.arraycopy(data, index * ELEMENT_SIZE, newData, index * ELEMENT_SIZE + ELEMENT_SIZE, (size - index) * ELEMENT_SIZE);
			ByteBuffer wrap = ByteBuffer.wrap(newData);
			wrap.position(index * ELEMENT_SIZE);
			wrap.putLong(value);
			memoryManager.free(address);
			address = memoryManager.allocate(newData);
		}
		
		size++;
	}

	@Override
	public long get(int index) {
		checkSize(index);

		byte[] data = memoryManager.read(address);
		ByteBuffer wrap = ByteBuffer.wrap(data);
		wrap.position(index * ELEMENT_SIZE);
		long value = wrap.getLong();
		return value;
	}

	@Override
	public long remove(int index) {
		checkSize(index);

		byte[] data = memoryManager.read(address);
		ByteBuffer wrap = ByteBuffer.wrap(data);
		wrap.position(index * ELEMENT_SIZE);
		long oldValue = wrap.getLong();
		System.arraycopy(data, 0, data, 0, index * ELEMENT_SIZE);
		System.arraycopy(data, index * ELEMENT_SIZE + ELEMENT_SIZE, data, index * ELEMENT_SIZE, (size - index - 1) * ELEMENT_SIZE);
		memoryManager.write(address, data);
		
		size--;
		
		return oldValue;
	}
	
	/**
	 * Sets the size of the array.
	 * 
	 * @param newSize the new size
	 */
	public void setSize(int newSize) {
		byte[] data = memoryManager.read(address);
		if (data.length < newSize * ELEMENT_SIZE) {
			byte[] newData = new byte[newSize * ELEMENT_SIZE];
			System.arraycopy(data, 0, newData, 0, size * ELEMENT_SIZE);
			memoryManager.free(address);
			address = memoryManager.allocate(newData);
		} else {
			for (int i = newSize; i < size; i++) {
				set(i, 0);
			}
		}
		size = newSize;
	}

	@Override
	public void clear() {
		size = 0;
	}

	@Override
	public int size() {
		return size;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{size=" + size + "}";
//		StringBuilder result = new StringBuilder();
//		result.append('[');
//		for (int i = 0; i < size; i++) {
//			if (i > 0) {
//				result.append(", ");
//			}
//			result.append(get(i));
//		}
//		result.append(']');
//		return result.toString();
	}

	private void checkSize(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
		}		
	}

	private void checkSizeIncludingRightBound(int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
		}
	}
}
