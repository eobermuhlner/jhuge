package ch.obermuhlner.jhuge.collection;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import ch.obermuhlner.jhuge.collection.internal.IntObjectMap;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

public abstract class AbstractHugeHashSet<E> extends AbstractSet<E> {

	private static final byte[] EMPTY_DATA = new byte[0];

	private final MemoryManager memoryManager;
	private final Converter<E> converter;
	
	private final IntObjectMap<long[]> hashCodeMap = new IntObjectMap<long[]>();

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
