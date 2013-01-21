package ch.obermuhlner.jhuge.converter;

import java.io.Serializable;

import ch.obermuhlner.jhuge.converter.CompactConverter;
import ch.obermuhlner.jhuge.converter.Converter;

/**
 * Tests {@link CompactConverter}.
 */
public class CompactConverterTest extends AbstractSerializableConverterTest {

	@Override
	protected <T extends Serializable> Converter<T> createConverter() {
		return new CompactConverter<T>(null);
	}

}
