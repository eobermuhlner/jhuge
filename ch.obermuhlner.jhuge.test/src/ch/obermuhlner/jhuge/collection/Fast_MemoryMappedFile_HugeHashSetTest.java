package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Tests {@link HugeHashSet} in faster mode with a {@link MemoryMappedFileManager}.
 */
public class Fast_MemoryMappedFile_HugeHashSetTest extends AbstractHugeHashSetTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager(150);
	}

	@Override
	protected boolean isFaster() {
		return true;
	}
}
