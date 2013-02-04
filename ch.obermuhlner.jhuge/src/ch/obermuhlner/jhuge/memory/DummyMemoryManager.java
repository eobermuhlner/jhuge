package ch.obermuhlner.jhuge.memory;

import java.util.HashMap;
import java.util.Map;

/**
 * A dummy implementation of a {@link MemoryManager}.
 * 
 * <p>This implementation uses pure Java to manage the allocated memory blocks.
 * It is useful as a reference implementation and for testing.</p>
 */
public class DummyMemoryManager extends AbstractMemoryManager {

	private long nextAddress;
	
	private Map<Long, byte[]> blocks = new HashMap<Long, byte[]>();
	
	/**
	 * {@inheritDoc}
	 * <p>This implementation does never reuse the address of a freed memory block.</p>
	 */
	@Override
	public long allocate(int length) {
		byte[] data = new byte[length];
		long address = nextAddress++;
		
		blocks.put(address, data);
		
		return address;
	}

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException if the address has not been allocated
	 */
	@Override
	public byte[] read(long address) {
		byte[] block = blocks.get(address);
		if (block == null) {
			throw new IllegalArgumentException("Block not found: " + address);
		}
		
		byte[] result = new byte[block.length];
		System.arraycopy(block, 0, result, 0, block.length);
		
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException if the address has not been allocated
	 */
	@Override
	public void write(long address, byte[] data) {
		byte[] block = blocks.get(address);
		if (block == null) {
			throw new IllegalArgumentException("Block not found: " + address);
		}

		if (block.length < data.length) {
			throw new IllegalArgumentException("data.length " + data.length + " < block.length " + block.length);
		}
		
		System.arraycopy(data, 0, block, 0, data.length);
	}
	
	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException if the address has not been allocated
	 */
	@Override
	public void free(long address) {
		if (!blocks.containsKey(address)) {
			throw new IllegalArgumentException("Block not found: " + address);
		}

		blocks.remove(Long.valueOf(address));
	}
	
	@Override
	public void reset() {
		blocks.clear();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{blocks=" + blocks.size() + "}";
	}
}
