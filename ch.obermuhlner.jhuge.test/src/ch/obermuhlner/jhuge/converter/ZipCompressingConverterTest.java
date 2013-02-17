package ch.obermuhlner.jhuge.converter;

import java.io.Serializable;

import org.junit.Test;

/**
 * Tests {@link CompactConverter}.
 */
@SuppressWarnings("javadoc")
public class ZipCompressingConverterTest extends AbstractSerializableConverterTest {

	@Override
	protected <T extends Serializable> Converter<T> createConverter() {
		return new ZipCompressionConverter<T>(new SerializableConverter<T>(null));
	}
	
	@Test
	public void testAuto() {
		final boolean auto = true;
		ZipCompressionConverter<Serializable> converter = new ZipCompressionConverter<Serializable>(new SerializableConverter<Serializable>(null), auto);
		
		assertConvert(converter, "small string");
		assertConvert(converter, createLargeString());
	}

	@Test
	public void testNotAuto() {
		final boolean auto = false;
		ZipCompressionConverter<Serializable> converter = new ZipCompressionConverter<Serializable>(new SerializableConverter<Serializable>(null), auto);
		
		assertConvert(converter, "small string");
		assertConvert(converter, createLargeString());
	}

	private String createLargeString() {
		StringBuilder result = new StringBuilder();
		
		result.append("This is a ");
		for (int i = 0; i < 1000; i++) {
			result.append("long ");
		}
		result.append("text.");
		
		return result.toString();
	}
}
