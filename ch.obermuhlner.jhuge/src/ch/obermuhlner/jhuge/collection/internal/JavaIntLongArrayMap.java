package ch.obermuhlner.jhuge.collection.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A map where the keys are <code>int</code> and the values are <code>long[]</code> based by a standard Java {@link Map}.
 */
public class JavaIntLongArrayMap implements IntLongArrayMap {

	private Map<Integer, long[]> map;

	/**
	 * Construct a {@link JavaIntLongArrayMap}.
	 */
	public JavaIntLongArrayMap() {
		this(8);
	}
	
	/**
	 * Construct a {@link JavaIntLongArrayMap} with the specified capacity.
	 * 
	 * @param capacity the initial capacity 
	 */
	public JavaIntLongArrayMap(int capacity) {
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
