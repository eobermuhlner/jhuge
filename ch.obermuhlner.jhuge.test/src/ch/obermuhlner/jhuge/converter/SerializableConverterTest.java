package ch.obermuhlner.jhuge.converter;

import java.io.Serializable;

import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.converter.SerializableConverter;

/**
 * Tests {@link SerializableConverter}
 */
public class SerializableConverterTest extends AbstractSerializableConverterTest {

	@Override
	protected <T extends Serializable> Converter<T> createConverter() {
		return new SerializableConverter<T>(null);
	}

}
