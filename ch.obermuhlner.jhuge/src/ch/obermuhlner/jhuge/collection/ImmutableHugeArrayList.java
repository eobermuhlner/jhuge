package ch.obermuhlner.jhuge.collection;

import java.util.Collection;
import java.util.List;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeListBuilder;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * An immutable {@link List} that stores elements in a {@link MemoryManager}.
 * 
 * <p>The mutating operations throw {@link UnsupportedOperationException}:</p>
 * <ul>
 * <li> {@link #set(int, Object)}</li>
 * <li> {@link #add(Object)}</li>
 * <li> {@link #add(int, Object)}</li>
 * <li> {@link #addAll(Collection)}</li>
 * <li> {@link #addAll(int, Collection)}</li>
 * <li> {@link #remove(int)}</li>
 * <li> {@link #remove(Object)}</li>
 * <li> {@link #removeAll(Collection)}</li>
 * <li> {@link #clear()}</li>
 * </ul>
 * 
 * <p>In order to create an {@link ImmutableHugeArrayList} you must add the elements in the {@link Builder}.</p>
 * 
 * <p>Other than the mutating operations all semantics, memory consumption and performance are identical to {@link HugeArrayList}.</p>
 * 
 * @param <E> the type of elements
 * @see HugeArrayList
 */
public class ImmutableHugeArrayList<E> extends AbstractHugeArrayList<E> {

	protected ImmutableHugeArrayList(MemoryManager memoryManager, Converter<E> converter, boolean faster, int capacity) {
		super(memoryManager, converter, faster, capacity);
	}

	/**
	 * Builds a {@link ImmutableHugeArrayList}.
	 * 
	 * @param <E> the type of elements
	 */
	public static class Builder<E> extends AbstractHugeListBuilder<E> {
		
		private ImmutableHugeArrayList<E> result;
		
		private boolean built;
		
		private ImmutableHugeArrayList<E> getList() {
			if (result == null) {
				result = new ImmutableHugeArrayList<E>(getMemoryManager(), getElementConverter(), isFaster(), getCapacity());
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
			ImmutableHugeArrayList<E> list = getList();
			list.addInternal(list.size(), element);
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
		public ImmutableHugeArrayList<E> build() {
			if (built) {
				throw new IllegalStateException("Has already been built.");
			}
			built = true;
			return getList();
		}
	}
}
