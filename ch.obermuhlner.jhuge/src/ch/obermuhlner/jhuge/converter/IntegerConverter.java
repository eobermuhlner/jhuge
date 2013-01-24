package ch.obermuhlner.jhuge.converter;


/**
 * Converts an {@link Integer} into a serialized form and from the serialized form back into an {@link Integer}.
 * 
 * <p>The serialized byte array always has a length of 4 bytes.</p>
 */
public class IntegerConverter implements Converter<Integer> {

	@Override
	public int serializedLength() {
		return 4;
	}
	
	@Override
	public byte[] serialize(Integer source) {
		int value = source;
		return new byte[] {
				(byte) (value >>> 24),
				(byte) (value >>> 16),
				(byte) (value >>> 8),
				(byte) (value) };
	}

	@Override
	public Integer deserialize(byte[] data) {
		int value = (
				((data[0] & 0xff) << 24) +
				((data[1] & 0xff) << 16) +
				((data[2] & 0xff) << 8) +
				(data[3] & 0xff));
		return value;
	}
}
