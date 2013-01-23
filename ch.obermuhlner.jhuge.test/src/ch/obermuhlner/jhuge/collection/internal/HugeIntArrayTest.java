package ch.obermuhlner.jhuge.collection.internal;

import ch.obermuhlner.jhuge.memory.DummyMemoryManager;

/**
 * Tests {@link HugeIntArray}.
 */
public class HugeIntArrayTest extends AbstractIntArrayTest {

	@Override
	protected IntArray createIntArray(int capacity) {
		return new HugeIntArray(new DummyMemoryManager());
	}

}
