package ch.obermuhlner.jhuge.memory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.jhuge.collection.internal.IntArray;
import ch.obermuhlner.jhuge.collection.internal.LongArray;
import ch.obermuhlner.jhuge.collection.internal.PrimitiveIntArray;
import ch.obermuhlner.jhuge.collection.internal.PrimitiveLongArray;

/**
 * Uses {@link ByteBuffer#allocateDirect(int) direct mapped buffers} to store the managed memory blocks outside of the Java heap.
 */
public class MemoryMappedFileManager extends AbstractMemoryManager {

	/**
	 * The number of bytes in a kilobyte.
	 */
	public static final int KILOBYTES = 1024;

	/**
	 * The number of bytes in a megabyte.
	 */
	public static final int MEGABYTES = 1024 * KILOBYTES;
	
	/**
	 * The number of bytes in a gigabyte.
	 */
	public static final int GIGABYTES = 1024 * MEGABYTES;

	/**
	 * Constant to specify that allocated blocks will always have exactly the specified length
	 * and not be quantified to a block size. 
	 */
	public static final int NO_BLOCK_SIZE = -1;
	
	/**
	 * Used to specify that an address is invalid.
	 */
	private static final long NO_ADDRESS = -1;

	private static final boolean DEBUG = false;
	private static final boolean DEBUG_TRACKING = false;
	private static final boolean DEBUG_TRACKING_DETAILS = false;
	
	/**
	 * Fake block that can be returned when a block of 0 bytes is allocated.
	 */
	private static byte[] EMPTY_BLOCK_DATA = new byte[4]; // size is initialized to 0
	
	/**
	 * The list of memory mapped {@link ByteBuffer}s.
	 */
	private final List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
	
	private final int bufferSize;
	private final int blockSize;
	private final int allowedBlockOversize;
	private final boolean compactAfterFree;
	
	/**
	 * The list of free blocks.
	 * 
	 * This list must be tracked in order for {@link #findFreeBlock2(int)} to find a free block.
	 */
	private final LongArray freeBlocksList = new PrimitiveLongArray();
	
	/**
	 * The list of allocated blocks, or <code>null</code>.
	 * 
	 * This list must not be tracked, since we assume that the client code remembers which addresses he has allocated.
	 * In {@link #DEBUG} mode this is tracked so that additional information can be verified and printed.
	 */
	private final LongArray allocatedBlocksList = DEBUG ? new PrimitiveLongArray() : null;
	
	/**
	 * The address of the fake {@link #EMPTY_BLOCK_DATA}.
	 * 
	 * The first time a block of 0 bytes is allocated, the address is stored here.
	 * This empty block will never be freed again.
	 * During {@link #reset()} this is reset to {@link #NO_ADDRESS}.
	 */
	private long emptyBlockAddress = NO_ADDRESS;

	private long usedBytes;
	private long freeBytes;
	private long totalBytes;
	private int allocatedBlocks;
	
	/**
	 * Constructs a {@link MemoryMappedFileManager} with a default buffer size of 100 megabytes and no block quantification.
	 */
	public MemoryMappedFileManager() {
		this(100 * MEGABYTES, NO_BLOCK_SIZE);
	}
	
	/**
	 * Constructs a {@link MemoryMappedFileManager} with the specified buffer size and no block quantification.
	 * 
	 * @param bufferSize the buffer size 
	 */
	public MemoryMappedFileManager(int bufferSize) {
		this(bufferSize, NO_BLOCK_SIZE);
	}
	
	/**
	 * Constructs a {@link MemoryMappedFileManager} with the specified buffer and block size.
	 * 
	 * @param bufferSize the buffer size 
	 * @param blockSize the block size, or {@link #NO_BLOCK_SIZE} to use no block quantification
	 */
	public MemoryMappedFileManager(int bufferSize, int blockSize) {
		this(bufferSize, blockSize, 32, false);
	}

	/**
	 * Constructs a {@link MemoryMappedFileManager} with the specified configuration.
	 * 
	 * @param bufferSize the buffer size 
	 * @param blockSize the block size, or {@link #NO_BLOCK_SIZE} to use no block quantification
	 * @param allowedBlockOversize the allowed block oversize when searching for a fitting free block
	 * @param compactAfterFree compact after freeing a block
	 */
	public MemoryMappedFileManager(int bufferSize, int blockSize, int allowedBlockOversize, boolean compactAfterFree) {
		this.bufferSize = bufferSize;
		this.blockSize = blockSize;
		this.allowedBlockOversize = allowedBlockOversize;
		this.compactAfterFree = compactAfterFree;
	}
	
	/**
	 * Returns the buffer size.
	 * 
	 * @return the buffer size
	 */
	public int getBufferSize() {
		return bufferSize;
	}
	
	/**
	 * Returns the block size.
	 * 
	 * @return the block size, or {@link #NO_BLOCK_SIZE}
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException if the specified length is greater than the buffer size
	 */
	@Override
	public long allocate(int length) {
		if (length == 0) {
			if (emptyBlockAddress == NO_ADDRESS) {
				emptyBlockAddress = findFreeBlock(0);
				allocatedBlocks++;
			}
			if (DEBUG_TRACKING) printTrackingInfo("allocate", length);
			return emptyBlockAddress;
		}
		
		long address = findFreeBlock(quantify(length));
		if (DEBUG) allocatedBlocksList.add(address);
		allocatedBlocks++;
		
		if (DEBUG_TRACKING) printTrackingInfo("allocate", length);
		return address;
	}

	@Override
	public byte[] read(long address) {
		ByteBuffer byteBuffer = readByteBufferInternal(address);
		int length = byteBuffer.getInt();
		if (DEBUG) checkBlockLength(address, length);
		
		byte[] data = new byte[length];
		byteBuffer.get(data);
		
		return data;
	}
	
	private ByteBuffer readByteBufferInternal(long address) {
		if (address == emptyBlockAddress) {
			return ByteBuffer.wrap(EMPTY_BLOCK_DATA);
		}
		
		int bufferIndex = (int) (address / bufferSize);
		int bufferPos = (int) (address % bufferSize);
		
		ByteBuffer buffer = buffers.get(bufferIndex);
		buffer.position(bufferPos);
		return buffer;
	}

	@Override
	public void write(long address, byte[] data) {
		ByteBuffer buffer = writeByteBufferInternal(address);
		int length = buffer.getInt();
		if (DEBUG) checkBlockLength(address, length);
		
		if (length < data.length) {
			throw new IllegalArgumentException("data.length " + data.length + " > block.length " + length);
		}
		
		buffer.put(data);
	}
	
	private ByteBuffer writeByteBufferInternal(long address) {
		if (address == emptyBlockAddress) {
			return ByteBuffer.wrap(EMPTY_BLOCK_DATA);
		}

		int bufferIndex = (int) (address / bufferSize);
		int bufferPos = (int) (address % bufferSize);
		
		ByteBuffer buffer = buffers.get(bufferIndex);
		buffer.position(bufferPos);
		
		return buffer;
	}

	@Override
	public void free(long address) {
		if (address == emptyBlockAddress) {
			return;
		}
		
		int length = getLength(address);
		if (DEBUG) checkBlockLength(address, length);
		
		if (DEBUG) {
			int index = allocatedBlocksList.indexOf(address);
			if (index < 0) {
				throw new IllegalArgumentException("Address was never allocated:" + address);
			}
			allocatedBlocksList.remove(index);
		}
		allocatedBlocks--;
		freeBytes += length;
		usedBytes -= length;
		freeBlocksList.addAscending(address);
		
		if (compactAfterFree) {
			compact();
		}
		if (DEBUG_TRACKING) printTrackingInfo("free", length);
	}
	
	/**
	 * Compacts the free memory blocks.
	 * 
	 * <p>This might lead to larger free memory blocks.</p>
	 */
	public void compact() {
		int n = freeBlocksList.size();
		for (int i = n-1; i > 0; i--) {
			long leftAddress = freeBlocksList.get(i - 1);
			long rightAddress = freeBlocksList.get(i);
			
			if (isSameBuffer(leftAddress, rightAddress)) {
				int leftLength = getLengthOfFreeBlock(leftAddress);
				int rightLength = getLengthOfFreeBlock(rightAddress);

				long calulatedAddressAfterLeft = leftAddress + 4 + leftLength;
				if (calulatedAddressAfterLeft == rightAddress) {
					int combinedLength = leftLength + 4 + rightLength;
					setLength(leftAddress, combinedLength);
					freeBlocksList.remove(i);
					freeBytes += 4;
				} else {
					if (calulatedAddressAfterLeft > rightAddress) {
						throw new IllegalStateException("left " + leftAddress + "," + leftLength + " overlaps " + rightAddress + "," + rightLength);
					}
				}
			}
		}
	}

	@Override
	public void reset() {
		usedBytes = 0;
		freeBytes = 0;
		allocatedBlocks = 0;
		totalBytes = 0;

		freeBlocksList.clear();
		emptyBlockAddress = NO_ADDRESS;
		
		for (int i = 0; i < buffers.size(); i++) {
			initFreeBuffer(i, buffers.get(i));
		}
	}
	
	/**
	 * Returns the number of used bytes.
	 * 
	 * @return the used bytes
	 */
	public long getUsedBytes() {
		return usedBytes;
	}
	
	/**
	 * Returns the number of free bytes.
	 * @return the free bytes
	 * 
	 */
	public long getFreeBytes() {
		return freeBytes;
	}
	
	/**
	 * Returns the number of total bytes.
	 * 
	 * @return the total bytes
	 */
	public long getTotalBytes() {
		return totalBytes;
	}
	
	/**
	 * Returns the number of allocated blocks.
	 * 
	 * @return the number of allocated blocks
	 */
	public int getAllocatedBlocks() {
		return allocatedBlocks;
	}

	/**
	 * Returns the number of free blocks.
	 * 
	 * @return the number of free blocks
	 */
	public int getFreeBlocks() {
		return freeBlocksList.size();
	}

	private void printTrackingInfo(String method, int length) {
		long overheadBytes = totalBytes-usedBytes-freeBytes;
		
		if (DEBUG_TRACKING) {
			System.out.printf("Memory %-10s %6d : used=%10d free=%10d total=%10d overhead=%10d allocated blocks=%5d free blocks=%5d\n", method, length, usedBytes, freeBytes, totalBytes, overheadBytes, allocatedBlocks, freeBlocksList.size());
		}

		if (DEBUG_TRACKING_DETAILS) {
			long totalFreeBytes = 0;
			System.out.println("Free blocks:");
			for (int i = 0; i < freeBlocksList.size(); i++) {
				long address = freeBlocksList.get(i);
				int blockLength = getLengthOfFreeBlock(address);
				System.out.printf("   free block[%d] address=%10d length=%6d\n", i, address, blockLength);
				totalFreeBytes += blockLength;
			}
			System.out.println("Total free bytes: " + totalFreeBytes);

			if (DEBUG) {
				long totalAllocatedBytes = 0;
				System.out.println("Allocated blocks:");
				for (int i = 0; i < allocatedBlocksList.size(); i++) {
					long address = allocatedBlocksList.get(i);
					int blockLength = getLength(address);
					System.out.printf("  alloc block[%d] address=%10d length=%6d\n", i, address, blockLength);
					totalAllocatedBytes += blockLength;
				}
				System.out.println("Total alloc bytes: " + totalAllocatedBytes);
			}
		}

		if (DEBUG) {
			int expectedOverheadBytes = (allocatedBlocks + freeBlocksList.size()) * 4;
			if (expectedOverheadBytes != overheadBytes) {
				throw new RuntimeException("expected overhead=" + expectedOverheadBytes + " actualOverhead=" + overheadBytes);
			}
		}
	}
	
	/**
	* DirectByteBuffers are garbage collected by using a phantom reference and a
	* reference queue. Every once a while, the JVM checks the reference queue and
	* cleans the DirectByteBuffers. However, as this doesn't happen
	* immediately after discarding all references to a DirectByteBuffer, it's
	* easy to OutOfMemoryError yourself using DirectByteBuffers. This function
	* explicitly calls the Cleaner method of a DirectByteBuffer.
	* 
	* http://stackoverflow.com/questions/1854398/how-to-garbage-collect-a-direct-buffer-java
	*  
	* @param toBeDestroyed
	*          The DirectByteBuffer that will be "cleaned". Utilizes reflection.
	*/
//	@SuppressWarnings("unused")
//	private static void destroyDirectByteBuffer(ByteBuffer toBeDestroyed) {
//		try {
//			Method cleanerMethod = toBeDestroyed.getClass().getMethod("cleaner");
//			cleanerMethod.setAccessible(true);
//			Object cleaner = cleanerMethod.invoke(toBeDestroyed);
//			Method cleanMethod = cleaner.getClass().getMethod("clean");
//			cleanMethod.setAccessible(true);
//			cleanMethod.invoke(cleaner);
//		} catch(InvocationTargetException exception) {
//			throw new IllegalStateException("Failed to destroy direct buffer", exception);
//		} catch (NoSuchMethodException exception) {
//			throw new IllegalStateException("Failed to destroy direct buffer", exception);
//		} catch (SecurityException exception) {
//			throw new IllegalStateException("Failed to destroy direct buffer", exception);
//		} catch (IllegalAccessException exception) {
//			throw new IllegalStateException("Failed to destroy direct buffer", exception);
//		} catch (IllegalArgumentException exception) {
//			throw new IllegalStateException("Failed to destroy direct buffer", exception);
//		}
//	}

	private void checkBlockLength(long address, int length) {
		if (DEBUG) {
			if (!isSameBuffer(address, address + length)) {
				throw new IllegalStateException("end of block is in the next buffer: address=" + address + " length=" + length + " bufferSize=" + bufferSize);
			}
		}
	}
	
	private boolean isSameBuffer(long address1, long address2) {
		int address1BufferIndex = (int) (address1 / bufferSize);
		int address2BufferIndex = (int) (address2 / bufferSize);
		return address1BufferIndex == address2BufferIndex;
	}

	/**
	 * Returns the sizes of the free memory blocks.
	 * 
	 * @return a snapshot of the sizes of the the free memory blocks in ascending order (smallest blocks first)
	 */
	public int[] getFreeBlockSizes() {
		int n = freeBlocksList.size();
		
		IntArray result = new PrimitiveIntArray(n);
		for (int i = 0; i < n; i++) {
			result.addAscending(getLengthOfFreeBlock(freeBlocksList.get(i)));
		}	
		
		return result.toArray();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{buffers=" + buffers.size() + ", bufferSize=" + bufferSize + ", freeblocks=" + freeBlocksList.size() + "}";
	}
	
	private int getLengthOfFreeBlock(long address) {
		return getLength(address);
	}
	
	private int getLength(long address) {
		int bufferIndex = (int) (address / bufferSize);
		int bufferPos = (int) (address % bufferSize);
		
		ByteBuffer buffer = buffers.get(bufferIndex);
		buffer.position(bufferPos);
		int length = buffer.getInt();
		if (DEBUG) checkBlockLength(address, length);
		
		return length;
	}
	
	private void setLength(long address, int length) {
		if (DEBUG) checkBlockLength(address, length);

		int bufferIndex = (int) (address / bufferSize);
		int bufferPos = (int) (address % bufferSize);
		
		ByteBuffer buffer = buffers.get(bufferIndex);
		buffer.position(bufferPos);
		buffer.putInt(length);
	}

	private int quantify(int length) {
		if (blockSize == NO_BLOCK_SIZE) {
			return length;
		}
		
		return quantify(length, blockSize);
	}
	
	static int quantify(int size, int blockSize) {
		return size % blockSize == 0 ? size : (size / blockSize + 1) * blockSize;
	}
	
	private long findFreeBlock(int length) {
		if (length > bufferSize) {
			throw new IllegalArgumentException("block.length=" + length + " > " + "buffer.length=" + bufferSize);
		}

		long freeBlock = findFreeBlock2(length);

		if (freeBlock == NO_ADDRESS) {
			compact();
			freeBlock = findFreeBlock2(length);
		}
		
		if (freeBlock == NO_ADDRESS) {
			addMemoryMappedFile();
			freeBlock = findFreeBlock2(length);
		}

		return freeBlock;
	}

	private long findFreeBlock2(int length) {
		int bestBlockIndex = -1;
		long bestBlockAddress = NO_ADDRESS;
		int bestBlockLength = -1;
		final int n = freeBlocksList.size();
		for (int i = 0; i < n; i++) {
			long blockAddress = freeBlocksList.get(i);
			int blockLength = getLengthOfFreeBlock(blockAddress);
			if (blockLength >= length) {
				if (blockLength - length <= allowedBlockOversize) {
					bestBlockIndex = i;
					bestBlockAddress = blockAddress;
					bestBlockLength = blockLength;
					break;
				}
				if (blockLength > bestBlockLength) {
					bestBlockIndex = i;
					bestBlockAddress = blockAddress;
					bestBlockLength = blockLength;
				}
			}
		}

		if (bestBlockIndex < 0) {
			return NO_ADDRESS;
		}
		
		freeBlocksList.remove(bestBlockIndex);
		freeBytes -= bestBlockLength;
		usedBytes += bestBlockLength;
		
		if (bestBlockLength > length + Math.max(4, allowedBlockOversize)) {
			long remainingBlockAddress = bestBlockAddress + length + 4;
			int remainingBlockLength = bestBlockLength - length - 4;
			setLength(remainingBlockAddress, remainingBlockLength);
			addFreeBlock(remainingBlockAddress, remainingBlockLength);

			bestBlockLength = length;
			setLength(bestBlockAddress, bestBlockLength);
			usedBytes -= 4;
		}

		return bestBlockAddress;
	}

	private void addMemoryMappedFile() {
			ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
			assert buffer.capacity() == bufferSize;
			initFreeBuffer(buffers.size(), buffer);
			buffers.add(buffer);
	}
	
	private void initFreeBuffer(int bufferIndex, ByteBuffer buffer) {
			int bufferLength = bufferSize - 4;
			buffer.clear();
			buffer.putInt(bufferLength);

			totalBytes += bufferSize;
			usedBytes += bufferLength; // corrected in addFreeBlock()
			
			long blockAddress = bufferIndex * bufferSize;
			addFreeBlock(blockAddress, bufferLength);
	}

	private void addFreeBlock(long address, int length) {
		if (DEBUG) checkBlockLength(address, length);
		freeBlocksList.addAscending(address);
		
		freeBytes += length;
		usedBytes -= length;
	}
}
