package ch.obermuhlner.jhuge.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.builder.AbstractHugeSetBuilder;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.memory.MemoryManager;

/**
 * An immutable {@link Set} that stores elements in a {@link MemoryManager}.
 * 
 * <p>The mutating operations throw {@link UnsupportedOperationException}:</p>
 * <ul>
 * <li> {@link #add(Object)}</li>
 * <li> {@link #addAll(Collection)}</li>
 * <li> {@link #remove(Object)}</li>
 * <li> {@link #removeAll(Collection)}</li>
 * <li> {@link #clear()}</li>
 * <li> {@link Iterator#remove()} when iterating over this collection</li>
s * </ul>
 * 
 * <p>In order to create an {@link ImmutableHugeHashSet} you must add the elements in the {@link Builder}.</p>
 * 
 * <p>Other than the mutating operations all semantics, memory consumption and performance are identical to {@link HugeHashSet}.</p>
 * 
 * @param <E> the type of elements
 * @see HugeHashSet
 */
public class ImmutableHugeHashSet<E> extends AbstractHugeHashSet<E> {

	private ImmutableHugeHashSet(MemoryManager memoryManager, Converter<E> converter, boolean faster, int capacity) {
		super(memoryManager, converter, faster, capacity);
	}

	@Override
	public Iterator<E> iterator() {
		return new ImmutableHugeHashSetIterator();
	}

	private class ImmutableHugeHashSetIterator extends AbstractHugeHashSetIterator {
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}	
	
	/**
	 * Builds an {@link ImmutableHugeHashSet}.
	 *
	 * @param <E> the type of elements
	 */
	public static class Builder<E> extends AbstractHugeSetBuilder<E> {
		
		private ImmutableHugeHashSet<E> result;
		
		private boolean built;
		
		private ImmutableHugeHashSet<E> getSet() {
			if (result == null) {
				result = new ImmutableHugeHashSet<E>(getMemoryManager(), getElementConverter(), isFaster(), getCapacity());
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
		public Builder<E> compressElement() {
			super.compressElement();
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
			getSet().addInternal(element);
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
		public ImmutableHugeHashSet<E> build() {
			if (built) {
				throw new IllegalStateException("Has already been built.");
			}
			built = true;
			return getSet();
		}
	}	
}
