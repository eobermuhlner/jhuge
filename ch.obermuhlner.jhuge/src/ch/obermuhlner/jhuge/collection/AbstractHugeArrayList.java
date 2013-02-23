package ch.obermuhlner.jhuge.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.jhuge.collection.internal.HugeLongArray;
import ch.obermuhlner.jhuge.collection.internal.PrimitiveLongArray;
import ch.obermuhlner.jhuge.collection.internal.LongArray;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * An abstract base class to simplify implementing a huge {@link List} that mimics an {@link ArrayList}.
 * 
 * <p>The mutating operations are implemented as protected methods with the suffix "Internal".</p>
 * <ul>
 * <li><code>setInternal(int, E)</code></li>
 * <li><code>addInternal(int, E)</code></li>
 * <li><code>removeInternal(int)</code></li>
 * <li><code>clearInternal()</code></li>
 * </ul>
 * 
 * <p>Immutable concrete subclasses can call these internal methods to implement a builder.</p>
 * <p>Mutable concrete subclasses can implement the equivalent public methods by calling the internal methods.</p>
 * 
 * @param <E> the type of elements
 */
public abstract class AbstractHugeArrayList<E> extends AbstractList<E> {

	private static final byte[] EMPTY_DATA = new byte[0];

	private final MemoryManager memoryManager;
	
	private final LongArray addresses;

	private final Converter<E> converter;

	/**
	 * Constructs a {@link AbstractHugeArrayList}.
	 * 
	 * @param memoryManager the {@link MemoryManager}
	 * @param converter the element {@link Converter}
	 * @param faster <code>true</code> to trade memory consumption for improved performance
	 * @param capacity the initial capacity
	 */
	protected AbstractHugeArrayList(MemoryManager memoryManager, Converter<E> converter, boolean faster, int capacity) {
		this.memoryManager = memoryManager;
		this.converter = converter;
		this.addresses = faster ? new PrimitiveLongArray(capacity) : new HugeLongArray(memoryManager, capacity);
	}

	/**
	 * Returns the {@link MemoryManager}.
	 * 
	 * @return the {@link MemoryManager}
	 */
	MemoryManager getMemoryManager() {
		return memoryManager;
	}
	
	/**
	 * Returns the element {@link Converter}.
	 * 
	 * @return the element {@link Converter}
	 */
	Converter<E> getElementConverter() {
		return converter;
	}
	
	@Override
	public E get(int index) {
		long address = addresses.get(index);
		byte[] data = memoryManager.read(address);
		E element = deserializeElement(data);
		return element;
	}

	/**
	 * Sets an element at the specified index position.
	 * 
	 * <p>This method has the same semantics as {@link #set(int, Object)}.
	 * Mutable subclasses can implement {@link #set(int, Object)} by calling this method.</p>
	 * <p>Immutable subclasses can call this method from the builder.</p>
	 * 
	 * @param index the index to insert the element
	 * @param element the element to insert
	 * @return the old element
	 */
	protected E setInternal(int index, E element) {
		byte[] data = serializeElement(element);
		long address = memoryManager.allocate(data);

		long oldAddress = addresses.set(index, address);
		byte[] oldData = memoryManager.read(oldAddress);
		E oldElement = deserializeElement(oldData);
		memoryManager.free(oldAddress);
		
		return oldElement;
	}

	/**
	 * Adds an element at the specified index position.
	 * 
	 * <p>This method has the same semantics as {@link #add(int, Object)}.
	 * Mutable subclasses can implement {@link #add(int, Object)} by calling this method.</p>
	 * <p>Immutable subclasses can call this method from the builder.</p>
	 * 
	 * @param index the index to insert the element
	 * @param element the element to insert
	 */
	protected void addInternal(int index, E element) {
		byte[] data = serializeElement(element);
		long address = memoryManager.allocate(data);
		
		addresses.add(index, address);
	}

	/**
	 * Removes the element at the specified index position.
	 * 
	 * <p>This method has the same semantics as {@link #remove(int)}.
	 * Mutable subclasses can implement {@link #remove(int)} by calling this method.</p>
	 * <p>Immutable subclasses can call this method from the builder.</p>
	 * 
	 * @param index the index of the element to remove
	 * @return the old element
	 */
	protected E removeInternal(int index) {
		long oldAddress1 = addresses.remove(index);
		long oldAddress = oldAddress1;
		byte[] oldData = memoryManager.read(oldAddress);
		E oldElement = deserializeElement(oldData);
		memoryManager.free(oldAddress);
		
		return oldElement;
	}

	/**
	 * Removes all elements.
	 * 
	 * <p>This method has the same semantics as {@link #clear()}.
	 * Mutable subclasses can implement {@link #clear()} by calling this method.</p>
	 * <p>Immutable subclasses can call this method from the builder.</p>
	 */
	protected void clearInternal() {
		for (int i = 0; i < addresses.size(); i++) {
			long address = addresses.get(i);
			memoryManager.free(address);
		}
		addresses.clear();
		
		//memoryManager.compact();
	}
	
	@Override
	public int size() {
		return addresses.size();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{size=" + size() + "}";
	}
	
	private byte[] serializeElement(E element) {
		return element == null ? EMPTY_DATA : converter.serialize(element);
	}

	private E deserializeElement(byte[] data) {
		return (data == null || data.length == 0) ? null : converter.deserialize(data);
	}
}
