package ch.obermuhlner.jhuge.memory;

import org.junit.Test;

/**
 * Tests {@link MemoryMappedFileManager}.
 */
@SuppressWarnings("javadoc")
public class MemoryMappedFileManagerTest extends AbstractMemoryManagerTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager();
	}
	
	@Test
	public void testBufferSize() {
		MemoryMappedFileManager memoryManager = new MemoryMappedFileManager(200);
		
		System.out.println("init      " + memoryManager.getFreeBlockSizes());
		
		long address1 = memoryManager.allocate(100);
		System.out.println("alloc 100 " + memoryManager.getFreeBlockSizes());
		
		long address2 = memoryManager.allocate(96);
		System.out.println("alloc  96 " + memoryManager.getFreeBlockSizes());
		
		long address3 = memoryManager.allocate(30);
		System.out.println("alloc  30 " + memoryManager.getFreeBlockSizes());
		
		long address4 = memoryManager.allocate(30);
		System.out.println("alloc  30 " + memoryManager.getFreeBlockSizes());
		
		memoryManager.compact();
		System.out.println("compact  " + memoryManager.getFreeBlockSizes());
		
		memoryManager.compact();
		System.out.println("compact  " + memoryManager.getFreeBlockSizes());
		
		memoryManager.free(address1);
		memoryManager.free(address2);
		memoryManager.free(address3);
		memoryManager.free(address4);
		System.out.println("free  all " + memoryManager.getFreeBlockSizes());
		
		memoryManager.compact();
		System.out.println("compact  " + memoryManager.getFreeBlockSizes());
		
		memoryManager.reset();
		System.out.println("reset    " + memoryManager.getFreeBlockSizes());
		System.out.println();
		
		// --------------------------
		
		long address5 = memoryManager.allocate(5);
		System.out.println("alloc   5 " + memoryManager.getFreeBlockSizes());

		long address6 = memoryManager.allocate(10);
		System.out.println("alloc  10 " + memoryManager.getFreeBlockSizes());

		long address7 = memoryManager.allocate(15);
		System.out.println("alloc  15 " + memoryManager.getFreeBlockSizes());

		memoryManager.free(address6);
		System.out.println("free   10 " + memoryManager.getFreeBlockSizes());

		memoryManager.compact();
		System.out.println("compact  " + memoryManager.getFreeBlockSizes());

		memoryManager.free(address5);
		memoryManager.free(address7);
		System.out.println("free  all " + memoryManager.getFreeBlockSizes());

		memoryManager.compact();
		System.out.println("compact  " + memoryManager.getFreeBlockSizes());

		memoryManager.reset();
		System.out.println("reset    " + memoryManager.getFreeBlockSizes());
		System.out.println();
		
		// --------------------------

	}

}
