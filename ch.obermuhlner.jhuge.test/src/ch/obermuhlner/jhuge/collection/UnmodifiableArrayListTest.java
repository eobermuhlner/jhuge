package ch.obermuhlner.jhuge.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests {@link ArrayList} made unmodifiable using {@link Collections#unmodifiableList(List)}.
 */
public class UnmodifiableArrayListTest extends AbstractListTest {

	@Override
	protected <T> List<T> createList(T... initial) {
		return Collections.unmodifiableList(new ArrayList<T>(Arrays.asList(initial)));
	}

	@Override
	protected boolean supportsMutable() {
		return false;
	}
	
	@Override
	protected boolean supportsNullValues() {
		return true;
	}
}
