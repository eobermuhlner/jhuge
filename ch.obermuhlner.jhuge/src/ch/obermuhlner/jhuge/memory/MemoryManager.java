package ch.obermuhlner.jhuge.memory;

/**
 * Manages memory blocks to read and write arbitrary content.
 * 
 * <p>The memory blocks are identified by a long address.
 * The address might (or might not) represent the physical location of the memory block.</p>
 * <p>You <em>cannot</em> make any assumption about addresses.</p>
 * <p>The behavior of accessing addresses that where nor allocated is unspecified.</p>
 */
public interface MemoryManager {

	/**
	 * Allocates a memory block of the specified length.
	 * 
	 * @param length the length of the memory block to allocate
	 * @return the address of the allocated memory block
	 */
	long allocate(int length);

	/**
	 * Allocates a memory block and fills it with the specified data.
	 * 
	 * <p>This has the same behavior as calling</p>
	 * <pre>
	 * long address = memoryManager.allocate(data.length);
	 * memoryManager.write(address, data);
	 * </pre>
	 * 
	 * @param data the data to write into the allocated memory block 
	 * @return the address of the allocated memory block containing the specified data
	 */
	long allocate(byte[] data);

	/**
	 * Returns the content of the memory block at the specified address.
	 * 
	 * <p>The returned byte array is a <em>copy</em> of the memory block.
	 * Modifying the byte array will <em>not</em> modify the memory block.
	 * Use {@link #write(long, byte[])} to do this.</p>
	 * 
	 * @param address the address of the memory block to read
	 * @return the content of the memory block
	 */
	byte[] read(long address);
	
	/**
	 * Writes the content of the specified byte array into the block at the specified address.
	 * 
	 * <p>The byte array may <em>not</em> be longer than the length of the allocated memory block.</p>
	 * 
	 * @param address the address of the memory block to write
	 * @param data the content to write into the memory block
	 * @throws IllegalArgumentException if the byte array is longer than the allocated memory block
	 */
	void write(long address, byte[] data);
	
	/**
	 * Frees the memory block at the specified address.
	 * 
	 * <p>The freed address may or may not be reused in later allocations.</p>
	 * 
	 * @param address the address of the memory block to free
	 */
	void free(long address);
	
	/**
	 * Resets the memory manager.
	 * 
	 * <p>This frees all allocated memory blocks.</p>
	 * <p>If possible any resources allocated by the memory manager in order to fulfill the allocation requests are freed.</p>
	 */
	void reset();
}
