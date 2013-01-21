package ch.obermuhlner.jhuge.collection.builder;

import java.util.List;

/**
 * Builds a {@link List}.
 * 
 * <p>The concrete implementations of this API are typically inner classes of the collection class they are building.</p>
 *
 * @param <E> the type of the element in the {@link List}
 */
public interface ListBuilder<E> extends CollectionBuilder<E> {
	
	@Override
	List<E> build();
}
