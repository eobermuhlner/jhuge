package ch.obermuhlner.jhuge.memory;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests {@link DummyMemoryManager}.
 */
@SuppressWarnings("javadoc")
public class DummyMemoryManagerTest extends AbstractMemoryManagerTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new DummyMemoryManager();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRead_not_found() {
		DummyMemoryManager memoryManager = new DummyMemoryManager();
		memoryManager.read(99);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrite_not_found() {
		DummyMemoryManager memoryManager = new DummyMemoryManager();
		memoryManager.write(99, new byte[] { });
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrite_too_long() {
		DummyMemoryManager memoryManager = new DummyMemoryManager();
		long address = memoryManager.allocate(10);
		byte[] data = memoryManager.read(address);
		memoryManager.write(address, new byte[data.length * 2]);
	}
	
	@Test
	public void testFree_twice() {
		DummyMemoryManager memoryManager = new DummyMemoryManager();
		long address = memoryManager.allocate(10);
		memoryManager.free(address);
		
		try {
			memoryManager.free(address);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException exception) {
			// expected
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFree_not_found() {
		DummyMemoryManager memoryManager = new DummyMemoryManager();
		memoryManager.free(99);
	}
}
