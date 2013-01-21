package ch.obermuhlner.jhuge.collection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests {@link HashSet}.
 */
public class HashSetTest extends AbstractSetTest {

	@Override
	protected <T> Set<T> createSet(T... initial) {
		return new HashSet<T>(Arrays.asList(initial));
	}

	@Override
	protected boolean supportsMutable() {
		return true;
	}
	
	@Override
	protected boolean supportsNullValues() {
		return true;
	}
}
