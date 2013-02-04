package ch.obermuhlner.jhuge.memory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.internal.LongIntMap;

/**
 * Uses {@link ByteBuffer#allocateDirect(int) direct mapped buffers} to store the managed memory blocks outside of the Java heap.
 * 
 * <p>If </p>
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
	 * The constant to specify that allocated blocks will always have exactly the specified length
	 * and not be quantified to a block size. 
	 */
	public static final int NO_BLOCK_SIZE = -1;
	
	private static final boolean DEBUG_MODE = true;
	
	private static byte[] EMPTY_BLOCK_DATA = new byte[4]; // size is set to 0
	
	private final List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
	private final int bufferSize;
	private final int blockSize;
	
	private final LongIntMap freeBlocks = new LongIntMap();
	
	private long emptyBlockAddress = -1;

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
		this.bufferSize = bufferSize;
		this.blockSize = blockSize;
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
			if (emptyBlockAddress == -1) {
				emptyBlockAddress = findFreeBlock(0);
			}
			return emptyBlockAddress;
		}
		
		return findFreeBlock(quantify(length));
	}

	@Override
	public byte[] read(long address) {
		ByteBuffer byteBuffer = readByteBufferInternal(address);
		int length = byteBuffer.getInt();
		checkBlockLength(address, length);
		
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
		checkBlockLength(address, length);
		
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
		checkBlockLength(address, length);
		
		freeBlocks.put(address, length);
	}
	
	/**
	 * Compacts the free memory blocks.
	 * 
	 * <p>This might lead to larger free memory blocks.</p>
	 */
	public void compact() {
		List<Long> addresses = new ArrayList<Long>(freeBlocks.keySet());
		Collections.sort(addresses);
		System.out.println("sorted addresses: " + addresses);
		for (int i = addresses.size()-1; i > 0; i--) {
			long leftAddress = addresses.get(i - 1);
			long rightAddress = addresses.get(i);
			int leftLength = freeBlocks.get(leftAddress);
			int rightLength = freeBlocks.get(rightAddress);
			System.out.println("adjacent?: left=" + leftAddress + "," + leftLength + " right=" + rightAddress + "," + rightLength);
			
			if (isSameBuffer(leftAddress, rightAddress)) {
				long calulatedAddressAfterLeft = leftAddress + 4 + leftLength;
				if (calulatedAddressAfterLeft == rightAddress) {
					int combinedLength = 4 + leftLength + rightLength;
					setLength(leftAddress, combinedLength);
					System.out.println(" combined: left=" + leftAddress + "," + combinedLength);
					freeBlocks.remove(rightAddress);
					System.out.println(" dropped: right=" + rightAddress + "," + rightLength);
				} else {
					if (DEBUG_MODE && calulatedAddressAfterLeft > rightAddress) {
						throw new IllegalStateException("left " + leftAddress + "," + leftLength + " overlaps " + rightAddress + "," + rightLength);
					}
					System.out.println("  not adjacent! " + calulatedAddressAfterLeft + " != " + rightAddress + " (" + (rightAddress - calulatedAddressAfterLeft) + " missing)");
				}
			} else {
				System.out.println("  not same buffer!");
			}
		}
	}
	
	@Override
	public void reset() {
		freeBlocks.clear();
		emptyBlockAddress = -1;
		for (int i = 0; i < buffers.size(); i++) {
			//destroyDirectByteBuffer(buffer);
			initFreeBuffer(i, buffers.get(i));
		}
		//buffers.clear();
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
	@SuppressWarnings("unused")
	private static void destroyDirectByteBuffer(ByteBuffer toBeDestroyed) {
		try {
			Method cleanerMethod = toBeDestroyed.getClass().getMethod("cleaner");
			cleanerMethod.setAccessible(true);
			Object cleaner = cleanerMethod.invoke(toBeDestroyed);
			Method cleanMethod = cleaner.getClass().getMethod("clean");
			cleanMethod.setAccessible(true);
			cleanMethod.invoke(cleaner);
		} catch(InvocationTargetException exception) {
			throw new IllegalStateException("Failed to destroy direct buffer", exception);
		} catch (NoSuchMethodException exception) {
			throw new IllegalStateException("Failed to destroy direct buffer", exception);
		} catch (SecurityException exception) {
			throw new IllegalStateException("Failed to destroy direct buffer", exception);
		} catch (IllegalAccessException exception) {
			throw new IllegalStateException("Failed to destroy direct buffer", exception);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("Failed to destroy direct buffer", exception);
		}
	}

	private void checkBlockLength(long address, int length) {
		if (DEBUG_MODE) {
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
	 * @return a snapshot of the sizes of the the free memory blocks
	 */
	public List<Integer> getFreeBlockSizes() {
		ArrayList<Integer> snapshot = new ArrayList<Integer>(freeBlocks.values());
		Collections.sort(snapshot);
		return snapshot;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{buffers=" + buffers.size() + ", bufferSize=" + bufferSize + ", freeblocks=" + freeBlocks.size() + "}";
	}
	
	private int getLength(long address) {
		int bufferIndex = (int) (address / bufferSize);
		int bufferPos = (int) (address % bufferSize);
		
		ByteBuffer buffer = buffers.get(bufferIndex);
		buffer.position(bufferPos);
		int length = buffer.getInt();
		checkBlockLength(address, length);
		
		return length;
	}
	
	private void setLength(long address, int length) {
		checkBlockLength(address, length);

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

		// TODO use compact() as soon as it is well tested
//		if (freeBlock < 0) {
//			compact();
//			freeBlock = findFreeBlock2(length);
//		}
		
		if (freeBlock < 0) {
			addMemoryMappedFile();
			freeBlock = findFreeBlock2(length);
		}

		return freeBlock;
	}

	private long findFreeBlock2(int length) {
		long bestBlockAddress = -1;
		int bestBlockLength = -1;
		Set<Entry<Long, Integer>> entrySet = freeBlocks.entrySet();
		for (Entry<Long, Integer> entry : entrySet) {
			long blockAddress = entry.getKey();
			int blockLength = entry.getValue();
			if (blockLength >= length) {
				bestBlockAddress = blockAddress;
				bestBlockLength = blockLength;
				if (blockLength == length) {
					break;
				}
			}
		}

		if (bestBlockAddress < 0) {
			return -1;
		}
		
		freeBlocks.remove(bestBlockAddress);

		if (bestBlockLength > length + 4) {
			long remainingBlockAddress = bestBlockAddress + length + 4;
			int remainingBlockLength = bestBlockLength - length - 4;
			setLength(remainingBlockAddress, remainingBlockLength);
			addFreeBlock(remainingBlockAddress, remainingBlockLength);

			bestBlockLength = length;
			setLength(bestBlockAddress, bestBlockLength);
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

			long blockAddress = bufferIndex * bufferSize;
			addFreeBlock(blockAddress, bufferLength);
	}

	private void addFreeBlock(long address, int length) {
		checkBlockLength(address, length);
		freeBlocks.put(address, length);
	}
}
