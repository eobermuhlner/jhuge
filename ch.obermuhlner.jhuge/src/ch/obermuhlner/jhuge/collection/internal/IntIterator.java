package ch.obermuhlner.jhuge.collection.internal;

import java.util.Iterator;

/**
 * Iterates over <code>int</code> values.
 * 
 * <p>This is not an {@link Iterator} but has been designed with the same goal in mind.
 * It uses the native type <code>int</code>.</p>
 */
public interface IntIterator {

	/**
	 * Returns whether the iterator has a valid next value.
	 * 
	 * @return <code>true</code> if there is a next value, <code>false</code> otherwise
	 */
	boolean hasNext();
	
	/**
	 * Returns the next valid value.
	 * 
	 * @return the next value value
	 */
	int next();
	
	/**
	 * Removes the current value.
	 */
	void remove();
}