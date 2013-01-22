package ch.obermuhlner.jhuge.collection.internal;

import ch.obermuhlner.jhuge.memory.DummyMemoryManager;

/**
 * Tests {@link HugeLongArray}.
 */
public class HugeLongArrayTest extends AbstractLongArrayTest {

	@Override
	protected LongArray createLongArray(int capacity) {
		return new HugeLongArray(new DummyMemoryManager());
	}

}
