package ch.obermuhlner.jhuge.memory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Abstract base class to test {@link MemoryManager}.
 */
public abstract class AbstractMemoryManagerTest {

	/**
	 * Creates the {@link MemoryManager} to test.
	 * 
	 * @return the created {@link MemoryManager}
	 */
	protected abstract MemoryManager createMemoryManager();
	
	/**
	 * Tests the basics.
	 */
	@Test
	public void testBasics() {
		MemoryManager memoryManager = createMemoryManager();
		
		long block1 = memoryManager.allocate(100);

		byte[] data1 = memoryManager.read(block1);
		assertEquals(true, data1.length >= 100);
		assertEquals(0, data1[0]);
		
		data1[0] = 1; // modify in byte[], not in managed memory
		
		byte[] data2 = memoryManager.read(block1);
		assertEquals(0, data2[0]); // not modified
		
		memoryManager.write(block1, data1);
		
		byte[] data3 = memoryManager.read(block1);
		assertEquals(1, data3[0]); // modified
		
		memoryManager.free(block1);
	}
	
}
