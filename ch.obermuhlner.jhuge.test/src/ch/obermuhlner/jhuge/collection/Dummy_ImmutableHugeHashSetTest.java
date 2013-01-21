package ch.obermuhlner.jhuge.collection;

import ch.obermuhlner.jhuge.memory.DummyMemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * Tests {@link ImmutableHugeHashSet} with a {@link DummyMemoryManager}.
 */
public class Dummy_ImmutableHugeHashSetTest extends AbstractImmutableHugeHashSetTest {

	@Override
	protected MemoryManager createMemoryManager() {
		return new DummyMemoryManager();
	}

}
