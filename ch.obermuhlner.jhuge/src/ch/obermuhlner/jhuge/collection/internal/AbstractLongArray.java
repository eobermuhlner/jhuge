package ch.obermuhlner.jhuge.collection.internal;

/**
 * Abstract base class to simplify implementing a {@link LongArray}.
 */
public abstract class AbstractLongArray implements LongArray {

	@Override
	public int indexOf(long element) {
		final int n = size();
		for (int i = 0; i < n; i++) {
			if (element == get(i)) {
				return i;
			}
		}
		
		return -1;
	}
}
