package ch.obermuhlner.jhuge.collection;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.internal.IntObjectMap;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * An abstract base class to simplify implementing a huge {@link Set} that mimics a {@link HashSet}.
 * 
 * <p>The mutating operations are implemented as protected methods with the suffix "Internal".</p>
 * <ul>
 * <li><code>addInternal(E)</code></li>
 * <li><code>removeInternal(Object)</code></li>
 * <li><code>clearInternal()</code></li>
 * </ul>
 * 
 * <p>Immutable concrete subclasses can call these internal methods to implement a builder.</p>
 * <p>Mutable concrete subclasses can implement the equivalent public methods by calling the internal methods.</p>
 * 
 * @param <E> the type of elements
 */
public abstract class AbstractHugeHashSet<E> extends AbstractSet<E> {

	private static final byte[] EMPTY_DATA = new byte[0];

	private final MemoryManager memoryManager;
	private final Converter<E> converter;
	
	private final IntObjectMap<long[]> hashCodeMap = new IntObjectMap<long[]>();

	/**
	 * Constructs a {@link AbstractHugeHashSet}.
	 * 
	 * @param memoryManager the {@link MemoryManager}
	 * @param converter the element {@link Converter}
	 */
	protected AbstractHugeHashSet(MemoryManager memoryManager, Converter<E> converter) {
		this.memoryManager = memoryManager;
		this.converter = converter;
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
	public boolean contains(Object element) {
		int hashCode = element == null ? 0 : element.hashCode();
		long[] addresses = hashCodeMap.get(hashCode);
		
		if (addresses == null) {
			return false;
			
		} else {
			for (int i = 0; i < addresses.length; i++) {
				E oldElement = readElement(addresses[i]);
				if (element == null ? oldElement == null : element.equals(oldElement)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Adds an element.
	 * 
	 * <p>This method has the same semantics as {@link #add(Object)}.
	 * Mutable subclasses can implement {@link #add(Object)} by calling this method.</p>
	 * <p>Immutable subclasses can call this method from the builder.</p>
	 * 
	 * @param element the element to add
	 * @return <code>true</code> if the element was added, <code>false</code> otherwise 
	 */
	protected boolean addInternal(E element) {
		int hashCode = element == null ? 0 : element.hashCode();
		long[] addresses = hashCodeMap.get(hashCode);
		
		if (addresses == null) {
			long address = writeElement(element);
			hashCodeMap.put(hashCode, new long[] { address });
			return true;
			
		} else {
			for (int i = 0; i < addresses.length; i++) {
				E oldElement = readElement(addresses[i]);
				if (element == null ? oldElement == null : element.equals(oldElement)) {
					return false;
				}
			}
			
			long newAddress = writeElement(element);
			long[] newAddresses = new long[addresses.length + 1];
			System.arraycopy(addresses, 0, newAddresses, 0, addresses.length);
			newAddresses[newAddresses.length - 1] = newAddress;
			hashCodeMap.put(hashCode, newAddresses);
			return true;
		}
	}
	
	/**
	 * Removes the element.
	 * 
	 * <p>This method has the same semantics as {@link #remove(Object)}.
	 * Mutable subclasses can implement {@link #remove(Object)} by calling this method.</p>
	 * <p>Immutable subclasses can call this method from the builder.</p>
	 * 
	 * @param element the element to remove
	 * @return <code>true</code> if the element was removed, <code>false</code> otherwise
	 */
	protected boolean removeInternal(Object element) {
		int hashCode = element == null ? 0 : element.hashCode();
		long[] addresses = hashCodeMap.get(hashCode);
		
		if (addresses == null) {
			return false;
			
		} else if (addresses.length == 1) {
			E oldElement = readElement(addresses[0]);
			if (element == null ? oldElement == null : element.equals(oldElement)) {
				memoryManager.free(addresses[0]);
				hashCodeMap.remove(hashCode);
				return true;
			}
			
		} else {
			for (int i = 0; i < addresses.length; i++) {
				E oldElement = readElement(addresses[i]);
				if (element == null ? oldElement == null : element.equals(oldElement)) {
					memoryManager.free(addresses[i]);
					long[] newAddresses = new long[addresses.length - 1];
					System.arraycopy(addresses, 0, newAddresses, 0, i);
					System.arraycopy(addresses, i + 1, newAddresses, i, addresses.length - i - 1);
					hashCodeMap.put(hashCode, newAddresses);
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Removes all elements.
	 * 
	 * <p>This method has the same semantics as {@link #clear()}.
	 * Mutable subclasses can implement {@link #clear()} by calling this method.</p>
	 * <p>Immutable subclasses can call this method from the builder.</p>
	 */
	protected void clearInternal() {
		hashCodeMap.clear();
		memoryManager.reset();
	}
	
	@Override
	public int size() {
		int result = 0;
		
		for (long[] addresses : hashCodeMap.values()) {
			result += addresses.length;
		}
		
		return result;
	}

	private long writeElement(E element) {
		byte[] data = serializeElement(element);
		long address = memoryManager.allocate(data);
		return address;
	}
	
	private E readElement(long address) {
		byte[] data = memoryManager.read(address);
		E element = deserializeElement(data);
		return element;
	}
	
	private byte[] serializeElement(E element) {
		return element == null ? EMPTY_DATA : converter.serialize(element);
	}

	private E deserializeElement(byte[] data) {
		return (data == null || data.length == 0) ? null : converter.deserialize(data);
	}
	
	/**
	 * Abstract base class to simplify implementing an {@link Iterator} for concrete subclasses of {@link AbstractHugeHashSet}.
	 * 
	 * <p>The mutating operation is implemented as a protected method with the suffix "Internal".</p>
	 * <ul>
	 * <li><code>removeInternal()</code></li>
	 * </ul>
	 */
	protected abstract class AbstractHugeHashSetIterator implements Iterator<E> {
		private Iterator<Entry<Integer, long[]>> hashCodeMapIterator = hashCodeMap.entrySet().iterator();
		private Entry<Integer, long[]> currentEntry = null;
		private int currentIndex = 0;

		@Override
		public boolean hasNext() {
			return (currentEntry != null && currentIndex + 1 < currentEntry.getValue().length) || hashCodeMapIterator.hasNext();
		}

		@Override
		public E next() {
			currentIndex++;
			if (currentEntry == null || currentIndex >= currentEntry.getValue().length) {
				currentIndex = 0;
				currentEntry = hashCodeMapIterator.hasNext() ? hashCodeMapIterator.next() : null;
			}
			if (currentEntry == null) {
				throw new NoSuchElementException();
			}

			long[] addresses = currentEntry.getValue();
			long address = addresses[currentIndex];
			return readElement(address);
		}

		/**
		 * Removes the last retrieved element from the underlying collection.
		 * 
		 * <p>This method has the same semantics as {@link #remove()}.
		 * Mutable subclasses can implement {@link #remove()} by calling this method.</p>
		 * <p>Immutable subclasses can call this method from the builder.</p>
		 */
		protected void removeInternal() {
			long[] addresses = currentEntry.getValue();
			long address = addresses[currentIndex];
			memoryManager.free(address);
			
			if (currentIndex == 0 && addresses.length == 1) {
				// remove entire entry
				hashCodeMapIterator.remove();
			} else {
				// remove 1 address from entry
				long[] newAddresses = new long[addresses.length - 1];
				System.arraycopy(addresses, 0, newAddresses, 0, currentIndex);
				System.arraycopy(addresses, currentIndex+1, newAddresses, currentIndex, addresses.length - currentIndex - 1);
				currentEntry.setValue(newAddresses);
				currentIndex--;
			}
		}
	}
}
