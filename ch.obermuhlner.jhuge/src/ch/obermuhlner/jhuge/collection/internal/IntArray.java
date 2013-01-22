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
}
