package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Tests {@link HugeHashSet} with a {@link MemoryMappedFileManager}.
 */
public class MemoryMappedFile_HugeHashSetTest extends AbstractHugeHashSetTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager(20000);
	}

	
	@Override
	protected boolean isFaster() {
		return false;
	}
}
