package ch.obermuhlner.jhuge.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class to test {@link List}.
 */
public class ArrayListTest extends AbstractListTest {

	@Override
	protected <T> List<T> createList(T... initial) {
		return new ArrayList<T>(Arrays.asList(initial));
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
