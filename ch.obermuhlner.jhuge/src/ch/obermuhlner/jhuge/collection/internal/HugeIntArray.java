package ch.obermuhlner.jhuge.collection.internal;

import java.nio.ByteBuffer;

import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link IntArray} implementation that stores the values in a {@link MemoryManager}.
 */
public class HugeIntArray extends AbstractIntArray {

	private static final int ELEMENT_SIZE = 4;
	
	private final MemoryManager memoryManager;

	private long address;
	
	private int size;

	/**
	 * Constructs a {@link HugeIntArray}.
	 * 
	 * @param memoryManager the {@link MemoryManager} to store the values
	 */
	public HugeIntArray(MemoryManager memoryManager) {
		this(memoryManager, 8);
	}
	
	/**
	 * Constructs a {@link HugeIntArray} with the specified initial capacity.
	 * 
	 * @param memoryManager the {@link MemoryManager} to store the values
	 * @param capacity the initial capacity
	 */
	public HugeIntArray(MemoryManager memoryManager, int capacity) {
		this.memoryManager = memoryManager;
		
		address = memoryManager.allocate(capacity * ELEMENT_SIZE);
	}
	
	@Override
	public int set(int index, int value) {
		checkSize(index);
		
		byte[] data = memoryManager.read(address);
		ByteBuffer wrap = ByteBuffer.wrap(data);
		wrap.position(index * ELEMENT_SIZE);
		int oldValue = wrap.getInt();
		wrap.position(index * ELEMENT_SIZE);
		wrap.putInt(value);

		memoryManager.write(address, data);
		return oldValue;
	}

	@Override
	public void add(int index, int value) {
		checkSizeIncludingRightBound(index);
		
		addInternal(index, value);
	}

	@Override
	public void add(int value) {
		addInternal(size, value);
	}

	private void addInternal(int index, int value) {
		byte[] data = memoryManager.read(address);
		if (data.length > size * ELEMENT_SIZE) {
			// move in block
			System.arraycopy(data, index * ELEMENT_SIZE, data, index * ELEMENT_SIZE + ELEMENT_SIZE, (size - index) * ELEMENT_SIZE);
			ByteBuffer wrap = ByteBuffer.wrap(data);
			wrap.position(index * ELEMENT_SIZE);
			wrap.putInt(value);
			memoryManager.write(address, data);
		} else {
			// copy into new block
			byte[] newData = new byte[data.length * 2];
			System.arraycopy(data, 0, newData, 0, index * ELEMENT_SIZE);
			System.arraycopy(data, index * ELEMENT_SIZE, newData, index * ELEMENT_SIZE + ELEMENT_SIZE, (size - index) * ELEMENT_SIZE);
			ByteBuffer wrap = ByteBuffer.wrap(newData);
			wrap.position(index * ELEMENT_SIZE);
			wrap.putInt(value);
			memoryManager.free(address);
			address = memoryManager.allocate(newData);
		}
		
		size++;
	}

	@Override
	public int get(int index) {
		checkSize(index);

		byte[] data = memoryManager.read(address);
		ByteBuffer wrap = ByteBuffer.wrap(data);
		wrap.position(index * ELEMENT_SIZE);
		int value = wrap.getInt();
		return value;
	}

	@Override
	public int remove(int index) {
		checkSize(index);

		byte[] data = memoryManager.read(address);
		ByteBuffer wrap = ByteBuffer.wrap(data);
		wrap.position(index * ELEMENT_SIZE);
		int oldValue = wrap.getInt();
		System.arraycopy(data, 0, data, 0, index * ELEMENT_SIZE);
		System.arraycopy(data, index * ELEMENT_SIZE + ELEMENT_SIZE, data, index * ELEMENT_SIZE, (size - index - 1) * ELEMENT_SIZE);
		memoryManager.write(address, data);
		
		size--;
		
		return oldValue;
	}

	@Override
	public void clear() {
		size = 0;
	}

	@Override
	public int size() {
		return size;
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
