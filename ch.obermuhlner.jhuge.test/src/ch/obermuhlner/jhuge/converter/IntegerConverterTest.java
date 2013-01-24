package ch.obermuhlner.jhuge.converter;

import org.junit.Test;

/**
 * Tests {@link IntegerConverter}.
 */
@SuppressWarnings("javadoc")
public class IntegerConverterTest {

	@Test
	public void testConvert_Integer() {
		Converter<Integer> converter = new IntegerConverter();
		AbstractSerializableConverterTest.assertConvert(converter, Integer.valueOf(0));
		AbstractSerializableConverterTest.assertConvert(converter, Integer.valueOf(1234));
		AbstractSerializableConverterTest.assertConvert(converter, Integer.valueOf(-4321));
		AbstractSerializableConverterTest.assertConvert(converter, Integer.MIN_VALUE);
		AbstractSerializableConverterTest.assertConvert(converter, Integer.MAX_VALUE);
	}	
}
