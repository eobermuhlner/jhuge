package ch.obermuhlner.jhuge.example;

import java.io.Serializable;

/**
 * Serializable class without fields.
 * 
 * <p>Used for measuring memory impact of serialized instances.</p>
 */
public class EmptySerializable implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}