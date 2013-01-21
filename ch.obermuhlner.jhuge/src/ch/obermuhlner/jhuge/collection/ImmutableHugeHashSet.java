package ch.obermuhlner.jhuge.collection;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeSetBuilder;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A {@link Set} that stores elements in a {@link MemoryManager}.
 * 
 * <p>The implementation mimics a {@link HashSet} but is immutable.</p>
 * 
 * <p>Access to single elements is O(1).</p>
 * 
 * <p>In order to store the keys and values in the {@link MemoryManager} they must be serialized and deserialized to read them.
 * This is done by a {@link Converter} which can be specified in the {@link Builder}.</p>
 * 
 * @param <E> the type of elements
 */
public class ImmutableHugeHashSet<E> extends AbstractSet<E> {

	private final MemoryManager memoryManager;
	private final Converter<E> converter;

	private final int[] elementHashCodes;
	private final long[] elementAddresses;

	private ImmutableHugeHashSet(MemoryManager memoryManager, Converter<E> converter, int[] elementHashCodes, long[] elementAddresses) {
		this.memoryManager = memoryManager;
		this.converter = converter;
		this.elementHashCodes = elementHashCodes;
		this.elementAddresses = elementAddresses;
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
	public boolean contains(Object object) {
		int hashCode = object == null ? 0 : object.hashCode();
		
		int matchingIndex = indexOfMatchingHashCode(hashCode);
		if (matchingIndex < 0) {
			return false;
		}

		// check equals() of first matching element
		if (equalsElementAtIndex(object, matchingIndex)) {
			return true;
		}

		// check equals() of left of first matching element (as long hash code matches)
		int index = matchingIndex - 1;
		while (index >= 0 && elementHashCodes[index] == hashCode) {
			if (equalsElementAtIndex(object, index)) {
				return true;
			}
			index--;
		}

		// check equals() of right of first matching element (as long hash code matches)
		index = matchingIndex + 1;
		while (index < elementHashCodes.length && elementHashCodes[index] == hashCode) {
			if (equalsElementAtIndex(object, index)) {
				return true;
			}
			index++;
		}

		return false;
	}
	
	private boolean equalsElementAtIndex(Object object, int index) {
		byte[] data = memoryManager.read(elementAddresses[index]);
		E element = deserializeElement(data);
	
		return object == null ? element == null : object.equals(element);
	}

	private int indexOfMatchingHashCode(int theElementHashCode) {
		int aStartIndex = 0;
		int aEndIndex = elementHashCodes.length - 1;
		while (aEndIndex >= aStartIndex) {
			int aMidIndex = (aStartIndex + aEndIndex) / 2;
			int aMidIndexHashCode = elementHashCodes[aMidIndex];

			if (theElementHashCode < aMidIndexHashCode) {
				aEndIndex = aMidIndex - 1;
			}
			else if (theElementHashCode > aMidIndexHashCode) {
				aStartIndex = aMidIndex + 1;
			}
			else {
				return aMidIndex;
			}
		}

		return -1;
	}
	
	@Override
	public Iterator<E> iterator() {
		return new ElementIterator();
	}

	@Override
	public int size() {
		return elementHashCodes.length;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{size=" + size() + "}";
	}
	
	private E deserializeElement(byte[] data) {
		return (data == null || data.length == 0) ? null : converter.deserialize(data);
	}

	class ElementIterator implements Iterator<E> {

		int index;
		
		@Override
		public boolean hasNext() {
			return index < elementAddresses.length;
		}

		@Override
		public E next() {
			if (index >= elementAddresses.length) {
				throw new NoSuchElementException();
			}
			
			byte[] data = memoryManager.read(elementAddresses[index]);
			index++;
			E element = deserializeElement(data);
			return element;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	/**
	 * Builds a {@link ImmutableHugeHashSet}.
	 *
	 * @param <E> the type of elements
	 */
	public static class Builder<E> extends AbstractHugeSetBuilder<E> {

		private static final byte[] EMPTY_DATA = new byte[0];
		
		private final List<Entry> entries = new ArrayList<Entry>();

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
		public Builder<E> addAll(Collection<E> elements) {
			for (E element : elements) {
				add(element);
			}
			return this;
		}
		
		@Override
		public Builder<E> addAll(E... elements) {
			return addAll(Arrays.asList(elements));
		}
		
		/**
		 * Adds a set of elements.
		 * 
		 * <p>Since the specified {@link Set} cannot contain duplicate events this method can be faster than {@link #addAll(Collection)}
		 * if there have elements been added to the builder yet.</p>
		 * 
		 * @param elements the elements to add
		 * @return the builder, so that calls can be chained
		 */
		public Builder<E> addAll(Set<E> elements) {
			if (entries.size() > 0) {
				addAll((Collection<E>) elements);
				return this;
			}
			
			// optimized adding of a Set, when this builder is still empty - we are sure there are no duplicates
			for (E element : elements) {
				int hashCode = element == null ? 0 : element.hashCode();
				add(hashCode, element);
			}
			return this;
		}
		
		@Override
		public Builder<E> add(E element) {
			int hashCode = element == null ? 0 : element.hashCode();
			add(hashCode, element);
			
			return this;
		}

		private void add(int hashCode, E element) {
			byte[] data = serializeElement(element);
			long address = getMemoryManager().allocate(data);
			
			Entry entry = new Entry(hashCode, address);
			entries.add(entry);
		}
		
		@Override
		public ImmutableHugeHashSet<E> build() {
			// sort entries by size - O(n * log(n))
			Collections.sort(entries, new Comparator<Entry>() {
				@Override
				public int compare(Entry o1, Entry o2) {
					return o1.hashCode == o2.hashCode ? 0 : o1.hashCode < o2.hashCode ? -1 : +1;
				}
			});
			
			if (entries.size() > 1) {
				// search for duplicate entries and remove them - O(n)
				int index = entries.size() - 1;
				List<Entry> sameHashCodeEntries = new ArrayList<Entry>();
				sameHashCodeEntries.add(entries.get(index--));
				while (index >= 0) {
					Entry entry = entries.get(index);
					if (sameHashCodeEntries.get(0).hashCode == entry.hashCode) {
						for (Entry sameHashCodeEntry : sameHashCodeEntries) {
							if (elementEquals(entry.address, sameHashCodeEntry.address)) {
								getMemoryManager().free(entry.address);
								entries.remove(index);
							}
						}
					} else {
						sameHashCodeEntries.clear();
						sameHashCodeEntries.add(entry);
					}
					
					index--;
				}
			}
			
			int size = entries.size();
			int[] hashCodes =  new int[size];
			long[] addresses = new long[size];
			for (int i = 0; i < size; i++) {
				Entry entry = entries.get(i);
				hashCodes[i] = entry.hashCode;
				addresses[i] = entry.address;
			}
			
			return new ImmutableHugeHashSet<E>(getMemoryManager(), getElementConverter(), hashCodes, addresses);
		}

		private boolean elementEquals(long address1, long address2) {
			byte[] data1 = getMemoryManager().read(address1);
			E element1 = deserializeElement(data1);
			byte[] data2 = getMemoryManager().read(address2);
			E element2 = deserializeElement(data2);
			return element1 == null ? element2 == null : element1.equals(element2);
		}

		private byte[] serializeElement(E element) {
			return element == null ? EMPTY_DATA : getElementConverter().serialize(element);
		}

		private E deserializeElement(byte[] data) {
			return (data == null || data.length == 0) ? null : getElementConverter().deserialize(data);
		}

	}
	
	private static class Entry {
		final int hashCode;
		final long address;
		
		Entry(int hashCode, long address) {
			this.hashCode = hashCode;
			this.address = address;
		}
		
		@Override
		public String toString() {
			return "(" + Integer.toHexString(hashCode) + "," + address + ")";
		}
	}
}
