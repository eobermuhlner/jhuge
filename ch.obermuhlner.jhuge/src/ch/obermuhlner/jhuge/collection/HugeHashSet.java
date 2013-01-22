package ch.obermuhlner.jhuge.collection;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeSetBuilder;
import ch.obermuhlner.jhuge.collection.internal.IntObjectMap;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link Set} that stores elements in a {@link MemoryManager}.
 * 
 * <p>The implementation mimics a {@link HashSet}.</p>
 * 
 * <p>Access to single elements is O(1).</p>
 * 
 * <p>In order to store the keys and values in the {@link MemoryManager} they must be serialized and deserialized to read them.
 * This is done by a {@link Converter} which can be specified in the {@link Builder}.</p>
 * 
 * @param <E> the type of elements
 */
public class HugeHashSet<E> extends AbstractSet<E> {

	private static final byte[] EMPTY_DATA = new byte[0];

	private final MemoryManager memoryManager;
	private final Converter<E> converter;
	
	private final IntObjectMap<long[]> hashCodeMap = new IntObjectMap<long[]>();

	private HugeHashSet(MemoryManager memoryManager, Converter<E> converter) {
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
	public boolean add(E element) {
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
	
	@Override
	public boolean remove(Object element) {
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

	@Override
	public void clear() {
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

	@Override
	public Iterator<E> iterator() {
		return new HugeHashSetIterator();
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
	
	private class HugeHashSetIterator implements Iterator<E> {
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

		@Override
		public void remove() {
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
	
	/**
	 * Builds a {@link HugeHashSet}.
	 *
	 * @param <E> the type of elements
	 */
	public static class Builder<E> extends AbstractHugeSetBuilder<E> {
		
		private HugeHashSet<E> result;
		
		private boolean built;
		
		private HugeHashSet<E> getSet() {
			if (result == null) {
				result = new HugeHashSet<E>(getMemoryManager(), getElementConverter());
			}
			return result;
		}
		
		@Override
		public Builder<E> classLoader(ClassLoader classLoader) {
			super.classLoader(classLoader);
			return this;
		}
		
		@Override
		public Builder<E> element(Class<E> elementClass) {
			super.element(elementClass);
			return this;
		}
		
		@Override
		public Builder<E> element(Converter<E> elementConverter) {
			super.element(elementConverter);
			return this;
		}
		
		@Override
		public Builder<E> bufferSize(int bufferSize) {
			super.bufferSize(bufferSize);
			return this;
		}
		
		@Override
		public Builder<E> blockSize(int blockSize) {
			super.blockSize(blockSize);
			return this;
		}
		
		@Override
		public Builder<E> memoryManager(MemoryManager memoryManager) {
			super.memoryManager(memoryManager);
			return this;
		}
		
		@Override
		public Builder<E> faster() {
			super.faster();
			return this;
		}
		
		@Override
		public Builder<E> capacity(int capacity) {
			super.capacity(capacity);
			return this;
		}
				
		@Override
		public Builder<E> add(E element) {
			getSet().add(element);
			return this;
		}
		
		@Override
		public Builder<E> addAll(Collection<E> elements) {
			getSet().addAll(elements);
			return this;
		}
		
		@Override
		public Builder<E> addAll(E... elements) {
			return addAll(Arrays.asList(elements));
		}

		@Override
		public HugeHashSet<E> build() {
			if (built) {
				throw new IllegalStateException("Has already been built.");
			}
			built = true;
			return getSet();
		}
	}	
}
