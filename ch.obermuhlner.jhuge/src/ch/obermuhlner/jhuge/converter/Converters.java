package ch.obermuhlner.jhuge.converter;

/**
 * Convenience methods to access different {@link Converter} implementations.
 */
public final class Converters {

	/**
	 * Returns the best {@link Converter} for the specified class.
	 * 
	 * @param clazz the {@link Class} to convert, or <code>null</code> if unknown
	 * @return the best {@link Converter}
	 */
	public static <T> Converter<T> bestConverter(Class<T> clazz) {
		return bestConverter(clazz, clazz == null ? null : clazz.getClassLoader());
	}
	
	/**
	 * Returns the best {@link Converter} for the specified class using the specified {@link ClassLoader}.
	 * 
	 * @param clazz the {@link Class} to convert, or <code>null</code> if unknown
	 * @param classLoader the {@link ClassLoader} to create deserialized object instances, or <code>null</code> to use the standard Java class loader.
	 * @return the best {@link Converter}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Converter<T> bestConverter(Class<T> clazz, ClassLoader classLoader) {
		if (clazz == Integer.class) {
			return (Converter<T>) new IntegerConverter();
		}
		if (clazz == Long.class) {
			return (Converter<T>) new LongConverter();
		}
		return new CompactConverter(classLoader);
	}
}
