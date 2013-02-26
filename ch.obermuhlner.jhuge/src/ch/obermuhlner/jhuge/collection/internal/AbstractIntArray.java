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
	public int[] toArray() {
		final int n = size();
		int[] result = new int[n];
		for (int i = 0; i < n; i++) {
			result[i] = get(i);
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		final int n = size();
		int hash = 1;

		for (int i = 0; i < n; i++) {
			hash = hash * 31 + get(i);
		}
		
		return hash;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof IntArray)) {
			return false;
		}
		
		IntArray other = (IntArray) object;

		final int n = size();
		if (other.size() != n) {
			return false;
		}
		for (int i = 0; i < n; i++) {
			if (other.get(i) != get(i)) {
				return false;
			}
		}
		
		return true;
	}
}
