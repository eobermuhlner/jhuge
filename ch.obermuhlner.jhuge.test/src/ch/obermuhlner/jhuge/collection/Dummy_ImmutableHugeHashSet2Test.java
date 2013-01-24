package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.DummyMemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * Tests {@link ImmutableHugeHashSet2} with a {@link DummyMemoryManager}.
 */
public class Dummy_ImmutableHugeHashSet2Test extends AbstractImmutableHugeHashSet2Test {

	@Override
	protected MemoryManager createMemoryManager() {
		return new DummyMemoryManager();
	}

	@Override
	protected boolean isFaster() {
		return false;
	}
}
