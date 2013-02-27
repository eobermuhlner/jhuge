package ch.obermuhlner.jhuge.collection.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A map where the keys are <code>int</code> and the values are <code>long[]</code> based by a standard Java {@link Map}.
 */
public class PrimitiveIntLongArrayMap implements IntLongArrayMap {

	private Map<Integer, long[]> map;

	/**
	 * Construct a {@link PrimitiveIntLongArrayMap}.
	 */
	public PrimitiveIntLongArrayMap() {
		this(8);
	}
	
	/**
	 * Construct a {@link PrimitiveIntLongArrayMap} with the specified capacity.
	 * 
	 * @param capacity the initial capacity 
	 */
	public PrimitiveIntLongArrayMap(int capacity) {
		map = new HashMap<Integer, long[]>(capacity);
	}
	
	@Override
	public void put(int key, long[] value) {
		map.put(key, value);
	}

	@Override
	public boolean containsKey(int key) {
		return map.containsKey(key);
	}

	@Override
	public long[] get(int key) {
		return map.get(key);
	}

	@Override
	public void remove(int key) {
		map.remove(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public IntIterator keySet() {
		return new MyIntIterator(map.keySet().iterator());
	}
	
	@Override
	public int hashCode() {
		return map.hashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof PrimitiveIntLongArrayMap)) {
			return false;
		}
		
		PrimitiveIntLongArrayMap other = (PrimitiveIntLongArrayMap) object;
		
		return map.equals(other.map);
	}
	
	private static class MyIntIterator implements IntIterator {

		private final Iterator<Integer> iterator;

		public MyIntIterator(Iterator<Integer> iterator) {
			this.iterator = iterator;
		}
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public int next() {
			return iterator.next();
		}
		
		@Override
		public void remove() {
			iterator.remove();
		}
	}
}
