package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Tests {@link ImmutableHugeHashMap} with a {@link MemoryMappedFileManager}.
 */
public class MemoryMappedFile_ImmutableHugeHashMapTest extends AbstractImmutableHugeHashMapTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new MemoryMappedFileManager(150);
	}

}
