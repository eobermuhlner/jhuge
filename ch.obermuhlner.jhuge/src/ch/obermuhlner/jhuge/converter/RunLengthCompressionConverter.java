package ch.obermuhlner.jhuge.converter;

import java.io.ByteArrayOutputStream;

/**
 * Decorates another {@link Converter} to compress and de-compress converted objects using a run-length algorithm. 
 * 
 * @param <T> the type of the object to convert
 */
public class RunLengthCompressionConverter<T> implements Converter<T> {

	/**
	 * The default value of the escape byte used to escape run length sequences.
	 */
	public static final byte DEFAULT_ESCAPE = -47;

	private final Converter<T> converter;

	private byte escape;
	
	/**
	 * Construct a {@link RunLengthCompressionConverter}.
	 * 
	 * @param converter the underlying {@link Converter}
	 */
	public RunLengthCompressionConverter(Converter<T> converter) {
		this(converter, DEFAULT_ESCAPE);
	}
	
	/**
	 * Construct a {@link RunLengthCompressionConverter} using the specified escape byte.
	 * 
	 * @param converter the underlying {@link Converter}
	 * @param escape the escape byte to encode run length sequences
	 */
	public RunLengthCompressionConverter(Converter<T> converter, byte escape) {
		this.converter = converter;
		this.escape = escape;
	}
	
	@Override
	public int serializedLength() {
		return -1;
	}

	@Override
	public byte[] serialize(T element) {
		byte[] data = converter.serialize(element);
		
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(1024);
		
		byte currentByte = 0;
		int currentRunLength = 0;
		int n = data.length;
		for (int i = 0; i < n; i++) {
			byte nextByte = data[i];
			if (currentByte == nextByte && currentRunLength < 0xff) {
				// same byte - continue counting how many
				// this also handles the case when the first byte is 0 by incrementing currentRunLength to 1
				currentRunLength++;
			} else {
				// different byte
				writeRunLengthBytes(byteOutputStream, currentByte, currentRunLength);
				
				currentByte = nextByte;
				currentRunLength = 1;
			}
		}

		writeRunLengthBytes(byteOutputStream, currentByte, currentRunLength);

		byte[] compressedData = byteOutputStream.toByteArray();
		
		return compressedData;
	}

	private void writeRunLengthBytes(ByteArrayOutputStream byteOutputStream,
			byte currentByte, int currentRunLength) {
		if (currentByte != escape && currentRunLength <= 2) {
			for (int i = 0; i < currentRunLength; i++) {
				byteOutputStream.write(currentByte);
			}
		} else if (currentRunLength > 0) {
			byteOutputStream.write(escape);
			byteOutputStream.write(currentRunLength);
			byteOutputStream.write(currentByte);
		}
	}

	@Override
	public T deserialize(byte[] data) {

		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(1024);

		int n = data.length;
		for (int i = 0; i < n; i++) {
			byte currentByte = data[i];
			if (currentByte == escape) {
				int runLength = data[++i] & 0xff;
				currentByte = data[++i];
				for (int j = 0; j < runLength; j++) {
					byteOutputStream.write(currentByte);
				}
			} else {
				byteOutputStream.write(currentByte);
			}
		}

		byte[] decompressedData = byteOutputStream.toByteArray();
		
		return converter.deserialize(decompressedData);
	}

}
