package ch.obermuhlner.jhuge.converter;

/**
 * Converts an object into a serialized form and from the serialized form back into an object.
 *
 * @param <T> the type of the object to convert
 */
public interface Converter<T> {

	/**
	 * Returns the length of the serialized form (if known).
	 * 
	 * @return the length of the serialized form in bytes if it is always the same, or negative if it varies with the data in the object or cannot be determined in advance. 
	 */
	int serializedLength();
	
	/**
	 * Serializes an object into a byte array.
	 * 
	 * @param element the object to serializable, or <code>null</code>
	 * @return the serialized representation
	 */
	byte[] serialize(T element);
	
	/**
	 * Deserializes a byte array into an object.
	 * 
	 * @param data the serialized representation
	 * @return the deserialized object, or <code>null</code>
	 */
	T deserialize(byte[] data);
}
