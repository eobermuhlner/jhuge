package ch.obermuhlner.jhuge.collection.builder;

import java.util.Map;

import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.ZipCompressionConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to implement a {@link MapBuilder} for a huge {@link Map}.
 *
 * @param <K> the type of the key in the {@link Map}
 * @param <V> the type of the value in the {@link Map}
 */
public abstract class AbstractHugeMapBuilder<K, V> implements MapBuilder<K, V> {
	
	private Class<K> keyClass;

	private Class<V> valueClass;

	private ClassLoader classLoader;

	private int bufferSize = 100 * MemoryMappedFileManager.MEGABYTES;

	private Integer blockSize;
	
	private Converter<K> keyConverter;

	private Converter<V> valueConverter;

	private boolean compressKey;
	
	private boolean compressValue;
	
	private MemoryManager memoryManager;
	
	private boolean faster;
	
	private int capacity;
	
	private boolean prepared;

	/**
	 * Specifies the {@link ClassLoader} used to deserialize keys and values.
	 * 
	 * @param classLoader the {@link ClassLoader} used to deserialize keys and values
	 * @return this {@link MapBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> classLoader(ClassLoader classLoader) {
		checkPrepared();
		this.classLoader = classLoader;
		return this;
	}
	
	/**
	 * Specifies the type of the keys.
	 * 
	 * <p>If the {@link #classLoader(ClassLoader) ClassLoader} is not explicitly specified then this class is used to determine the {@link ClassLoader}.</p>
	 * <p>If the {@link #key(Converter) key Converter} is not explicitly specified then this class is used to determine the best key {@link Converter}.</p>
	 * 
	 * @param keyClass the type of the keys
	 * @return this {@link MapBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> key(Class<K> keyClass) {
		checkPrepared();
		this.keyClass = keyClass;
		return this;
	}
	
	/**
	 * Specifies the key {@link Converter} used to serialize/deserialize keys.
	 * 
	 * @param keyConverter the {@link Converter} used to serialize/deserialize keys
	 * @return this {@link MapBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> key(Converter<K> keyConverter) {
		checkPrepared();
		this.keyConverter = keyConverter;
		return this;
	}
	
	/**
	 * Specifies the type of the values.
	 * 
	 * <p>If the {@link #classLoader(ClassLoader) ClassLoader} is not explicitly specified then this class is used to determine the {@link ClassLoader}.</p>
	 * <p>If the {@link #value(Converter) value Converter} is not explicitly specified then this class is used to determine the best value {@link Converter}.</p>
	 * 
	 * @param valueClass the type of the values
	 * @return this {@link MapBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> value(Class<V> valueClass) {
		checkPrepared();
		this.valueClass = valueClass;
		return this;
	}
	
	/**
	 * Specifies the value {@link Converter} used to serialize/deserialize keys.
	 * 
	 * @param valueConverter the {@link Converter} used to serialize/deserialize values
	 * @return this {@link MapBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> value(Converter<V> valueConverter) {
		checkPrepared();
		this.valueConverter = valueConverter;
		return this;
	}
	
	/**
	 * Specifies that the serialized keys should be stored in a compressed form.
	 * 
	 * @return this {@link CollectionBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> compressKey() {
		checkPrepared();
		this.compressKey = true;
		return this;
	}
	
	/**
	 * Specifies that the serialized values should be stored in a compressed form.
	 * 
	 * @return this {@link CollectionBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> compressValue() {
		checkPrepared();
		this.compressValue = true;
		return this;
	}
	
	/**
	 * Specifies the buffer size used in the {@link MemoryMappedFileManager}.
	 * 
	 * @param bufferSize the buffer size
	 * @return this {@link MapBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> bufferSize(int bufferSize) {
		checkPrepared();
		this.bufferSize = bufferSize;
		return this;
	}
	
	/**
	 * Specifies the block size used in the {@link MemoryMappedFileManager}.
	 * 
	 * @param blockSize the block size
	 * @return this {@link MapBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> blockSize(int blockSize) {
		checkPrepared();
		this.blockSize = blockSize;
		return this;
	}

	/**
	 * Specifies the {@link MemoryManager} used to store keys and values.
	 * 
	 * <p>This overrides any setting of {@link #bufferSize(int)} and {@link #blockSize(int)}.</p>
	 * 
	 * @param memoryManager the MemoryManager to store the keys and values 
	 * @return this {@link MapBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeMapBuilder<K, V> memoryManager(MemoryManager memoryManager) {
		checkPrepared();
		this.memoryManager = memoryManager;
		return this;
	}

	/**
	 * Specifies that the created huge map is allowed to trade other resources (typically Java heap memory) to gain speed improvements.
	 * 
	 * <p>This is a hint and may or may not be ignored.</p>
	 * 
	 * @return this {@link MapBuilder} to chain calls
	 */
	public AbstractHugeMapBuilder<K, V> faster() {
		faster = true;
		return this;
	}
	
	/**
	 * Specifies that the created huge map should be initialized for the specified capacity.
	 * 
	 * <p>This is a hint and may or may not be ignored.</p>
	 * 
	 * @param capacity the initial capacity 
	 * @return this {@link MapBuilder} to chain calls
	 */
	public AbstractHugeMapBuilder<K, V> capacity(int capacity) {
		this.capacity = Math.max(this.capacity, capacity);
		return this;
	}
		
	private void checkPrepared() {
		if (prepared) {
			throw new IllegalStateException("Cannot change the configuration after adding the first element.");
		}
	}

	private void prepare() {
		if (prepared) {
			return;
		}
		
		if (keyConverter == null) {
			if (classLoader == null && keyClass != null) {
				classLoader = keyClass.getClassLoader();
			}
			
			keyConverter = Converters.bestConverter(keyClass, classLoader);
		}

		if (compressKey) {
			keyConverter = new ZipCompressionConverter<K>(keyConverter);
		}

		if (valueConverter == null) {
			if (classLoader == null && valueClass != null) {
				classLoader = valueClass.getClassLoader();
			}

			valueConverter = Converters.bestConverter(valueClass, classLoader);
		}
		
		if (compressValue) {
			valueConverter = new ZipCompressionConverter<V>(valueConverter);
		}

		if (memoryManager == null) {
			if (blockSize == null) {
				int serializedKeyLength = keyConverter.serializedLength();
				int serializedValueLength = valueConverter.serializedLength();
				blockSize = (serializedKeyLength == serializedValueLength && serializedKeyLength > 0) ? serializedKeyLength : MemoryMappedFileManager.NO_BLOCK_SIZE;
			}
			memoryManager = new MemoryMappedFileManager(bufferSize, blockSize);
		}
		
		if (capacity == 0) {
			capacity = 8;
		}

		prepared = true;
	}
	
	/**
	 * Returns the key {@link Converter}.
	 * 
	 * @return the key {@link Converter}
	 */
	protected Converter<K> getKeyConverter() {
		prepare();
		
		return keyConverter;
	}
	
	/**
	 * Returns the value {@link Converter}.
	 * 
	 * @return the value {@link Converter}
	 */
	protected Converter<V> getValueConverter() {
		prepare();
		
		return valueConverter;
	}
	
	/**
	 * Returns the {@link MemoryManager}.
	 * 
	 * @return the {@link MemoryManager}
	 */
	protected MemoryManager getMemoryManager() {
		prepare();

		return memoryManager;
	}

	/**
	 * Returns whether the faster mode was specified in the builder.
	 * 
	 * @return <code>true</code> if faster, <code>false</code> otherwise
	 */
	protected boolean isFaster() {
		prepare();
		
		return faster;
	}
	
	/**
	 * Returns the initial capacity.
	 * 
	 * @return the initial capacity
	 */
	protected int getCapacity() {
		prepare();
		
		return capacity;
	}
}
