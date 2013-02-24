package ch.obermuhlner.jhuge.memory;

import static org.junit.Assert.assertEquals;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

import org.junit.Test;
import static ch.obermuhlner.jhuge.converter.AbstractSerializableConverterTest.assertArrayEquals;

/**
 * Tests {@link MemoryMappedFileManager}.
 */
@SuppressWarnings("javadoc")
public class MemoryMappedFileManagerTest extends AbstractMemoryManagerTest {

	private static final boolean DEBUG = false;
	
	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager();
	}
	
	@Test
	public void testSizes() {
		MemoryMappedFileManager memoryManager = new MemoryMappedFileManager(200, -1, 32, false);

		assertEquals(0, memoryManager.getAllocatedBlocks());
		assertEquals(0, memoryManager.getFreeBlocks()); // no buffer created yet - so there are no free blocks
		assertEquals(0, memoryManager.getUsedBytes());
		assertEquals(0, memoryManager.getFreeBytes());
		assertEquals(0, memoryManager.getTotalBytes());
		
		long address1 = memoryManager.allocate(20); // allocate from buffer #1
		assertEquals(20, memoryManager.read(address1).length);
		assertEquals(1, memoryManager.getAllocatedBlocks());
		assertEquals(1, memoryManager.getFreeBlocks()); // one big free block in buffer #1 
		assertEquals(20, memoryManager.getUsedBytes());
		assertEquals(200-4-20-4, memoryManager.getFreeBytes());
		assertEquals(200, memoryManager.getTotalBytes());
		assertArrayEquals(new int[] { 200-4-20-4 }, memoryManager.getFreeBlockSizes());
		
		memoryManager.free(address1);
		assertEquals(0, memoryManager.getAllocatedBlocks());
		assertEquals(2, memoryManager.getFreeBlocks());
		assertEquals(200-4-4, memoryManager.getFreeBytes());
		assertEquals(200, memoryManager.getTotalBytes());
		assertArrayEquals(new int[] { 20, 200-4-20-4 }, memoryManager.getFreeBlockSizes());
		
		memoryManager.compact();
		assertEquals(1, memoryManager.getFreeBlocks());
		assertEquals(200-4, memoryManager.getFreeBytes());
		assertEquals(200, memoryManager.getTotalBytes());
		assertArrayEquals(new int[] { 200-4 }, memoryManager.getFreeBlockSizes());
		
		long address2 = memoryManager.allocate(60); // allocated from buffer #1
		assertEquals(60, memoryManager.read(address2).length);
		assertEquals(1, memoryManager.getAllocatedBlocks());
		assertEquals(1, memoryManager.getFreeBlocks());
		assertEquals(60, memoryManager.getUsedBytes());
		assertEquals(200-4-60-4, memoryManager.getFreeBytes());
		assertEquals(200, memoryManager.getTotalBytes());
		assertArrayEquals(new int[] { 200-4-60-4 }, memoryManager.getFreeBlockSizes());

		long address3 = memoryManager.allocate(150); // need to create buffer #2
		assertEquals(150, memoryManager.read(address3).length);
		assertEquals(2, memoryManager.getAllocatedBlocks());
		assertEquals(2, memoryManager.getFreeBlocks());
		assertEquals(60+150, memoryManager.getUsedBytes());
		assertEquals(200-4-60-4 +200-4-150-4, memoryManager.getFreeBytes());
		assertEquals(200 +200, memoryManager.getTotalBytes());
		assertArrayEquals(new int[] { 200-4-150-4, 200-4-60-4 }, memoryManager.getFreeBlockSizes());
		
		long address4 = memoryManager.allocate(120); // allocated from buffer #1 - with oversize taking the entire free block of buffer #1
		assertEquals((200-4-60-4), memoryManager.read(address4).length);
		assertEquals(3, memoryManager.getAllocatedBlocks());
		assertEquals(1, memoryManager.getFreeBlocks());
		assertEquals(60+150+(200-4-60-4), memoryManager.getUsedBytes());
		assertEquals(0 +200-4-150-4, memoryManager.getFreeBytes());
		assertEquals(200 +200, memoryManager.getTotalBytes());
		assertArrayEquals(new int[] { 200-4-150-4 }, memoryManager.getFreeBlockSizes());
		
		memoryManager.reset();
		assertEquals(0, memoryManager.getAllocatedBlocks());
		assertEquals(2, memoryManager.getFreeBlocks());
		assertEquals(0, memoryManager.getUsedBytes());
		assertEquals(200-4 + 200-4, memoryManager.getFreeBytes());
		assertEquals(200 +200, memoryManager.getTotalBytes());
		assertArrayEquals(new int[] { 200-4, 200-4 }, memoryManager.getFreeBlockSizes());
	}
	
	@Test
	public void testBufferSize() {
		MemoryMappedFileManager memoryManager = new MemoryMappedFileManager(200);
		
		System.out.println("init      " + freeBlockSizes(memoryManager));
		
		long address1 = memoryManager.allocate(100);
		System.out.println("alloc 100 " + freeBlockSizes(memoryManager));
		
		long address2 = memoryManager.allocate(96);
		System.out.println("alloc  96 " + freeBlockSizes(memoryManager));
		
		long address3 = memoryManager.allocate(30);
		System.out.println("alloc  30 " + freeBlockSizes(memoryManager));
		
		long address4 = memoryManager.allocate(30);
		System.out.println("alloc  30 " + freeBlockSizes(memoryManager));
		
		memoryManager.compact();
		System.out.println("compact   " + freeBlockSizes(memoryManager));
		
		memoryManager.free(address1);
		memoryManager.free(address2);
		memoryManager.free(address3);
		memoryManager.free(address4);
		System.out.println("free  all " + freeBlockSizes(memoryManager));
		
		memoryManager.compact();
		System.out.println("compact   " + freeBlockSizes(memoryManager));
		
		memoryManager.reset();
		System.out.println("reset     " + freeBlockSizes(memoryManager));
		System.out.println();
		
		// --------------------------
		
		long address5 = memoryManager.allocate(5);
		System.out.println("alloc   5 " + freeBlockSizes(memoryManager));

		long address6 = memoryManager.allocate(10);
		System.out.println("alloc  10 " + freeBlockSizes(memoryManager));

		long address7 = memoryManager.allocate(15);
		System.out.println("alloc  15 " + freeBlockSizes(memoryManager));

		memoryManager.free(address6);
		System.out.println("free   10 " + freeBlockSizes(memoryManager));

		memoryManager.compact();
		System.out.println("compact   " + freeBlockSizes(memoryManager));

		memoryManager.free(address5);
		memoryManager.free(address7);
		System.out.println("free  all " + freeBlockSizes(memoryManager));

		memoryManager.compact();
		System.out.println("compact   " + freeBlockSizes(memoryManager));

		memoryManager.reset();
		System.out.println("reset     " + freeBlockSizes(memoryManager));
		System.out.println();
		
		// --------------------------

	}

	@Test
	public void testFragmentation() {
		Random random = new Random(1234);
		MemoryMappedFileManager memoryManager = new MemoryMappedFileManager(200);

		final int count = 10000;

		Deque<Long> blocks = new ArrayDeque<Long>();
		for (int i = 0; i < count; i++) {
			String desc = "step #" + i;
			int r = random.nextInt(100);
			
			if (r < 60) {
				int length = random.nextInt(50);
				if (DEBUG) System.out.println(desc + " allocate " + length + " bytes");
				long address = memoryManager.allocate(length);
				blocks.add(address);
			
			} else if (r < 100) {
				if (!blocks.isEmpty()) {
					long address = blocks.removeFirst();
					if (DEBUG) System.out.println(desc + " free " + memoryManager.read(address).length + " bytes");
					memoryManager.free(address);
				}
			}
			
			if (DEBUG) {
				System.out.println("Alloc: " + blocks.size());
				System.out.println("Free : " + freeBlockSizes(memoryManager));
				long overheadBytes = memoryManager.getTotalBytes() - memoryManager.getUsedBytes() - memoryManager.getFreeBytes();
				System.out.printf("Memory used=%10d free=%10d total=%10d overhead=%10d allocated blocks=%5d free blocks=%5d\n", memoryManager.getUsedBytes(), memoryManager.getFreeBytes(), memoryManager.getTotalBytes(), overheadBytes, memoryManager.getAllocatedBlocks(), memoryManager.getFreeBlocks());
				System.out.println();
			}
		}
	}

	private String freeBlockSizes(MemoryMappedFileManager memoryManager) {
		return Arrays.toString(memoryManager.getFreeBlockSizes());
	}
}
