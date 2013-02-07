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
	 * Returns the index of the first occurrence of the specified element.
	 * 
	 * @param element the element to search for
	 * @return the index of the first occurrence, or -1 if not found
	 */
	int indexOf(long element);
}
