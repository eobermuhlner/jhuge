package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Tests {@link ImmutableHugeHashSet} with a {@link MemoryMappedFileManager}.
 */
public class MemoryMappedFile_ImmutableHugeHashSetTest extends AbstractImmutableHugeHashSetTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager(20000);
	}

}
