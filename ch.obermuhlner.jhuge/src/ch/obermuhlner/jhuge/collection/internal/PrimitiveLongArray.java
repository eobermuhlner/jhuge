package ch.obermuhlner.jhuge.collection.internal;

/**
 * A {@link LongArray} implementation that stores the values in a Java primitive <code>long[]</code>.
 */
public class PrimitiveLongArray extends AbstractLongArray {

	private long[] array;

	private int size;

	/**
	 * Construct a {@link PrimitiveLongArray}.
	 */
	public PrimitiveLongArray() {
		this(8);
	}
	
	/**
	 * Construct a {@link PrimitiveLongArray} with the specified initial capacity.
	 * 
	 * @param capacity the initial capacity 
	 */
	public PrimitiveLongArray(int capacity) {
		array = new long[capacity];
	}
	
	@Override
	public long set(int index, long value) {
		checkSize(index);
		
		long oldValue = array[index];
		array[index] = value;
		return oldValue;
	}

	@Override
	public void add(int index, long value) {
		checkSizeIncludingRightBound(index);
		
		if (size == array.length) {
			long[] newArray = new long[array.length * 2];
			System.arraycopy(array, 0, newArray, 0, index);
			newArray[index] = value;
			System.arraycopy(array, index, newArray, index + 1, size - index);
			array = newArray;
		} else {
			System.arraycopy(array, index, array, index + 1, size - index);
			array[index] = value;
		}

		size++;
	}

	@Override
	public void add(long value) {
		add(size, value);
	}

	@Override
	public long get(int index) {
		checkSize(index);
		return array[index];
	}
	
	@Override
	public long remove(int index) {
		checkSize(index);

		long oldValue = array[index];
		System.arraycopy(array, index + 1, array, index, size - index - 1);
		size--;

		return oldValue;
	}

	@Override
	public void clear() {
		size = 0;
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public int indexOf(long element) {
		final int n = size();
		for (int i = 0; i < n; i++) {
			if (element == array[i]) {
				return i;
			}
		}
		
		return -1;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('[');
		for (int i = 0; i < size; i++) {
			if (i != 0) {
				result.append(", ");
			}
			result.append(array[i]);
		}
		result.append(']');
		
		return result.toString();
	}

	private void checkSize(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
		}
	}

	private void checkSizeIncludingRightBound(int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
		}
	}
}
