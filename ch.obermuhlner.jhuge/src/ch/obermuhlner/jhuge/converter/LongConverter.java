package ch.obermuhlner.jhuge.converter;


/**
 * Converts an {@link Long} into a serialized form and from the serialized form back into an {@link Long}.
 * 
 * <p>The serialized byte array always has a length of 8 bytes.</p>
 */
public class LongConverter implements Converter<Long> {

	@Override
	public int serializedLength() {
		return 8;
	}

	@Override
	public byte[] serialize(Long source) {
		long value = source;
		return new byte[] {
				(byte) (value >>> 56),
				(byte) (value >>> 48),
				(byte) (value >>> 40),
				(byte) (value >>> 32),
				(byte) (value >>> 24),
				(byte) (value >>> 16),
				(byte) (value >>> 8),
				(byte) (value) };
	}
	
	@Override
	public Long deserialize(byte[] data) {
		long value = (
				((long) data[0] << 56) +
				((long) (data[1] & 255) << 48) +
				((long) (data[2] & 255) << 40) +
				((long) (data[3] & 255) << 32) +
				((long) (data[4] & 255) << 24) +
				((data[5] & 255) << 16) +
				((data[6] & 255) << 8) +
				((data[7] & 255) << 0));
		return value;
	}
}
