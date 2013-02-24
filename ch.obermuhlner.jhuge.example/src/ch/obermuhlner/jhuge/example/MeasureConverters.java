package ch.obermuhlner.jhuge.example;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

import ch.obermuhlner.jhuge.converter.CompactConverter;
import ch.obermuhlner.jhuge.converter.RunLengthCompressionConverter;
import ch.obermuhlner.jhuge.converter.SerializableConverter;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.converter.ZipCompressionConverter;

/**
 * Application to measure the memory consumption of objects serialized with {@link Converter}s.
 */
public class MeasureConverters {

	/**
	 * Starts the converter measurement application.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		measureCompactConverter();
		measureSerializableConverter();
		measureZipConverter();
	}

	private static void measureCompactConverter() {
		CompactConverter<Serializable> compactConverter = new CompactConverter<Serializable>();
		
		System.out.println("## CompactConverter");
		System.out.println();
		
		measureConverter(compactConverter);

		System.out.println();
		System.out.println();
	}

	private static void measureSerializableConverter() {
		SerializableConverter<Serializable> compactConverter = new SerializableConverter<Serializable>(null);
		
		System.out.println("## SerializableConverter");
		System.out.println();
		
		measureConverter(compactConverter);

		System.out.println();
		System.out.println();
	}

	private static void measureZipConverter() {
		CompactConverter<Serializable> compactConverter = new CompactConverter<Serializable>(null);
		ZipCompressionConverter<Serializable> zipConverter = new ZipCompressionConverter<Serializable>(compactConverter);
		RunLengthCompressionConverter<Serializable> runLengthConverter = new RunLengthCompressionConverter<Serializable>(compactConverter);
		
		System.out.println("## Compressing Converters");
		
		System.out.println("To measure the compressing converters the following randomly generated data was used:");
		System.out.println("```");
		System.out.println(createLongString(200));
		System.out.println("```");
		
		System.out.println();

		System.out.println("### CompactConverter");
		System.out.println();

		measureSerializeHeader();
		for (int i = 0; i < 200; i+=10) {
			String string = createLongString(i);
			measureSerialize(compactConverter, string, i + " words, " + string.length() + " characters");
		}

		System.out.println();
		
		System.out.println("### ZipCompressingConverter");
		System.out.println();

		measureSerializeHeader();
		for (int i = 0; i < 200; i+=10) {
			String string = createLongString(i);
			measureSerialize(zipConverter, string, i + " words, " + string.length() + " characters");
		}
		System.out.println();
		
		System.out.println("### RunLengthCompressingConverter");
		System.out.println();

		measureSerializeHeader();
		for (int i = 0; i < 200; i+=10) {
			String string = createLongString(i);
			measureSerialize(runLengthConverter, string, i + " words, " + string.length() + " characters");
		}
		
		System.out.println();
		System.out.println();
	}
	
	private static void measureConverter(Converter<Serializable> converter) {
		measureSerializeHeader();
		
		measureSerialize(converter, null);
		measureSerialize(converter, Byte.MAX_VALUE);
		measureSerialize(converter, Short.MAX_VALUE);
		measureSerialize(converter, Integer.MAX_VALUE);
		measureSerialize(converter, Long.MAX_VALUE);
		measureSerialize(converter, Float.MAX_VALUE);
		measureSerialize(converter, Double.MAX_VALUE);
		measureSerialize(converter, true);
		measureSerialize(converter, 'X');
		measureSerialize(converter, "");
		measureSerialize(converter, "this is a string");
		measureSerialize(converter, new BigInteger("1234567890"));
		measureSerialize(converter, new BigDecimal("3.1415923"));
		measureSerialize(converter, new Date());
		measureSerialize(converter, new EmptySerializable());
		
		measureSerialize(converter, new byte[] { });
		measureSerialize(converter, new byte[] { (byte)1, (byte)2, (byte)3 });
		measureSerialize(converter, new short[] { });
		measureSerialize(converter, new short[] { (short)1, (short)2, (short)3 });
		measureSerialize(converter, new int[] { });
		measureSerialize(converter, new int[] { 1, 2, 3 });
		measureSerialize(converter, new long[] { });
		measureSerialize(converter, new long[] { 1L, 2L, 3L });
		measureSerialize(converter, new float[] { });
		measureSerialize(converter, new float[] { 1.1f, 2.2f, 3.3f });
		measureSerialize(converter, new double[] { });
		measureSerialize(converter, new double[] { 1.1, 2.2, 3.3 });
		measureSerialize(converter, new boolean[] { });
		measureSerialize(converter, new boolean[] { true, false, true});
		measureSerialize(converter, new boolean[] { true, false, true, false, true, true, false, true });
		measureSerialize(converter, new boolean[] { true, false, true, false, true, true, false, true, false });
		measureSerialize(converter, new char[] { });
		measureSerialize(converter, new char[] { 'a', 'b', 'c' });
		measureSerialize(converter, new String[] { });
		measureSerialize(converter, new String[] { "a", "b", "c" });
		measureSerialize(converter, new BigInteger[] { });
		measureSerialize(converter, new BigInteger[] { new BigInteger("1"), new BigInteger("222"), new BigInteger("333") });
		measureSerialize(converter, new BigDecimal[] { });
		measureSerialize(converter, new BigDecimal[] { new BigDecimal("1.1"), new BigDecimal("2.2"), new BigDecimal("3.3") });
		measureSerialize(converter, new Date[] { });
		measureSerialize(converter, new Date[] { new Date(), new Date(), new Date() });
	}

	private static final String[] words = { "this", "is", "a", "selection", "of", "stupid", "words", "but", "they", "can", "be", "repeated", "many", "times", "in", "random", "order", "although", "obviously", "it", "will", "never", "win", "the", "nobel", "price", "for", "literature" };
	
	private static String createLongString(int wordCount) {
		StringBuilder result = new StringBuilder();
		
		Random random = new Random(1);
		
		for (int i = 0; i < wordCount; i++) {
			if (i < words.length) {
				result.append(words[i]);
			} else {
				int r = random.nextInt(100);
				if (r < 80) {
					result.append(words[random.nextInt(words.length)]);
				} else {
					result.append(i);
				}
			}
			result.append(' ');
		}
		
		return result.toString();
	}

	private static void measureSerializeHeader() {
		System.out.printf("%30s | %6s | %s\n", "Type", "Length", "Content");
		System.out.println("-------------------------------|--------|------------------------------");
	}
	
	private static void measureSerialize(Converter<Serializable> converter, Serializable object) {
		measureSerialize(converter, object, null);
	}
	
	private static void measureSerialize(Converter<Serializable> converter, Serializable object, String forcedDescription) {
		String className = object == null ? "null" : object.getClass().getSimpleName();
		String description = forcedDescription == null ? String.valueOf(object) : forcedDescription;
		byte[] data = converter.serialize(object);
		
		if (object != null) {
			if (object.getClass().isArray()) {
				className = object.getClass().getComponentType().getSimpleName() + "[]";
				description = arrayToString(object);
			}
			
		}

		System.out.printf("%-30s |   %4d | %-30s\n", className, data.length, description);
	}

	private static String arrayToString(Serializable object) {
		StringBuilder result = new StringBuilder();
		int length = Array.getLength(object);
		result.append('[');
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				result.append(", ");
			}
			Object element = Array.get(object, i);
			result.append(element);
		}
		result.append(']');
		
		return result.toString();
	}
}
