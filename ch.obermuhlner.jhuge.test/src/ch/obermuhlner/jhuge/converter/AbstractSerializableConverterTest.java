package ch.obermuhlner.jhuge.converter;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

/**
 * Abstract base class to test {@link Converter}s that accept any {@link Serializable} object.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractSerializableConverterTest {

	protected abstract <T extends Serializable> Converter<T> createConverter();
	
	@Test
	public void testConvert_null() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, null);
	}
	
	@Test
	public void testConvert_Boolean() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, Boolean.TRUE);
		assertConvert(converter, Boolean.FALSE);
	}
	
	@Test
	public void testConvert_Char() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, 'x');
		assertConvert(converter, ' ');
		assertConvert(converter, '\0');
		assertConvert(converter, Character.MIN_VALUE);
		assertConvert(converter, Character.MAX_VALUE);
	}
	
	@Test
	public void testConvert_Byte() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, Byte.valueOf((byte)0));
		assertConvert(converter, Byte.valueOf((byte)12));
		assertConvert(converter, Byte.valueOf((byte)-21));
		assertConvert(converter, Byte.MIN_VALUE);
		assertConvert(converter, Byte.MAX_VALUE);
	}
	
	@Test
	public void testConvert_Short() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, Short.valueOf((byte)0));
		assertConvert(converter, Short.valueOf((byte)123));
		assertConvert(converter, Short.valueOf((byte)-321));
		assertConvert(converter, Short.MIN_VALUE);
		assertConvert(converter, Short.MAX_VALUE);
	}
	
	@Test
	public void testConvert_Integer() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, Integer.valueOf(0));
		assertConvert(converter, Integer.valueOf(1234));
		assertConvert(converter, Integer.valueOf(-4321));
		assertConvert(converter, Integer.MIN_VALUE);
		assertConvert(converter, Integer.MAX_VALUE);
	}
	
	@Test
	public void testConvert_Long() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, Long.valueOf(0));
		assertConvert(converter, Long.valueOf(1234));
		assertConvert(converter, Long.valueOf(-4321));
		assertConvert(converter, Long.MIN_VALUE);
		assertConvert(converter, Long.MAX_VALUE);
	}
	
	@Test
	public void testConvert_Float() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, Float.valueOf(0));
		assertConvert(converter, Float.valueOf(1234.56f));
		assertConvert(converter, Float.valueOf(-4321.01f));
		assertConvert(converter, Float.MIN_VALUE);
		assertConvert(converter, -Float.MIN_VALUE);
		assertConvert(converter, Float.MAX_VALUE);
		assertConvert(converter, -Float.MAX_VALUE);
	}
	
	@Test
	public void testConvert_Double() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, Double.valueOf(0));
		assertConvert(converter, Double.valueOf(1234));
		assertConvert(converter, Double.valueOf(-4321));
		assertConvert(converter, Double.MIN_VALUE);
		assertConvert(converter, -Double.MIN_VALUE);
		assertConvert(converter, Double.MAX_VALUE);
		assertConvert(converter, -Double.MAX_VALUE);
	}
	
	@Test
	public void testConvert_String() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, "");
		assertConvert(converter, "example");
	}

	@Test
	public void testConvert_BigInteger() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, BigInteger.ZERO);
		assertConvert(converter, BigInteger.ONE);
		assertConvert(converter, BigInteger.TEN);
		assertConvert(converter, new BigInteger("1234"));
		assertConvert(converter, new BigInteger("ffff", 16));
		assertConvert(converter, new SubBigInteger("12345678"));
	}
	
	@Test
	public void testConvert_BigDecimal() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, BigDecimal.ZERO);
		assertConvert(converter, BigDecimal.ONE);
		assertConvert(converter, BigDecimal.TEN);
		assertConvert(converter, new BigDecimal("1234.5678"));
		assertConvert(converter, new BigDecimal("1234.5678", MathContext.DECIMAL32));
		assertConvert(converter, new BigDecimal("1234.5678", MathContext.DECIMAL64));
		assertConvert(converter, new BigDecimal("1234.5678", MathContext.DECIMAL128));
		assertConvert(converter, new BigDecimal("1234.5678").setScale(8, RoundingMode.HALF_EVEN));
		assertConvert(converter, new SubBigDecimal("4.4"));
	}

	@Test
	public void testConvert_Date() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new Date());

		Calendar calendar = Calendar.getInstance();
		calendar.set(67, 2, 24);
		assertConvert(converter, calendar.getTime());
	}

	@Test
	public void testConvert_Array_boolean() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new boolean[0]);
		assertConvert(converter, new boolean[] { true, false } );
		assertConvert(converter, new boolean[] { false, false, false, false, false, false, true } ); // 7 elements
		assertConvert(converter, new boolean[] { false, false, false, false, false, false, false, true } ); // 8 elements
		assertConvert(converter, new boolean[] { false, false, false, false, false, false, false, false, true } ); // 9 elements
	}
	
	@Test
	public void testConvert_Array_char() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new char[0]);
		assertConvert(converter, new char[] { 'a', 'b', '\0', 'Z'} );
	}
	
	@Test
	public void testConvert_Array_byte() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new byte[0]);
		assertConvert(converter, new byte[] { -3, 0, 3, Byte.MIN_VALUE, Byte.MAX_VALUE } );
	}
	
	@Test
	public void testConvert_Array_short() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new short[0]);
		assertConvert(converter, new short[] { -3, 0, 3, Short.MIN_VALUE, Short.MAX_VALUE } );
	}
	
	@Test
	public void testConvert_Array_int() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new int[0]);
		assertConvert(converter, new int[] { -3, 0, 3, Integer.MIN_VALUE, Integer.MAX_VALUE } );
	}
	
	@Test
	public void testConvert_Array_long() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new long[0]);
		assertConvert(converter, new long[] { -3, 0, 3, Long.MIN_VALUE, Long.MAX_VALUE } );
	}
	
	@Test
	public void testConvert_Array_float() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new float[0]);
		assertConvert(converter, new float[] { -3.3f, 0, 3.3f, Float.MIN_VALUE, Float.MAX_VALUE } );
	}
	
	@Test
	public void testConvert_Array_double() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new double[0]);
		assertConvert(converter, new double[] { -3.3, 0, 3.3, Double.MIN_VALUE, Double.MAX_VALUE } );
	}
	
	@Test
	public void testConvert_Array_BigDecimal() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new BigDecimal[0]);
		assertConvert(converter, new BigDecimal[] { new BigDecimal("-3.3"), BigDecimal.ZERO, null, new SubBigDecimal("9.9"), new BigDecimal("3.3") } );
	}
	
	@Test
	public void testConvert_Array_String() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new String[0]);
		assertConvert(converter, new String[] { "", "a", null, "XYZ"} );
	}
	
	@Test
	public void testConvert_Array_Date() {
		Converter<Serializable> converter = createConverter();
		assertConvert(converter, new Date[0]);
		assertConvert(converter, new Date[] { new Date(), null } );
	}
	
	private static class SubBigInteger extends BigInteger {
		private static final long serialVersionUID = 1L;

		public SubBigInteger(String value) {
			super(value);
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "{" + super.toString() + "}";
		}
	}
	
	private static class SubBigDecimal extends BigDecimal {
		private static final long serialVersionUID = 1L;

		public SubBigDecimal(String value, MathContext matchContext) {
			super(value, matchContext);
		}

		public SubBigDecimal(String value) {
			super(value);
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "{" + super.toString() + "}";
		}
	}

	/**
	 * Asserts the correct {@link Converter#serialize(Object)} and {@link Converter#deserialize(byte[])} behavior with the specified object instance.
	 * 
	 * @param converter the {@link Converter} to test
	 * @param object the object to convert
	 */
	public static <T> void assertConvert(Converter<T> converter, T object) {
		assertConvert(converter, object, true);
	}
	
	/**
	 * Asserts the correct {@link Converter#serialize(Object)} and {@link Converter#deserialize(byte[])} behavior with the specified object instance.
	 * 
	 * @param converter the {@link Converter} to test
	 * @param object the object to convert
	 * @param strict <code>true</code> to assert that the deserialized object can be serialized again and both byte arrays (first and second serialization) are equal, <code>false</code> to do not test this
	 */
	public static <T> void assertConvert(Converter<T> converter, T object, boolean strict) {
		String desc = "object=" + object;

		int serializedLength = converter.serializedLength();
		
		byte[] data = converter.serialize(object);
		if (serializedLength >= 0) {
			assertEquals(desc, serializedLength, data.length);
		}
		T deserializedObject = converter.deserialize(data);
		if (object != null && object.getClass().isArray()) {
			assertArrayEquals(object, deserializedObject);
			return;
		}
		assertEquals(desc, object, deserializedObject);

		if (strict) {
			byte[] data2 = converter.serialize(deserializedObject);
			assertArrayEquals(data, data2);
		}
	}
	
	public static void assertArrayEquals(Object expected, Object actual) {
		assertArrayEquals("", expected, actual);
	}
	
	public static void assertArrayEquals(String message, Object expected, Object actual) {
		int length = Array.getLength(expected);
		assertEquals(message + "length", length, Array.getLength(actual));
		
		for (int i = 0; i < length; i++) {
			Object expectedElement = Array.get(expected, i);
			Object actualElement = Array.get(actual, i);
			assertEquals(message + " array[" + i + "]", expectedElement, actualElement);
			if (expectedElement != null && actualElement != null) {
				assertEquals(message + " array[" + i + "]", expectedElement.getClass(), actualElement.getClass());
			}
		}
	}
}
