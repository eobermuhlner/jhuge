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
	
	@Override
	public void addAscending(int value) {
		// TODO binary search
		final int n = size();
		for (int i = 0; i < n; i++) {
			if (get(i) >= value) {
				add(i, value);
				return;
			}
		}
		add(value);
	}
	
	@Override
	public int[] toArray() {
		final int n = size();
		int[] result = new int[n];
		for (int i = 0; i < n; i++) {
			result[i] = get(i);
		}
		return result;
	}
}
