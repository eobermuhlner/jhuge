package ch.obermuhlner.jhuge.collection.internal;

/**
 * Abstract base class to simplify implementing a {@link IntArray}.
 */
public abstract class AbstractIntArray implements IntArray {

	@Override
	public int indexOf(int element) {
		final int n = size();
		for (int i = 0; i < n; i++) {
			if (element == get(i)) {
				return i;
			}
		}
		
		return -1;
	}
}