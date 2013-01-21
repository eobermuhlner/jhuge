package ch.obermuhlner.jhuge.memory;

/**
 * Abstract base class to simplify the implementation of a {@link MemoryManager}.
 */
public abstract class AbstractMemoryManager implements MemoryManager {

	@Override
	public long allocate(byte[] data) {
		long address = allocate(data.length);
		write(address, data);
		return address;
	}
}
