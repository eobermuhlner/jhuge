package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Tests {@link HugeArrayList} with a {@link MemoryMappedFileManager}.
 */
public class MemoryMappedFile_HugeArrayListTest extends AbstractHugeArrayListTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager(20000);
	}

	@Override
	protected boolean isFaster() {
		return false;
	}
}
