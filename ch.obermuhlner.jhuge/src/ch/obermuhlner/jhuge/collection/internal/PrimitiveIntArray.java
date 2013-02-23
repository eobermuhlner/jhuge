package ch.obermuhlner.jhuge.collection.internal;

/**
 * A {@link IntArray} implementation that stores the values in a Java primitive <code>int[]</code>.
 */
public class PrimitiveIntArray extends AbstractIntArray {

	private int[] array;

	private int size;

	/**
	 * Construct a {@link PrimitiveIntArray}.
	 */
	public PrimitiveIntArray() {
		this(8);
	}
	
	/**
	 * Construct a {@link PrimitiveIntArray} with the specified initial capacity.
	 * 
	 * @param capacity the initial capacity 
	 */
	public PrimitiveIntArray(int capacity) {
		array = new int[capacity];
	}
	
	@Override
	public int set(int index, int value) {
		checkSize(index);
		
		int oldValue = array[index];
		array[index] = value;
		return oldValue;
	}

	@Override
	public void add(int index, int value) {
		checkSizeIncludingRightBound(index);
		
		if (size == array.length) {
			int[] newArray = new int[array.length * 2];
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
	public void add(int value) {
		add(size, value);
	}

	@Override
	public int get(int index) {
		checkSize(index);
		return array[index];
	}
	
	@Override
	public int remove(int index) {
		checkSize(index);

		int oldValue = array[index];
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
