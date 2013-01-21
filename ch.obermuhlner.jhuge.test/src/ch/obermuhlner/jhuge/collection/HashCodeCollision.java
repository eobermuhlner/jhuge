package ch.obermuhlner.jhuge.collection;

import java.io.Serializable;

/**
 * Useful to test hash code collisions.
 *
 * @param <T> the type of the object decorated
 */
public class HashCodeCollision<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final T data;
	
	HashCodeCollision(T data) {
		this.data= data;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof HashCodeCollision)) {
			return false;
		}
		
		HashCodeCollision<?> other = (HashCodeCollision<?>) obj;
		
		return data == null ? other.data == null : data.equals(other.data);
	}
	
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	public String toString() {
		return data + "@" + hashCode();
	}
}