package ch.obermuhlner.jhuge.collection.builder;

import java.util.Collection;
import java.util.Set;

/**
 * Builds a {@link Set}.
 * 
 * <p>The concrete implementations of this API are typically inner classes of the collection class they are building.</p>
 *
 * @param <E> the type of the element in the {@link Set}
 */
public interface SetBuilder<E> extends CollectionBuilder<E> {
	
	/**
	 * {@inheritDoc}
	 * <p>Elements that have {@link Object#equals(Object) already} been added will be ignored.</p>
	 */
	@Override
	SetBuilder<E> add(E element);
	
	/**
	 * {@inheritDoc}
	 * <p>Elements that have {@link Object#equals(Object) already} been added will be ignored.</p>
	 */
	@Override
	SetBuilder<E> addAll(Collection<E> elements);

	/**
	 * {@inheritDoc}
	 * <p>Elements that have {@link Object#equals(Object) already} been added will be ignored.</p>
	 */
	@Override
	SetBuilder<E> addAll(E... elements);

	@Override
	Set<E> build();
}
