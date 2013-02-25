package ch.obermuhlner.jhuge.collection.internal;

/**
 * Abstract base class to simplify implementing a {@link LongArray}.
 */
public abstract class AbstractLongArray implements LongArray {

	@Override
	public int indexOf(long value) {
		final int n = size();
		for (int i = 0; i < n; i++) {
			if (value == get(i)) {
				return i;
			}
		}
		
		return -1;
	}
	
	@Override
	public void addAscending(long value) {
		int minIndex = 0;
		int maxIndex = size() - 1;
		
		while (minIndex < maxIndex) {
			int midIndex = minIndex + (maxIndex - minIndex) / 2;
			if (get(midIndex) < value) {
				minIndex = midIndex + 1;
			} else {
				maxIndex = midIndex;
			}
		}
		
		if (maxIndex == minIndex && get(minIndex) >= value) {
			add(minIndex, value);
		} else {
			add(value);
		}
	}
	
	@Override
	public long[] toArray() {
		final int n = size();
		long[] result = new long[n];
		for (int i = 0; i < n; i++) {
			result[i] = get(i);
		}
		return result;
	}
}
