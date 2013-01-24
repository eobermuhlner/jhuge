package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.DummyMemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * Tests {@link ImmutableHugeHashMap} with a {@link DummyMemoryManager}.
 */
public class Dummy_ImmutableHugeHashMapTest extends AbstractImmutableHugeHashMapTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new DummyMemoryManager();
	}

}
