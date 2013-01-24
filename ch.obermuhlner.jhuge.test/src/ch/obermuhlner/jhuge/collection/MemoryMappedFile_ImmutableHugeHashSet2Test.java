package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Tests {@link ImmutableHugeHashSet2} with a {@link MemoryMappedFileManager}.
 */
public class MemoryMappedFile_ImmutableHugeHashSet2Test extends AbstractImmutableHugeHashSet2Test {

	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager(20000);
	}

	@Override
	protected boolean isFaster() {
		return false;
	}
}
