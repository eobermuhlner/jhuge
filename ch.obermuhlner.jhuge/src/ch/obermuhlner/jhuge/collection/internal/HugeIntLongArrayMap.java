package ch.obermuhlner.jhuge.collection.internal;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;

import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * A map where the keys are <code>int</code> and the values are <code>long[]</code> that stores all data in a {@link MemoryManager}.
 */
public class HugeIntLongArrayMap implements IntLongArrayMap {

	private static final long NO_ADDRESS = -1;

	private static final boolean DEBUG = false;
	
	private final MemoryManager memoryManager;

	private HugeLongArray addresses;

	private int size;
	
	private int countHashCodes;

	private float loadFactor = 0.75f;
	
	/**
	 * Constructs a {@link HugeIntLongArrayMap} with the specified {@link MemoryManager} and capacity.
	 * 
	 * @param memoryManager the {@link MemoryManager} to store the data
	 * @param capacity the initial capacity
	 */
	public HugeIntLongArrayMap(MemoryManager memoryManager, int capacity) {
		this.memoryManager = memoryManager;
		
		initialize(capacity);
	}
	
	private void initialize(int capacity) {
		countHashCodes = 0;
		addresses = new HugeLongArray(memoryManager, capacity);
		addresses.setSize(capacity);
		for (int i = 0; i < capacity; i++) {
			addresses.set(i, NO_ADDRESS);
		}
	}
	
	@Override
	public void put(int key, long[] value) {
		growIfNecessary();
		
		int index = hashIndex(key);

		long address = addresses.get(index);
		long newAddress;
		if (address == NO_ADDRESS) {
			newAddress = setValueInNew(key, value);
		} else {
			newAddress = setEntryInOld(address, key, value);
		}

		if (newAddress != address) {
			addresses.set(index, newAddress);
			if (address == NO_ADDRESS) {
				countHashCodes++;
			}
		}
	}

	@Override
	public boolean containsKey(int key) {
		// TODO optimize
		return get(key) != null;
	}

	@Override
	public long[] get(int key) {
		int index = hashIndex(key);
		
		long address = addresses.get(index);
		if (address == NO_ADDRESS) {
			return null;
		}
		
		return getValue(address, key);
	}

	private long[] getValue(long address, int key) {
		byte[] data = memoryManager.read(address);
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		
		int count = byteBuffer.getInt();
		for (int i = 0; i < count; i++) {
			int storedKey = byteBuffer.getInt();
			int storedArrayLength = byteBuffer.getInt();
			if (storedKey == key) {
				long[] result = new long[storedArrayLength];
				for (int j = 0; j < storedArrayLength; j++) {
					result[j] = byteBuffer.getLong();
				}
				return result;
			} else {
				byteBuffer.position(byteBuffer.position() + storedArrayLength * 8);
			}
		}
		
		return null;
	}
	
	private int getKeyInsideIndex(long address, int insideIndex) {
		byte[] data = memoryManager.read(address);
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		
		int count = byteBuffer.getInt();
		if (count < insideIndex) {
			throw new IllegalArgumentException("insideIndex=" + insideIndex + " insideCount=" + count);
		}
		
		for (int i = 0; i < count; i++) {
			int storedKey = byteBuffer.getInt();
			int storedArrayLength = byteBuffer.getInt();
			if (i == insideIndex) {
				return storedKey;
			} else {
				byteBuffer.position(byteBuffer.position() + storedArrayLength * 8);
			}
		}
		
		throw new IllegalArgumentException("insideIndex=" + insideIndex + " insideCount=" + count);
	}
	
	@Override
	public void remove(int key) {
		int index = hashIndex(key);

		long address = addresses.get(index);
		if (address == NO_ADDRESS) {
			return;
		}
		
		byte[] data = memoryManager.read(address);
		if (DEBUG) printData("BEFORE remove " + key, data);
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		
		int count = byteBuffer.getInt();
		if (count == 1) {
			int storedKey = byteBuffer.getInt();
			if (storedKey == key) {
				memoryManager.free(address);
				addresses.set(index, NO_ADDRESS);
				size--;
				countHashCodes--;
				return;
			}
		}
		
		long newAddress = removeEntryInCopy(byteBuffer, key);
		if  (newAddress == NO_ADDRESS) {
			return;
		}
		
		size--;
		memoryManager.free(address);
		addresses.set(index, newAddress);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		int n = addresses.size();
		for (int i = 0; i < n; i++) {
			long address = addresses.get(i);
			if (address != NO_ADDRESS) {
				memoryManager.free(address);
				addresses.set(i, NO_ADDRESS);
			}
		}
		countHashCodes = 0;
		size = 0;
	}

	private int hashIndex(int key) {
		int size = addresses.size();
		return Math.abs(key % size);
	}
	
	@Override
	public IntIterator keySet() {
		return new MyIntIterator();
	}
	
	private long setValueInNew(int key, long[] value) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(128);
		DataOutputStream out = new DataOutputStream(byteStream);
		
		try {
			out.writeInt(1);
			out.writeInt(key);
			out.writeInt(value.length);
			for (int j = 0; j < value.length; j++) {
				out.writeLong(value[j]);
			}				
		} catch(IOException exception) {
			throw new IllegalStateException(exception);
		}
		
		size++;
		byte[] data = byteStream.toByteArray();
		if (DEBUG) printData("setValueInNew " + key + Arrays.toString(value), data);
		return memoryManager.allocate(data);
	}
	
	private long setEntryInOld(long address, int key, long[] value) {
		byte[] data = memoryManager.read(address);
		if (DEBUG) printData("BEFORE setEntryInOld " + key, data);
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);

		boolean found = findKey(byteBuffer, key);

		memoryManager.free(address);
		return setEntryInCopy(byteBuffer, !found, key, value);

		/*
		int count = byteBuffer.getInt();
		for (int i = 0; i < count; i++) {
			int storedKey = byteBuffer.getInt();
			int storedArrayLength = byteBuffer.getInt();
			if (storedKey == key) {
//				if (storedArrayLength == value.length) {
//					for (int j = 0; j < storedArrayLength; j++) {
//						byteBuffer.putLong(value[j]);
//					}
//					if (DEBUG) printData("setEntryInOld " + key + Arrays.toString(value), data);
//					memoryManager.write(address, data);
//					return address;
//				}
				memoryManager.free(address);
				return setEntryInCopy(byteBuffer, false, key, value);
			}
			for (int j = 0; j < storedArrayLength; j++) {
				byteBuffer.getLong();
			}
		}
		memoryManager.free(address);
		return setEntryInCopy(byteBuffer, true, key, value);
		*/
	}

	private long setEntryInCopy(ByteBuffer byteBuffer, boolean append, int key, long[] value) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(128);
		DataOutputStream out = new DataOutputStream(byteStream);
		
		try {
			byteBuffer.position(0);

			int count = byteBuffer.getInt();
			out.writeInt(append ? count + 1 : count);
			for (int i = 0; i < count; i++) {
				int storedKey = byteBuffer.getInt();
				int storedArrayLength = byteBuffer.getInt();
				out.writeInt(storedKey);
				if (storedKey == key) {
					assert !append;
					for (int j = 0; j < storedArrayLength; j++) {
						byteBuffer.getLong();
					}				
					out.writeInt(value.length);
					for (int j = 0; j < value.length; j++) {
						out.writeLong(value[j]);
					}				
				} else {
					out.writeInt(storedArrayLength);
					for (int j = 0; j < storedArrayLength; j++) {
						out.writeLong(byteBuffer.getLong());
					}				
				}
			}
			
			if (append) {
				size++;
				out.writeInt(key);
				out.writeInt(value.length);
				for (int j = 0; j < value.length; j++) {
					out.writeLong(value[j]);
				}				
			}
		} catch(IOException exception) {
			throw new IllegalStateException(exception);
		}

		byte[] data = byteStream.toByteArray();
		if (DEBUG) printData("setEntryInCopy " + key + Arrays.toString(value), data);
		return memoryManager.allocate(data);
	}

	/**
	 * Removes the specified key from the {@link ByteBuffer} where key/value entries are stored.
	 * @param byteBuffer the {@link ByteBuffer} containing key/value entries
	 * @param key the key to remove
	 * @return the address of the newly allocated buffer (with the key removed) or -1 if the key was not found in the {@link ByteBuffer}
	 */
	private long removeEntryInCopy(ByteBuffer byteBuffer, int key) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(128);
		DataOutputStream out = new DataOutputStream(byteStream);
		
		boolean found = findKey(byteBuffer, key);
		if (!found) {
			return -1;
		}
		
		try {
			byteBuffer.position(0);

			int count = byteBuffer.getInt();
			out.writeInt(count - 1);
			for (int i = 0; i < count; i++) {
				int storedKey = byteBuffer.getInt();
				int storedArrayLength = byteBuffer.getInt();
				if (storedKey == key) {
					assert found;
					for (int j = 0; j < storedArrayLength; j++) {
						byteBuffer.getLong();
					}				
					// do not write anything
				} else {
					out.writeInt(storedKey);
					out.writeInt(storedArrayLength);
					for (int j = 0; j < storedArrayLength; j++) {
						out.writeLong(byteBuffer.getLong());
					}				
				}
			}
		} catch(IOException exception) {
			throw new IllegalStateException(exception);
		}
		
		byte[] data = byteStream.toByteArray();
		if (DEBUG) printData("removeEntryInCopy " + key, data);
		return memoryManager.allocate(data);
	}


	private int getEntriesCount(long address) {
		byte[] data = memoryManager.read(address);
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		
		int count = byteBuffer.getInt();
		return count;
	}

	private static void printData(String message, byte[] data) {
//		System.out.println("--- " + message);
//		System.out.println("data.length = " + data.length);
//		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
//		
//		int count = byteBuffer.getInt();
//		System.out.println("count = " + count);
//		for (int i = 0; i < count; i++) {
//			int storedKey = byteBuffer.getInt();
//			int storedArrayLength = byteBuffer.getInt();
//			System.out.println("#" + i + " key = " + storedKey);
//			System.out.println("#" + i + " value.length = " + storedArrayLength);
//			for (int j = 0; j < storedArrayLength; j++) {
//				long storedArrayElement = byteBuffer.getLong();
//				System.out.println("#" + i + " value[" + j + "] = " + storedArrayElement);
//			}
//		}		
	}

	private boolean findKey(ByteBuffer byteBuffer, int key) {
		byteBuffer.position(0);

		int count = byteBuffer.getInt();
		for (int i = 0; i < count; i++) {
			int storedKey = byteBuffer.getInt();
			int storedArrayLength = byteBuffer.getInt();
			if (key == storedKey) {
				return true;
			}
			byteBuffer.position(byteBuffer.position() + storedArrayLength * 8);
		}
		
		return false;
	}
	
	private void growIfNecessary() {
		int thresholdSize = (int)(addresses.size() * loadFactor);
		if (countHashCodes <= thresholdSize) {
			return;
		}

		grow();
	}

	private void grow() {
		HugeLongArray oldAddresses = addresses;
		
		// instead of just doubling the size - this gives more prime number sizes which tend to distribute hash codes better
		int newCapacity = oldAddresses.size() <= 1 ? 2 : oldAddresses.size() * 2 - 1;
		initialize(newCapacity);
		
		for (int oldAddressIndex = 0; oldAddressIndex < oldAddresses.size(); oldAddressIndex++) {
			long address = oldAddresses.get(oldAddressIndex);
			if (address != NO_ADDRESS) {
				boolean cheap = false;
				int entriesCount = getEntriesCount(address);
				if (entriesCount == 1) {
					// maybe possible to do a cheap copy of key/value pair
					int key = getKeyInsideIndex(address, 0);
					int index = hashIndex(key);
					if (addresses.get(index) == NO_ADDRESS) {
						// possible to do a cheap copy, since only 1 key/value pair and hashIndex in new growing map is still empty - just point to the old address
						addresses.set(index, address);
						countHashCodes++;
						cheap = true;
					}
				}
				
				if (!cheap) {
					// extract all key/value pairs at 'address' and put then into the growing new map
					byte[] data = memoryManager.read(address);
					memoryManager.free(address);
					ByteBuffer byteBuffer = ByteBuffer.wrap(data);
					
					int count = byteBuffer.getInt();
					for (int i = 0; i < count; i++) {
						int storedKey = byteBuffer.getInt();
						int storedArrayLength = byteBuffer.getInt();
						long[] storedArray = new long[storedArrayLength];
						for (int j = 0; j < storedArrayLength; j++) {
							storedArray[j] = byteBuffer.getLong();
						}
						put(storedKey, storedArray);
						size--; // correct the size that was modified by put() 
					}		
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{size=" + size() + ", tableSize=" + addresses.size() + ", tableUsed=" + countHashCodes + "}";
	}

	@Override
	public int hashCode() {
		int hash = size();
		
		IntIterator keySet = keySet();
		while (keySet.hasNext()) {
			int key = keySet.next();
			long[] value = get(key);

			int entryHash = key + Arrays.hashCode(value);
			hash += entryHash; // not multiplied with factor - so hash does not depend in order in keySet
		}		
		
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof HugeIntLongArrayMap)) {
			return false;
		}
		
		HugeIntLongArrayMap other = (HugeIntLongArrayMap) object;
		
		if (size() != other.size()) {
			return false;
		}
		
		IntIterator keySet = keySet();
		while (keySet.hasNext()) {
			int key = keySet.next();
			long[] value = get(key);
			
			if (value == null) {
				if (!(other.containsKey(key) && other.get(key) == null)) {
					return false;
				}
			} else {
				long[] otherValue = other.get(key);
				if (!Arrays.equals(value, otherValue)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private class MyIntIterator implements IntIterator {

		private int currentAddressIndex;
		private int currentEntriesIndex;
		
		private int nextAddressIndex = -1;
		private int nextEntriesCount;
		private int nextEntriesIndex;
		
		public MyIntIterator() {
			toNext();
		}

		@Override
		public boolean hasNext() {
			int n = addresses.size();
			return nextEntriesIndex < nextEntriesCount && nextAddressIndex < n;
		}

		@Override
		public int next() {
			if (nextAddressIndex < 0 || nextAddressIndex >= addresses.size()) {
				throw new NoSuchElementException();
			}
			
			toNext();
			
			long address = addresses.get(currentAddressIndex);
			int key = getKeyInsideIndex(address, currentEntriesIndex);
			return key;
		}
		
		private void toNext() {
			currentAddressIndex = nextAddressIndex;
			currentEntriesIndex = nextEntriesIndex;
			
			if (nextEntriesIndex + 1 < nextEntriesCount) {
				nextEntriesIndex++;
				return;
			}

			nextEntriesIndex = 0;
			int n = addresses.size();
			while (++nextAddressIndex < n) {
				long address = addresses.get(nextAddressIndex);
				if (address != NO_ADDRESS) {
					nextEntriesCount = getEntriesCount(address);
					return;
				}
			}
			
			nextAddressIndex = Integer.MAX_VALUE;
		}
		
		@Override
		public void remove() {
			long address = addresses.get(currentAddressIndex);
			int key = getKeyInsideIndex(address, currentEntriesIndex);
			
			HugeIntLongArrayMap.this.remove(key);
			
			if (currentAddressIndex == nextAddressIndex) {
				nextEntriesCount--;
				nextEntriesIndex--;
				if (nextEntriesCount == 0) {
					toNext();
				}
			}
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(getClass().getSimpleName());
			result.append("{currentAddressIndex=");
			result.append(currentAddressIndex);
			result.append(", currentEntriesIndex=");
			result.append(currentEntriesIndex);
			result.append(", nextAddressIndex=");
			result.append(nextAddressIndex);
			result.append(", nextEntriesCount=");
			result.append(nextEntriesCount);
			result.append(", nextEntriesIndex=");
			result.append(nextEntriesIndex);
			result.append("}");
			return result.toString();
		}
	}
}
