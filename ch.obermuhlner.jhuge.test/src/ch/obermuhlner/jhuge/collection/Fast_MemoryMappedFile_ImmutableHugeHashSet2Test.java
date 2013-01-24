package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Tests {@link ImmutableHugeHashSet2} with a {@link MemoryMappedFileManager}.
 */
public class Fast_MemoryMappedFile_ImmutableHugeHashSet2Test extends AbstractImmutableHugeHashSet2Test {

	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager(150);
	}

	@Override
	protected boolean isFaster() {
		return true;
	}
}
