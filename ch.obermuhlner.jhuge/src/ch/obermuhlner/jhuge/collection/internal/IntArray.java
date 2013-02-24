package ch.obermuhlner.jhuge.collection.internal;

/**
 * Provides access to an array of int values.
 */
public interface IntArray {

	/**
	 * Sets a value at the specified index position.
	 * 
	 * @param index the index position to set
	 * @param value the value to set
	 * @return the old value
	 */
	int set(int index, int value);
	
	/**
	 * Adds a value at the specified index position.
	 * 
	 * @param index the index position to insert in
	 * @param value the value to add
	 */
	void add(int index, int value);
	
	/**
	 * Adds a value at the end.
	 * 
	 * @param value the value to add
	 */
	void add(int value);
	
	/**
	 * Adds a value in ascending order.
	 * 
	 * <p>The implementation works only correctly if all values in the array are already sorted.
	 * The easiest way to achieve this is to only use this method to add values to the array.</p>
	 * 
	 * @param value the value to insert
	 */
	void addAscending(int value);
		
	/**
	 * Returns the value at the specified index position.
	 * 
	 * @param index the index position to get
	 * @return the value
	 */
	int get(int index);

	/**
	 * Removes a value from the specified index position.
	 * 
	 * @param index the index position to remove
	 * @return the removed value
	 */
	int remove(int index);

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
	int indexOf(int value);
	
	/**
	 * Returns the values of this {@link IntArray} as an <code>int[]</code>.
	 * 
	 * <p>The returned array is a copy of the content and can be modified without influencing the actual values of this {@link IntArray}.</p>
	 * 
	 * @return the values as <code>int[]</code>
	 */
	int[] toArray();
}
