package ch.obermuhlner.jhuge.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeSetBuilder;
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
 * This is done by a {@link Converter} which can be specified in the {@link Builder}.
 * The default {@link Converter} can handle instances of all serializable classes.</p>
 * 
 * @param <E> the type of elements
 */
public class HugeHashSet<E> extends AbstractHugeHashSet<E> {

	private HugeHashSet(MemoryManager memoryManager, Converter<E> converter, boolean faster, int capacity) {
		super(memoryManager, converter, faster, capacity);
	}

	@Override
	public boolean add(E element) {
		return addInternal(element);
	}
	
	@Override
	public boolean remove(Object element) {
		return removeInternal(element);
	}

	@Override
	public void clear() {
		clearInternal();
	}
	@Override
	public Iterator<E> iterator() {
		return new HugeHashSetIterator();
	}

	private class HugeHashSetIterator extends AbstractHugeHashSetIterator {
		@Override
		public void remove() {
			removeInternal();
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
				result = new HugeHashSet<E>(getMemoryManager(), getElementConverter(), isFaster(), getCapacity());
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
			for (E element : elements) {
				add(element);
			}
			return this;
		}
		
		@Override
		public Builder<E> addAll(E... elements) {
			for (E element : elements) {
				add(element);
			}
			return this;
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
