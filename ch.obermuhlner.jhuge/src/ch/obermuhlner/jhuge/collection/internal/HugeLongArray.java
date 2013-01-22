package ch.obermuhlner.jhuge.collection.internal;

import java.nio.ByteBuffer;

import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link LongArray} implementation that stores the values in a {@link MemoryManager}.
 */
public class HugeLongArray implements LongArray {

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
		
		address = memoryManager.allocate(capacity * 8);
	}
	
	@Override
	public long set(int index, long value) {
		checkSize(index);
		
		byte[] data = memoryManager.read(address);
		ByteBuffer wrap = ByteBuffer.wrap(data);
		wrap.position(index * 8);
		long oldValue = wrap.getLong();
		wrap.position(index * 8);
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
		if (data.length > size * 8) {
			// move in block
			System.arraycopy(data, index * 8, data, index * 8 + 8, (size - index) * 8);
			ByteBuffer wrap = ByteBuffer.wrap(data);
			wrap.position(index * 8);
			wrap.putLong(value);
			memoryManager.write(address, data);
		} else {
			// copy into new block
			byte[] newData = new byte[data.length * 2];
			System.arraycopy(data, 0, newData, 0, index * 8);
			System.arraycopy(data, index * 8, newData, index * 8 + 8, (size - index) * 8);
			ByteBuffer wrap = ByteBuffer.wrap(newData);
			wrap.position(index * 8);
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
		wrap.position(index * 8);
		long value = wrap.getLong();
		return value;
	}

	@Override
	public long remove(int index) {
		checkSize(index);

		byte[] data = memoryManager.read(address);
		ByteBuffer wrap = ByteBuffer.wrap(data);
		wrap.position(index * 8);
		long oldValue = wrap.getLong();
		System.arraycopy(data, 0, data, 0, index * 8);
		System.arraycopy(data, index * 8 + 8, data, index * 8, (size - index - 1) * 8);
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
