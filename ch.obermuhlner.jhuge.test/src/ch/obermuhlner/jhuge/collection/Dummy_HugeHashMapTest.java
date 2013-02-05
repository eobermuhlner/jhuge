package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.DummyMemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * Tests {@link HugeHashMap} with a {@link DummyMemoryManager}.
 */
public class Dummy_HugeHashMapTest extends AbstractHugeHashMapTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new DummyMemoryManager();
	}

	@Override
	protected boolean isFaster() {
		return false;
	}
}
