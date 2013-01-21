package ch.obermuhlner.jhuge.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

/**
 * Uses a customized compact representation to serialize an object and back.
 * 
 * If no customized compact representation exists, it uses Java {@link Serializable serialization} as fallback strategy. 
 *
 * @param <T> the type of the object to convert
 */
public class CompactConverter<T extends Serializable> extends SerializableConverter<T> {

	private static final byte NULL = 0;
	private static final byte SERIALIZED = 1;
	private static final byte BYTE = 2;
	private static final byte SHORT = 3;
	private static final byte INT = 4;
	private static final byte LONG = 5;
	private static final byte FLOAT = 6;
	private static final byte DOUBLE = 7;
	private static final byte CHAR = 8;
	private static final byte BIGDECIMAL = 9;
//	private static final byte STRING = 10;
//	private static final byte DATE = 11;
	private static final byte ARRAY_BYTE = 12;
	private static final byte ARRAY_SHORT = 13;
	private static final byte ARRAY_INT = 14;
	private static final byte ARRAY_LONG = 15;
	private static final byte ARRAY_CHAR = 18;

	/**
	 * Constructs a converter that uses the standard Java class loader to deserialize the object.
	 */
	public CompactConverter() {
		super(null);
	}

	/**
	 * Constructs a converter that uses the specified {@link ClassLoader} to deserialize the object.
	 * 
	 * @param classLoader the {@link ClassLoader} to create deserialized object instances, or <code>null</code> to use the standard Java class loader.
	 */
	public CompactConverter(ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	public byte[] serialize(T source) {
		if (source == null) {
			return new byte[] {
					NULL
					};
		}

		Class<? extends Serializable> clazz = source.getClass();
		if (clazz == Byte.class) {
			return new byte[] {
					BYTE,
					((Byte) source).byteValue()
					};
		}
		if (clazz == Short.class) {
			short value = ((Short) source).shortValue();
			return new byte[] {
					SHORT,
					(byte) (value >>> 8),
					(byte) (value)
					};
		}
		if (clazz == Integer.class) {
			int value = ((Integer) source).intValue();
			return new byte[] {
					INT,
					(byte) (value >>> 24),
					(byte) (value >>> 16),
					(byte) (value >>> 8),
					(byte) (value)
					};
		}
		if (clazz == Long.class) {
			long value = ((Long) source).longValue();
			return new byte[] {
					LONG,
					(byte) (value >>> 56),
					(byte) (value >>> 48),
					(byte) (value >>> 40),
					(byte) (value >>> 32),
					(byte) (value >>> 24),
					(byte) (value >>> 16),
					(byte) (value >>> 8),
					(byte) (value)
					};
		}
		if (clazz == Character.class) {
			char value = ((Character) source).charValue();
			return new byte[] {
					CHAR,
					(byte) (value >> 8),
					(byte) (value)
					};
		}
		if (clazz == Double.class) {
			long value = Double.doubleToRawLongBits(((Double) source).doubleValue());
			return new byte[] {
					DOUBLE,
					(byte) (value >>> 56),
					(byte) (value >>> 48),
					(byte) (value >>> 40),
					(byte) (value >>> 32),
					(byte) (value >>> 24),
					(byte) (value >>> 16),
					(byte) (value >>> 8),
					(byte) (value)
					};
		}
		if (clazz == Float.class) {
			int value = Float.floatToRawIntBits(((Float) source).floatValue());
			return new byte[] {
					FLOAT,
					(byte) (value >>> 24),
					(byte) (value >>> 16),
					(byte) (value >>> 8),
					(byte) (value)
					};
		}
		if (clazz == BigDecimal.class) {
			BigDecimal value = ((BigDecimal) source);
			ByteArrayOutputStream aByteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(aByteArrayOutputStream);
			try {
				stream.writeByte(BIGDECIMAL);
				stream.writeUTF(value.toString());
				stream.close();
				return aByteArrayOutputStream.toByteArray();
			}
			catch (IOException exception) {
				// ignore
			}
		}
		if (clazz == byte[].class) {
			byte[] value = (byte[]) source;
			byte[] compactData = new byte[1 + 4 + value.length];
			ByteBuffer byteBuffer = ByteBuffer.wrap(compactData);
			byteBuffer.put(ARRAY_BYTE);
			byteBuffer.putInt(value.length);
			byteBuffer.put(value);
			return compactData;
		}
		if (clazz == short[].class) {
			short[] value = (short[]) source;
			byte[] compactData = new byte[1 + 4 + value.length * 2];
			ByteBuffer byteBuffer = ByteBuffer.wrap(compactData);
			byteBuffer.put(ARRAY_SHORT);
			byteBuffer.putInt(value.length);
			for (int i = 0; i < value.length; i++) {
				byteBuffer.putShort(value[i]);
			}
			return compactData;
		}
		if (clazz ==  int[].class) {
			int[] value = (int[]) source;
			byte[] compactData = new byte[1 + 4 + value.length * 4];
			ByteBuffer byteBuffer = ByteBuffer.wrap(compactData);
			byteBuffer.put(ARRAY_INT);
			byteBuffer.putInt(value.length);
			for (int i = 0; i < value.length; i++) {
				byteBuffer.putInt(value[i]);
			}
			return compactData;
		}
		if (clazz ==  long[].class) {
			long[] value = (long[]) source;
			byte[] compactData = new byte[1 + 4 + value.length * 8];
			ByteBuffer byteBuffer = ByteBuffer.wrap(compactData);
			byteBuffer.put(ARRAY_LONG);
			byteBuffer.putInt(value.length);
			for (int i = 0; i < value.length; i++) {
				byteBuffer.putLong(value[i]);
			}
			return compactData;
		}
		if (clazz == char[].class) {
			char[] value = (char[]) source;
			byte[] compactData = new byte[1 + 4 + value.length * 2];
			ByteBuffer byteBuffer = ByteBuffer.wrap(compactData);
			byteBuffer.put(ARRAY_CHAR);
			byteBuffer.putInt(value.length);
			for (int i = 0; i < value.length; i++) {
				byteBuffer.putChar(value[i]);
			}
			return compactData;
		}
		
		byte[] serializedData = super.serialize(source);
		byte[] compactData = new byte[serializedData.length + 1];
		compactData[0] = SERIALIZED;
		System.arraycopy(serializedData, 0, compactData, 1, serializedData.length);
		return compactData;
	}
	
	@Override
	public T deserialize(byte[] data) {
		if (data == null || data.length == 0) {
			return null;
		}

		byte type = data[0];
		switch (type) {
		case NULL: {
			return null;
		}
		case BYTE: {
			byte value = data[1];
			@SuppressWarnings("unchecked")
			T result = (T) Byte.valueOf(value);
			return result;
		}
		case SHORT: {
			short value = (short) (
					((data[1] & 0xff) << 8) + 
					(data[2] & 0xff));
			@SuppressWarnings("unchecked")
			T result = (T) Short.valueOf(value);
			return result;
		}
		case INT: {
			int value = (
					((data[1] & 0xff) << 24) + 
					((data[2] & 0xff) << 16) + 
					((data[3] & 0xff) << 8) + 
					(data[4] & 0xff));
			@SuppressWarnings("unchecked")
			T result = (T) Integer.valueOf(value);
			return result;
		}
		case LONG: {
			long value = (
					((long) data[1] << 56) +
					((long) (data[2] & 255) << 48) +
					((long) (data[3] & 255) << 40) +
					((long) (data[4] & 255) << 32) +
					((long) (data[5] & 255) << 24) +
					((data[6] & 255) << 16) +
					((data[7] & 255) << 8) +
					((data[8] & 255) << 0));

			@SuppressWarnings("unchecked")
			T result = (T) Long.valueOf(value);
			return result;
		}
		case CHAR: {
			char value = (char) (
					((data[1] & 0xff) << 8) +
					(data[2] & 0xff));

			@SuppressWarnings("unchecked")
			T result = (T) Character.valueOf(value);
			return result;
		}
		case FLOAT: {
			int value = (
					((data[1] & 0xff) << 24) +
					((data[2] & 0xff) << 16) +
					((data[3] & 0xff) << 8) +
					(data[4] & 0xff));
			@SuppressWarnings("unchecked")
			T result = (T) Float.valueOf(Float.intBitsToFloat(value));
			return result;
		}
		case DOUBLE: {
			long value = (
					((long) data[1] << 56) +
					((long) (data[2] & 0xff) << 48) +
					((long) (data[3] & 0xff) << 40) +
					((long) (data[4] & 0xff) << 32) +
					((long) (data[5] & 0xff) << 24) +
					((data[6] & 0xff) << 16) +
					((data[7] & 0xff) << 8) +
					((data[8] & 255) << 0));
			@SuppressWarnings("unchecked")
			T result = (T) Double.valueOf(Double.longBitsToDouble(value));
			return result;
		}
		case BIGDECIMAL: {
			DataInputStream aStream = new DataInputStream(new ByteArrayInputStream(data, 1, data.length - 1));
			try {
				String aString = aStream.readUTF();
				@SuppressWarnings("unchecked")
				T result = (T) new BigDecimal(aString);
				return result;
			} catch (IOException exception) {
				throw new IllegalArgumentException("Failed to parse bigdecimal string representation");
			}

		}
		case ARRAY_BYTE: {
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.get(); // type
			int length = byteBuffer.getInt();
			byte[] array = new byte[length];
			byteBuffer.get(array, 0, length);
			@SuppressWarnings("unchecked")
			T result = (T) array;
			return result;
		}
		case ARRAY_SHORT: {
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.get(); // type
			int length = byteBuffer.getInt();
			short[] array = new short[length];
			for (int i = 0; i < length; i++) {
				array[i] = byteBuffer.getShort();
			}
			@SuppressWarnings("unchecked")
			T result = (T) array;
			return result;
		}
		case ARRAY_INT: {
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.get(); // type
			int length = byteBuffer.getInt();
			int[] array = new int[length];
			for (int i = 0; i < length; i++) {
				array[i] = byteBuffer.getInt();
			}
			@SuppressWarnings("unchecked")
			T result = (T) array;
			return result;
		}
		case ARRAY_LONG: {
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.get(); // type
			int length = byteBuffer.getInt();
			long[] array = new long[length];
			for (int i = 0; i < length; i++) {
				array[i] = byteBuffer.getLong();
			}
			@SuppressWarnings("unchecked")
			T result = (T) array;
			return result;
		}
		case ARRAY_CHAR: {
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.get(); // type
			int length = byteBuffer.getInt();
			char[] array = new char[length];
			for (int i = 0; i < length; i++) {
				array[i] = byteBuffer.getChar();
			}
			@SuppressWarnings("unchecked")
			T result = (T) array;
			return result;
		}
		case SERIALIZED: {
			byte[] bytes = new byte[data.length - 1];
			System.arraycopy(data, 1, bytes, 0, data.length - 1);
			return super.deserialize(bytes);
		}
		default:
			throw new IllegalArgumentException("Unknown compact converter type: " + type);
		}
	}
}
