package ch.obermuhlner.jhuge.collection.builder;

import java.util.List;

/**
 * Abstract base class to implement a {@link ListBuilder} for a huge {@link List}.
 *
 * @param <E> the type of the element in the {@link List}
 */
public abstract class AbstractHugeListBuilder<E> extends AbstractHugeCollectionBuilder<E> implements ListBuilder<E> {

}
