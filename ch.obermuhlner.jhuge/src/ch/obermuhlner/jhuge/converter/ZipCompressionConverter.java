package ch.obermuhlner.jhuge.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Decorates another {@link Converter} to compress and de-compress converted objects using the ZIP algorithm. 
 * 
 * <p>The {@link ZipCompressionConverter} relies on another {@link Converter} to do the actual conversion
 * and simply compresses the output of the underlying {@link Converter#serialize(Object)},
 * respectively de-compresses before calling {@link Converter#deserialize(byte[])}.</p>
 * 
 * <p>The compressed representation has some overhead (an empty compressed string uses 127 bytes) and
 * the {@link ZipCompressionConverter} should only be used if measurements with typical data show significant reduction.</p>
 * 
 * @param <T> the type of the object to convert
 */
public class ZipCompressionConverter<T> implements Converter<T> {

	private static final byte UNCOMPRESSED = 0;
	private static final byte COMPRESSED = 1;
	
	private final Converter<T> converter;
	private final boolean auto;
	
	/**
	 * Creates a {@link ZipCompressionConverter}.
	 * 
	 * @param converter the {@link Converter} to actually convert the objects
	 */
	public ZipCompressionConverter(Converter<T> converter) {
		this(converter, true);
	}
	
	/**
	 * Creates a {@link ZipCompressionConverter}.
	 * 
	 * @param converter the {@link Converter} to actually convert the objects
	 * @param auto <code>true</code> to compress only if compressed data is smaller than uncompressed, <code>false</code> to always compress
	 */
	public ZipCompressionConverter(Converter<T> converter, boolean auto) {
		this.converter = converter;
		this.auto = auto;
	}
	
	@Override
	public int serializedLength() {
		return -1;
	}

	@Override
	public byte[] serialize(T element) {
		byte[] data = converter.serialize(element);
		return compress(data);
	}

	@Override
	public T deserialize(byte[] data) {
		byte[] uncompressedData = decompress(data);
		return converter.deserialize(uncompressedData);
	}

	private byte[] compress(byte[] data) {
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(1024);
			if (auto) {
				byteOutputStream.write(COMPRESSED);
			}
			ZipOutputStream zipOutputStream = new ZipOutputStream(byteOutputStream);
			zipOutputStream.putNextEntry(new ZipEntry(""));
			zipOutputStream.write(data, 0, data.length);
			zipOutputStream.closeEntry();
			zipOutputStream.close();
			byte[] compressedData = byteOutputStream.toByteArray();
			
			if (auto) {
				// TODO consider a threshold percentage
				if (data.length <= compressedData.length) {
					// original data is smaller than compressed
					byteOutputStream.reset();
					byteOutputStream.write(UNCOMPRESSED);
					byteOutputStream.write(data);
					return byteOutputStream.toByteArray();
				}
			}
			
			return compressedData;
		} catch (IOException exception) {
			throw new IllegalArgumentException(exception);
		}
	}

	private byte[] decompress(byte[] data) {
		try {
			if (auto) {
				if (data[0] == UNCOMPRESSED) {
					byte[] data2 = new byte[data.length - 1];
					System.arraycopy(data, 1, data2, 0, data.length - 1);
					return data2;
				}
			}
			
			ByteArrayInputStream byteInputStream = auto ? new ByteArrayInputStream(data, 1, data.length - 1) : new ByteArrayInputStream(data);
			
			ZipInputStream zipInputStream = new ZipInputStream(byteInputStream);
			zipInputStream.getNextEntry();
			
			byte[] buffer = new byte[1024];
			
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(1024);
			int length = 0;
			while ((length = zipInputStream.read(buffer, 0, buffer.length)) >= 0) {
				byteOutputStream.write(buffer, 0, length);
			}
			
			byte[] uncompressedData = byteOutputStream.toByteArray();
			return uncompressedData;
		} catch (IOException exception) {
			throw new IllegalArgumentException(exception);
		}
	}
}
