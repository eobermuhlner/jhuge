package ch.obermuhlner.jhuge.collection.internal;

import ch.obermuhlner.jhuge.memory.DummyMemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * Tests {@link HugeIntLongArrayMap}
 */
public class HugeIntLongArrayMapTest extends AbstractIntLongArrayMapTest {

	@Override
	protected IntLongArrayMap createIntLongArrayMap() {
		return new HugeIntLongArrayMap(createMemoryManager(), 1);
	}

	private MemoryManager createMemoryManager() {
		return new DummyMemoryManager();
	}
}
