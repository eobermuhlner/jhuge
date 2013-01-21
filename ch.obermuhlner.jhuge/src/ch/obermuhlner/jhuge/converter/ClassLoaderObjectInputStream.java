package ch.obermuhlner.jhuge.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Special {@link ObjectInputStream} using a specific {@link ClassLoader} to create object instances. 
 *
 */
public class ClassLoaderObjectInputStream extends ObjectInputStream {

	private final ClassLoader classLoader;

	/**
	 * Creates a {@link ClassLoaderObjectInputStream}.
	 * 
	 * @param inputStream the {@link InputStream} to deserialize
	 * @param classLoader the {@link ClassLoader} to create deserialized object instances, or <code>null</code> to use the standard Java class loader.
     * @throws  IOException if an I/O error occurs while reading stream header
	 */
	public ClassLoaderObjectInputStream(InputStream inputStream, ClassLoader classLoader) throws IOException {
		super(inputStream);
		
		this.classLoader = classLoader;
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass descriptor) throws IOException, ClassNotFoundException {
		if (classLoader == null) {
			return super.resolveClass(descriptor);
		}

		return Class.forName(descriptor.getName(), false, classLoader);
	}
}
