package ch.obermuhlner.jhuge.collection.builder;

import java.util.Collection;

import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.ZipCompressionConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to implement a {@link CollectionBuilder} for a huge {@link Collection}.
 *
 * @param <E> the type of the element in the {@link Collection}
 */
public abstract class AbstractHugeCollectionBuilder<E> implements CollectionBuilder<E> {
	
	private Class<E> elementClass;

	private ClassLoader classLoader;

	private int bufferSize = 100 * MemoryMappedFileManager.MEGABYTES;

	private Integer blockSize;
	
	private Converter<E> elementConverter;
	
	private boolean compressElement;

	private MemoryManager memoryManager;

	private boolean faster;
	
	private int capacity;
	
	private boolean prepared;

	/**
	 * Specifies the {@link ClassLoader} used to deserialize elements.
	 * 
	 * @param classLoader the {@link ClassLoader} used to deserialize elements
	 * @return this {@link CollectionBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeCollectionBuilder<E> classLoader(ClassLoader classLoader) {
		checkPrepared();
		this.classLoader = classLoader;
		return this;
	}
	
	/**
	 * Specifies the type of the elements.
	 * 
	 * <p>If the {@link #classLoader(ClassLoader) ClassLoader} is not explicitly specified then this class is used to determine the {@link ClassLoader}.</p>
	 * <p>If the {@link #element(Converter) element Converter} is not explicitly specified then this class is used to determine the best key {@link Converter}.</p>
	 * 
	 * @param elementClass the type of the elements
	 * @return this {@link CollectionBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeCollectionBuilder<E> element(Class<E> elementClass) {
		checkPrepared();
		this.elementClass = elementClass;
		return this;
	}
	
	/**
	 * Specifies the element {@link Converter} used to serialize/deserialize elements.
	 * 
	 * @param elementConverter the {@link Converter} used to serialize/deserialize elements
	 * @return this {@link CollectionBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeCollectionBuilder<E> element(Converter<E> elementConverter) {
		checkPrepared();
		this.elementConverter = elementConverter;
		return this;
	}
	
	/**
	 * Specifies that the serialized elements should be stored in a compressed form.
	 * 
	 * @return this {@link CollectionBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeCollectionBuilder<E> compressElement() {
		checkPrepared();
		this.compressElement = true;
		return this;
	}
	
	/**
	 * Specifies the buffer size used in the {@link MemoryMappedFileManager}.
	 * 
	 * @param bufferSize the buffer size
	 * @return this {@link CollectionBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeCollectionBuilder<E> bufferSize(int bufferSize) {
		checkPrepared();
		this.bufferSize = bufferSize;
		return this;
	}
	
	/**
	 * Specifies the block size used in the {@link MemoryMappedFileManager}.
	 * 
	 * @param blockSize the block size
	 * @return this {@link CollectionBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeCollectionBuilder<E> blockSize(int blockSize) {
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
	 * @return this {@link CollectionBuilder} to chain calls
	 * @throws IllegalStateException if called after adding the first element to this builder
	 */
	public AbstractHugeCollectionBuilder<E> memoryManager(MemoryManager memoryManager) {
		checkPrepared();
		this.memoryManager = memoryManager;
		return this;
	}
	
	/**
	 * Specifies that the created huge collection is allowed to trade other resources (typically Java heap memory) to gain speed improvements.
	 * 
	 * <p>This is a hint and may or may not be ignored.</p>
	 * 
	 * @return this {@link CollectionBuilder} to chain calls
	 */
	public AbstractHugeCollectionBuilder<E> faster() {
		faster = true;
		return this;
	}
	
	/**
	 * Specifies that the created huge collection should be initialized for the specified capacity.
	 * 
	 * <p>This is a hint and may or may not be ignored.</p>
	 * 
	 * @param capacity the initial capacity 
	 * @return this {@link CollectionBuilder} to chain calls
	 */
	public AbstractHugeCollectionBuilder<E> capacity(int capacity) {
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
		
		if (elementConverter == null) {
			if (classLoader == null && elementClass != null) {
				classLoader = elementClass.getClassLoader();
			}
			
			elementConverter = Converters.bestConverter(elementClass, classLoader);
		}
		
		if (compressElement) {
			elementConverter = new ZipCompressionConverter<E>(elementConverter);
		}
		
		if (memoryManager == null) {
			if (blockSize == null) {
				int serializedLength = elementConverter.serializedLength();
				blockSize = serializedLength > 0 ? serializedLength : MemoryMappedFileManager.NO_BLOCK_SIZE;
			}
			memoryManager = new MemoryMappedFileManager(bufferSize, blockSize);
		}
		
		if (capacity == 0) {
			capacity = 8;
		}
		
		prepared = true;
	}
	
	/**
	 * Returns the element {@link Converter}.
	 * 
	 * @return the element {@link Converter}
	 */
	protected Converter<E> getElementConverter() {
		prepare();
		
		return elementConverter;
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
