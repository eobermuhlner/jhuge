package ch.obermuhlner.jhuge.collection.internal;

/**
 * Provides access to an array of long values.
 */
public interface LongArray {

	/**
	 * Sets a value at the specified index position.
	 * 
	 * @param index the index position to set
	 * @param value the value to set
	 * @return the old value
	 */
	long set(int index, long value);
	
	/**
	 * Adds a value at the specified index position.
	 * 
	 * @param index the index position to insert in
	 * @param value the value to add
	 */
	void add(int index, long value);
	
	/**
	 * Adds a value at the end.
	 * 
	 * @param value the value to add
	 */
	void add(long value);
	
	/**
	 * Adds a value in ascending order.
	 * 
	 * <p>The implementation works only correctly if all values in the array are already sorted.
	 * The easiest way to achieve this is to only use this method to add values to the array.</p>
	 * 
	 * @param value the value to insert
	 */
	void addAscending(long value);
	
	/**
	 * Returns the value at the specified index position.
	 * 
	 * @param index the index position to get
	 * @return the value
	 */
	long get(int index);

	/**
	 * Removes a value from the specified index position.
	 * 
	 * @param index the index position to remove
	 * @return the removed value
	 */
	long remove(int index);

	/**
	 * Clears the array by removing all values.
	 */
	void clear();
	
	/**
	 * Returns the size of the array
	 * 
	 * @return the size of the array
	 */
	int size();
	
	/**
	 * Returns the index of the first occurrence of the specified value.
	 * 
	 * @param value the value to search for
	 * @return the index of the first occurrence, or -1 if not found
	 */
	int indexOf(long value);
	
	/**
	 * Returns the values of this {@link LongArray} as an <code>long[]</code>.
	 * 
	 * <p>The returned array is a copy of the content and can be modified without influencing the actual values of this {@link IntArray}.</p>
	 * 
	 * @return the values as <code>long[]</code>
	 */
	long[] toArray();
}
