package ch.obermuhlner.jhuge.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;

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
	private static final byte BOOLEAN = 9;
	private static final byte STRING = 20;
	private static final byte BIGINTEGER = 21;
	private static final byte BIGDECIMAL = 22;
	private static final byte DATE = 23;
	
	private static final byte ARRAY_BYTE = -BYTE;
	private static final byte ARRAY_SHORT = -SHORT;
	private static final byte ARRAY_INT = -INT;
	private static final byte ARRAY_LONG = -LONG;
	private static final byte ARRAY_FLOAT = -FLOAT;
	private static final byte ARRAY_DOUBLE = -DOUBLE;
	private static final byte ARRAY_CHAR = -CHAR;
	private static final byte ARRAY_BOOLEAN = -BOOLEAN;
	private static final byte ARRAY_BIGDECIMAL = -BIGDECIMAL;
	private static final byte ARRAY_STRING = -STRING;

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
			byte value = ((Byte) source).byteValue();
			return new byte[] {
					BYTE,
					value
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
		if (clazz == Boolean.class) {
			Boolean value = (Boolean) source;
			return new byte[] {
					BOOLEAN,
					((byte) (value == Boolean.TRUE ? 1 : 0))
					};
		}
		if (clazz == BigInteger.class) {
			BigInteger value = (BigInteger) source;
			ByteArrayOutputStream aByteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(aByteArrayOutputStream);
			try {
				stream.writeByte(BIGINTEGER);
				stream.writeUTF(value.toString());
				stream.close();
				return aByteArrayOutputStream.toByteArray();
			}
			catch (IOException exception) {
				// ignore
			}
		}
		if (clazz == BigDecimal.class) {
			BigDecimal value = (BigDecimal) source;
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
		if (clazz == String.class) {
			String value = (String) source;
			ByteArrayOutputStream aByteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(aByteArrayOutputStream);
			try {
				stream.writeByte(STRING);
				stream.writeUTF(value);
				stream.close();
				return aByteArrayOutputStream.toByteArray();
			}
			catch (IOException exception) {
				// ignore
			}
		}
		if (clazz == Date.class) {
			Date value = ((Date) source);
			long time = value.getTime();
			return new byte[] {
					DATE,
					(byte) (time >>> 56),
					(byte) (time >>> 48),
					(byte) (time >>> 40),
					(byte) (time >>> 32),
					(byte) (time >>> 24),
					(byte) (time >>> 16),
					(byte) (time >>> 8),
					(byte) (time)
					};
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
		if (clazz ==  float[].class) {
			float[] value = (float[]) source;
			byte[] compactData = new byte[1 + 4 + value.length * 4];
			ByteBuffer byteBuffer = ByteBuffer.wrap(compactData);
			byteBuffer.put(ARRAY_FLOAT);
			byteBuffer.putInt(value.length);
			for (int i = 0; i < value.length; i++) {
				byteBuffer.putFloat(value[i]);
			}
			return compactData;
		}
		if (clazz ==  double[].class) {
			double[] value = (double[]) source;
			byte[] compactData = new byte[1 + 4 + value.length * 8];
			ByteBuffer byteBuffer = ByteBuffer.wrap(compactData);
			byteBuffer.put(ARRAY_DOUBLE);
			byteBuffer.putInt(value.length);
			for (int i = 0; i < value.length; i++) {
				byteBuffer.putDouble(value[i]);
			}
			return compactData;
		}
		if (clazz == boolean[].class) {
			boolean[] value = (boolean[]) source;
			int compactLength = (value.length + 7) / 8;
			byte[] compactData = new byte[1 + 4 + compactLength];
			ByteBuffer byteBuffer = ByteBuffer.wrap(compactData);
			byteBuffer.put(ARRAY_BOOLEAN);
			byteBuffer.putInt(value.length);
			byte currentByte = 0;
			for (int i = 0; i < value.length; i++) {
				int moduloIndex = i % 8;
				if (value[i]) {
					currentByte |= 1 << moduloIndex; 
				}
				if (i == 7 || i == value.length - 1) {
					byteBuffer.put(currentByte);
					currentByte = 0;
				}
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
		if (clazz == BigDecimal[].class) {
			BigDecimal[] value = (BigDecimal[]) source;
			ByteArrayOutputStream aByteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(aByteArrayOutputStream);
			try {
				stream.writeByte(ARRAY_BIGDECIMAL);
				stream.writeInt(value.length);
				for (int i = 0; i < value.length; i++) {
					BigDecimal element = value[i];
					if (element == null) {
						stream.writeByte(NULL);
					} else if (element.getClass() == BigDecimal.class){
						stream.writeByte(BIGDECIMAL);
						stream.writeUTF(element.toString());
					} else {
						stream.writeByte(SERIALIZED);
						serialize(element, stream);
					}
				}
				stream.close();
				return aByteArrayOutputStream.toByteArray();
			}
			catch (IOException exception) {
				// ignore
			}
		}
		if (clazz == String[].class) {
			String[] value = (String[]) source;
			ByteArrayOutputStream aByteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(aByteArrayOutputStream);
			try {
				stream.writeByte(ARRAY_STRING);
				stream.writeInt(value.length);
				for (int i = 0; i < value.length; i++) {
					String element = value[i];
					if (element == null) {
						stream.writeByte(NULL);
					} else {
						stream.writeByte(STRING);
						stream.writeUTF(element);
					}
				}
				stream.close();
				return aByteArrayOutputStream.toByteArray();
			}
			catch (IOException exception) {
				// ignore
			}
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
					((long) (data[6] & 255) << 16) +
					((long) (data[7] & 255) << 8) +
					((long) (data[8] & 255) << 0));

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
		case BOOLEAN: {
			byte value = data[1];
			@SuppressWarnings("unchecked")
			T result = (T) (value == 0 ? Boolean.FALSE : Boolean.TRUE);
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
					((long) (data[6] & 0xff) << 16) +
					((long) (data[7] & 0xff) << 8) +
					((long) (data[8] & 0xff) << 0));
			@SuppressWarnings("unchecked")
			T result = (T) Double.valueOf(Double.longBitsToDouble(value));
			return result;
		}
		case BIGINTEGER: {
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data, 1, data.length - 1));
			try {
				String string = stream.readUTF();
				@SuppressWarnings("unchecked")
				T result = (T) new BigInteger(string);
				return result;
			} catch (IOException exception) {
				throw new IllegalArgumentException("Failed to parse biginteger string representation");
			}
		}
		case BIGDECIMAL: {
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data, 1, data.length - 1));
			try {
				String string = stream.readUTF();
				@SuppressWarnings("unchecked")
				T result = (T) new BigDecimal(string);
				return result;
			} catch (IOException exception) {
				throw new IllegalArgumentException("Failed to parse bigdecimal string representation");
			}
		}
		case STRING: {
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data, 1, data.length - 1));
			try {
				String string = stream.readUTF();
				@SuppressWarnings("unchecked")
				T result = (T) string;
				return result;
			} catch (IOException exception) {
				throw new IllegalArgumentException("Failed to parse string representation");
			}
		}
		case DATE: {
			long time = (
					((long) data[1] << 56) +
					((long) (data[2] & 0xff) << 48) +
					((long) (data[3] & 0xff) << 40) +
					((long) (data[4] & 0xff) << 32) +
					((long) (data[5] & 0xff) << 24) +
					((long) (data[6] & 0xff) << 16) +
					((long) (data[7] & 0xff) << 8) +
					((long) (data[8] & 0xff) << 0));
			@SuppressWarnings("unchecked")
			T result = (T) new Date(time);
			return result;
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
		case ARRAY_FLOAT: {
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.get(); // type
			int length = byteBuffer.getInt();
			float[] array = new float[length];
			for (int i = 0; i < length; i++) {
				array[i] = byteBuffer.getFloat();
			}
			@SuppressWarnings("unchecked")
			T result = (T) array;
			return result;
		}
		case ARRAY_DOUBLE: {
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.get(); // type
			int length = byteBuffer.getInt();
			double[] array = new double[length];
			for (int i = 0; i < length; i++) {
				array[i] = byteBuffer.getDouble();
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
		case ARRAY_BOOLEAN: {
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			byteBuffer.get(); // type
			int length = byteBuffer.getInt();
			boolean[] array = new boolean[length];
			byte currentByte = 0;
			for (int i = 0; i < length; i++) {
				int moduloIndex = i % 8;
				if (moduloIndex == 0) {
					currentByte = byteBuffer.get();
				}
				array[i] = (currentByte & (1 << moduloIndex)) != 0;
			}
			@SuppressWarnings("unchecked")
			T result = (T) array;
			return result;
		}
		case ARRAY_BIGDECIMAL: {
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data, 1, data.length - 1));
			try {
				int length = stream.readInt();
				BigDecimal[] array = new BigDecimal[length];
				for (int i = 0; i < length; i++) {
					byte elementType = stream.readByte();
					switch(elementType) {
					case NULL:
						break;
					case BIGDECIMAL:
						array[i] = new BigDecimal(stream.readUTF());
						break;
					case SERIALIZED:
						array[i] = deserialize(stream, getClassLoader());
						break;
					default:
						throw new IllegalArgumentException("Unexpected element type " + elementType + " for element[" + i + "]");
					}
				}
				@SuppressWarnings("unchecked")
				T result = (T) array;
				return result;
			} catch (IOException exception) {
				throw new IllegalArgumentException("Failed to parse string[] representation");
			}
		}
		case ARRAY_STRING: {
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data, 1, data.length - 1));
			try {
				int length = stream.readInt();
				String[] array = new String[length];
				for (int i = 0; i < length; i++) {
					byte elementType = stream.readByte();
					switch(elementType) {
					case NULL:
						break;
					case STRING:
						array[i] = stream.readUTF();
						break;
					default:
						throw new IllegalArgumentException("Unexpected element type " + elementType + " for element[" + i + "]");
					}
				}
				@SuppressWarnings("unchecked")
				T result = (T) array;
				return result;
			} catch (IOException exception) {
				throw new IllegalArgumentException("Failed to parse string[] representation");
			}
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
