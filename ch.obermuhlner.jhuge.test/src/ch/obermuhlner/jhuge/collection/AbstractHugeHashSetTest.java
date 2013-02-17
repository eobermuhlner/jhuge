package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

import ch.obermuhlner.jhuge.collection.HugeHashSet.Builder;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.IntegerConverter;
import ch.obermuhlner.jhuge.converter.ZipCompressionConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to test {@link HugeHashSet}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractHugeHashSetTest extends AbstractSetTest {

	@Override
	protected <E> Set<E> createSet(E... initial) {
		MemoryManager memoryManager = createMemoryManager();

		Builder<E> builder = new HugeHashSet.Builder<E>().memoryManager(memoryManager);
		if (isFaster()) {
			builder.faster();
		}
		builder.addAll(initial);
		
		return builder.build();
	}

	protected abstract boolean isFaster();

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
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		assertEquals(true, list.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}
		
	@Test
	public void testBuilder_classLoader() {
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().classLoader(getClass().getClassLoader()).build();
		assertNotNull(list);
	}
	
	@Test
	public void testBuilder_elementClass() {
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().element(Integer.class).build();
		assertEquals(true, (list.getElementConverter() instanceof IntegerConverter));
		assertEquals(true, list.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
	}

	@Test
	public void testBuilder_elementConverter() {
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().element(new IntegerConverter()).build();
		assertEquals(true, (list.getElementConverter() instanceof IntegerConverter));
	}

	@Test
	public void testBuilder_compressElement() {
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().compressElement().build();
		assertEquals(true, (list.getElementConverter() instanceof ZipCompressionConverter));
	}

	@Test
	public void testBuilder_bufferSize() {
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().bufferSize(1234).build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) list.getMemoryManager();
		assertEquals(1234, memoryManager.getBufferSize());
	}

	@Test
	public void testBuilder_blockSize() {
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().blockSize(3456).build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) list.getMemoryManager();
		assertEquals(3456, memoryManager.getBlockSize());
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilder_prepare() {
		Builder<Integer> builder = new HugeHashSet.Builder<Integer>();
		builder.add(1);
		builder.element(Integer.class);
	}

	@Test
	public void testBuilder_add() {
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).add(1).build();
		assertEquals(1, list.size());
	}

	@Test
	public void testBuilder_addAll_vararg() {
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).addAll(1, 2, 3).build();
		assertEquals(3, list.size());
	}

	@Test
	public void testBuilder_addAll_Collection() {
		HugeHashSet<Integer> list = new HugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).addAll(Arrays.asList(1, 2, 3)).build();
		assertEquals(3, list.size());
	}
}
