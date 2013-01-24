package ch.obermuhlner.jhuge.converter;

import org.junit.Test;

/**
 * Tests {@link LongConverter}.
 */
@SuppressWarnings("javadoc")
public class LongConverterTest {

	@Test
	public void testConvert_Long() {
		Converter<Long> converter = new LongConverter();
		AbstractSerializableConverterTest.assertConvert(converter, Long.valueOf(0));
		AbstractSerializableConverterTest.assertConvert(converter, Long.valueOf(1234));
		AbstractSerializableConverterTest.assertConvert(converter, Long.valueOf(-4321));
		AbstractSerializableConverterTest.assertConvert(converter, Long.MIN_VALUE);
		AbstractSerializableConverterTest.assertConvert(converter, Long.MAX_VALUE);
	}	
}
