package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Tests {@link HugeHashMap} with a {@link MemoryMappedFileManager}.
 */
public class MemoryMappedFile_HugeHashMapTest extends AbstractHugeHashMapTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager(150);
	}

}
