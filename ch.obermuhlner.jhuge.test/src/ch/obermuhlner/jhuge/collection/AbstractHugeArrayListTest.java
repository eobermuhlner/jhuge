package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ch.obermuhlner.jhuge.collection.HugeArrayList.Builder;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.IntegerConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to test {@link HugeArrayList}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractHugeArrayListTest extends AbstractListTest {

	@Override
	protected <E> List<E> createList(E... initial) {
		MemoryManager memoryManager = createMemoryManager();

		return new HugeArrayList.Builder<E>().memoryManager(memoryManager).addAll(initial).build();
	}

	protected abstract MemoryManager createMemoryManager();

	@Override
	protected boolean supportsMutable() {
		return true;
	}
	
	@Override
	protected boolean supportsNullValues() {
		return true;
	}
	
	@Test
	public void testBuilder_default() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		assertEquals(true, list.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}
		
	@Test
	public void testBuilder_classLoader() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().classLoader(getClass().getClassLoader()).build();
		assertNotNull(list);
	}
	
	@Test
	public void testBuilder_elementClass() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().element(Integer.class).build();
		assertEquals(true, (list.getElementConverter() instanceof IntegerConverter));
		assertEquals(true, list.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
	}

	@Test
	public void testBuilder_elementConverter() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().element(new IntegerConverter()).build();
		assertEquals(true, (list.getElementConverter() instanceof IntegerConverter));
	}

	@Test
	public void testBuilder_bufferSize() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().bufferSize(1234).build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) list.getMemoryManager();
		assertEquals(1234, memoryManager.getBufferSize());
	}

	@Test
	public void testBuilder_blockSize() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().blockSize(3456).build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) list.getMemoryManager();
		assertEquals(3456, memoryManager.getBlockSize());
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilder_prepare() {
		Builder<Integer> builder = new HugeArrayList.Builder<Integer>();
		builder.add(1);
		builder.element(Integer.class);
	}

	@Test
	public void testBuilder_add() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().memoryManager(createMemoryManager()).add(1).build();
		assertEquals(1, list.size());
	}

	@Test
	public void testBuilder_addAll_vararg() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().memoryManager(createMemoryManager()).addAll(1, 2, 3).build();
		assertEquals(3, list.size());
	}

	@Test
	public void testBuilder_addAll_Collection() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().memoryManager(createMemoryManager()).addAll(Arrays.asList(1, 2, 3)).build();
		assertEquals(3, list.size());
	}
}
