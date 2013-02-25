package ch.obermuhlner.jhuge.converter;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.junit.Test;

/**
 * Tests {@link CompactConverter}.
 */
public class RunLengthCompressingConverterTest extends AbstractSerializableConverterTest {

	@Override
	protected <T extends Serializable> Converter<T> createConverter() {
		return new RunLengthCompressionConverter<T>(new SerializableConverter<T>(null));
	}

	/**
	 * Tests some corner cases of the {@link RunLengthCompressionConverter}.
	 * 
	 * This is black-box testing.
	 * The tests simply use the official API of the class but specifically use arguments that are known to be corner cases of the implementation.
	 */
	@Test
	public void testCornerCases() {
		RunLengthCompressionConverter<byte[]> converter = new RunLengthCompressionConverter<byte[]>(new CompactConverter<byte[]>(null));

		// various small run lengths
		assertConvert(converter, new byte[0]);
		assertConvert(converter, new byte[1]);
		assertConvert(converter, new byte[2]);
		assertConvert(converter, new byte[3]);
		assertConvert(converter, new byte[4]);

		// run length of 255 is the highest that can be stored in a single byte - check the off-by-one cases
		assertConvert(converter, new byte[254]);
		assertConvert(converter, new byte[255]);
		assertConvert(converter, new byte[256]);
		
		// run length of 1000 cannot be stored in a single byte, needs to be split up
		assertConvert(converter, new byte[1000]); 
		
		// needs escaping the escape byte
		assertConvert(converter, new byte[] { 0, 0, RunLengthCompressionConverter.DEFAULT_ESCAPE, 0, 0 });
	}
	
	/**
	 * Tests providing a custom escape byte.
	 * 
	 * This is gray-box testing.
	 * The converted byte[] is parsed with the knowledge of implementation details of {@link RunLengthCompressionConverter} and {@link CompactConverter}. 
	 */
	@Test
	public void testEscape() {
		final byte escape = -11;
		RunLengthCompressionConverter<byte[]> converter = new RunLengthCompressionConverter<byte[]>(new CompactConverter<byte[]>(null), escape);

		final int n = 10;
		byte[] original = new byte[n];
		byte[] data = converter.serialize(original);

		int i = 0;
		// type ARRAY_CHAR
		assertEquals(-2, data[i++]);
		// array length as int
		assertEquals(escape, data[i++]); // 3 times 0
		assertEquals(3, data[i++]);
		assertEquals(0, data[i++]);
		assertEquals(n, data[i++]);
		// n byte elements
		assertEquals(escape, data[i++]); // n times 0 
		assertEquals(n, data[i++]);
		assertEquals(0, data[i++]);
	}

	/**
	 * Tests that a single value which is the same as escape is escaped correctly (with a run length of 1).
	 * 
	 * This is gray-box testing.
	 * The converted byte[] is parsed with the knowledge of implementation details of {@link RunLengthCompressionConverter} and {@link CompactConverter}. 
	 */
	@Test
	public void testEscapingEscape() {
		RunLengthCompressionConverter<byte[]> converter = new RunLengthCompressionConverter<byte[]>(new CompactConverter<byte[]>(null));

		final int n = 10;
		byte[] original = new byte[n];
		original[5] = RunLengthCompressionConverter.DEFAULT_ESCAPE;
		
		byte[] data = converter.serialize(original);
		
		int i = 0;
		// type ARRAY_CHAR
		assertEquals(-2, data[i++]);
		// array length as int
		assertEquals(RunLengthCompressionConverter.DEFAULT_ESCAPE, data[i++]); // 3 times 0
		assertEquals(3, data[i++]);
		assertEquals(0, data[i++]);
		assertEquals(n, data[i++]);
		// n byte elements
		assertEquals(RunLengthCompressionConverter.DEFAULT_ESCAPE, data[i++]); // 5 times 0 
		assertEquals(5, data[i++]);
		assertEquals(0, data[i++]);
		assertEquals(RunLengthCompressionConverter.DEFAULT_ESCAPE, data[i++]); // 1 times RunLengthCompressingConverter.DEFAULT_ESCAPE 
		assertEquals(1, data[i++]);
		assertEquals(RunLengthCompressionConverter.DEFAULT_ESCAPE, data[i++]);
		assertEquals(RunLengthCompressionConverter.DEFAULT_ESCAPE, data[i++]); // 4 times 0 
		assertEquals(4, data[i++]);
		assertEquals(0, data[i++]);
	}

	/**
	 * Tests run-lengths that do not fit into a single byte.
	 * 
	 * This is gray-box testing.
	 * The converted byte[] is parsed with the knowledge of implementation details of {@link RunLengthCompressionConverter} and {@link CompactConverter}. 
	 */
	@Test
	public void testLongRunLength() {
		RunLengthCompressionConverter<byte[]> converter = new RunLengthCompressionConverter<byte[]>(new CompactConverter<byte[]>(null));

		byte[] original = new byte[1000];
		byte[] data = converter.serialize(original);
		
		int i = 0;
		// type ARRAY_CHAR
		assertEquals(-2, data[i++]);
		// array length as int
		assertEquals(0, data[i++]);
		assertEquals(0, data[i++]);
		assertEquals((byte) (1000 >> 8), data[i++]);
		assertEquals((byte) (1000 & 0xff), data[i++]);
		// n byte elements
		assertEquals(RunLengthCompressionConverter.DEFAULT_ESCAPE, data[i++]); // 255 times 0 
		assertEquals((byte) 255, data[i++]);
		assertEquals(0, data[i++]);
		assertEquals(RunLengthCompressionConverter.DEFAULT_ESCAPE, data[i++]); // 255 times 0 
		assertEquals((byte) 255, data[i++]);
		assertEquals(0, data[i++]);
		assertEquals(RunLengthCompressionConverter.DEFAULT_ESCAPE, data[i++]); // 255 times 0 
		assertEquals((byte) 255, data[i++]);
		assertEquals(0, data[i++]);
		assertEquals(RunLengthCompressionConverter.DEFAULT_ESCAPE, data[i++]); // 235 times 0 
		assertEquals((byte) 235, data[i++]);
		assertEquals(0, data[i++]);
		
		assertEquals(1000, 255 + 255 + 255 + 235);
	}
}
