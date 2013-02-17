package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ch.obermuhlner.jhuge.collection.ImmutableHugeArrayList.Builder;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.IntegerConverter;
import ch.obermuhlner.jhuge.converter.ZipCompressionConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to test {@link ImmutableHugeArrayList}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractImmutableHugeArrayListTest extends AbstractListTest {

	@Override
	protected <E> List<E> createList(E... initial) {
		MemoryManager memoryManager = createMemoryManager();

		ImmutableHugeArrayList.Builder<E> builder = new ImmutableHugeArrayList.Builder<E>();
		builder.memoryManager(memoryManager);
		if (isFaster()) {
			builder.faster();
		}
		builder.capacity(initial.length);
		builder.addAll(initial);
		return builder.build();
	}

	protected abstract boolean isFaster();
	
	protected abstract MemoryManager createMemoryManager();

	@Override
	protected boolean supportsMutable() {
		return false;
	}
	
	@Override
	protected boolean supportsNullValues() {
		return true;
	}
	
	@Test
	public void testBuilder_default() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		assertEquals(true, list.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}
		
	@Test
	public void testBuilder_classLoader() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().classLoader(getClass().getClassLoader()).build();
		assertNotNull(list);
	}
	
	@Test
	public void testBuilder_elementClass() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().element(Integer.class).build();
		assertEquals(true, (list.getElementConverter() instanceof IntegerConverter));
		assertEquals(true, list.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
	}

	@Test
	public void testBuilder_elementConverter() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().element(new IntegerConverter()).build();
		assertEquals(true, (list.getElementConverter() instanceof IntegerConverter));
	}

	@Test
	public void testBuilder_compressElement() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().compressElement().build();
		assertEquals(true, (list.getElementConverter() instanceof ZipCompressionConverter));
	}
	
	@Test
	public void testBuilder_bufferSize() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().bufferSize(12345).build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) list.getMemoryManager();
		assertEquals(12345, memoryManager.getBufferSize());
	}

	@Test
	public void testBuilder_blockSize() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().blockSize(34567).build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) list.getMemoryManager();
		assertEquals(34567, memoryManager.getBlockSize());
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilder_prepare() {
		Builder<Integer> builder = new ImmutableHugeArrayList.Builder<Integer>();
		builder.add(1);
		builder.element(Integer.class);
	}

	@Test
	public void testBuilder_add() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().memoryManager(createMemoryManager()).add(1).build();
		assertEquals(1, list.size());
	}

	@Test
	public void testBuilder_addAll_vararg() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().memoryManager(createMemoryManager()).addAll(1, 2, 3).build();
		assertEquals(3, list.size());
	}

	@Test
	public void testBuilder_addAll_Collection() {
		ImmutableHugeArrayList<Integer> list = new ImmutableHugeArrayList.Builder<Integer>().memoryManager(createMemoryManager()).addAll(Arrays.asList(1, 2, 3)).build();
		assertEquals(3, list.size());
	}
}
