package ch.obermuhlner.jhuge.collection.builder;

import java.util.Collection;

/**
 * Builds a {@link Collection}.
 * 
 * <p>The concrete implementations of this API are typically inner classes of the collection class they are building.</p>
 *
 * @param <E> the type of the element in the {@link Collection}
 */
public interface CollectionBuilder<E> {
	
	/**
	 * Adds a single element.
	 * 
	 * @param element the element to add (may be <code>null</code> if the {@link Collection} being built supports <code>null</code> elements)
	 * @return the builder, so that calls can be chained
	 */
	CollectionBuilder<E> add(E element);
		
	/**
	 * Adds a collection of elements.
	 * 
	 * @param elements the elements to add
	 * @return the builder, so that calls can be chained
	 */
	CollectionBuilder<E> addAll(Collection<E> elements);
	
	/**
	 * Adds all of the specified elements.
	 * 
	 * @param elements the elements to add (single arguments in the varargs may be <code>null</code> if the {@link Collection} being built supports <code>null</code> elements)
	 * @return the builder, so that calls can be chained
	 */
	CollectionBuilder<E> addAll(E... elements);

	/**
	 * Returns the built {@link Collection}.
	 * 
	 * @return the {@link Collection} containing all the elements added to it
	 */
	Collection<E> build();
}
