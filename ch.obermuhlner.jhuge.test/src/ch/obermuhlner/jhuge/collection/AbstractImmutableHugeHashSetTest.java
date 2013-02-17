package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

import ch.obermuhlner.jhuge.collection.ImmutableHugeHashSet.Builder;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.IntegerConverter;
import ch.obermuhlner.jhuge.converter.ZipCompressionConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to test {@link ImmutableHugeHashSet}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractImmutableHugeHashSetTest extends AbstractSetTest {

	@Override
	protected <E> Set<E> createSet(E... initial) {
		MemoryManager memoryManager = createMemoryManager();

		return new ImmutableHugeHashSet.Builder<E>().memoryManager(memoryManager).addAll(initial).build();
	}

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
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		assertEquals(true, list.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}
		
	@Test
	public void testBuilder_classLoader() {
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().classLoader(getClass().getClassLoader()).build();
		assertNotNull(list);
	}
	
	@Test
	public void testBuilder_elementClass() {
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().element(Integer.class).build();
		assertEquals(true, (list.getElementConverter() instanceof IntegerConverter));
		assertEquals(true, list.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
	}

	@Test
	public void testBuilder_elementConverter() {
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().element(new IntegerConverter()).build();
		assertEquals(true, (list.getElementConverter() instanceof IntegerConverter));
	}

	@Test
	public void testBuilder_compressElement() {
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().compressElement().build();
		assertEquals(true, (list.getElementConverter() instanceof ZipCompressionConverter));
	}
	
	@Test
	public void testBuilder_bufferSize() {
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().bufferSize(12346).build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) list.getMemoryManager();
		assertEquals(12346, memoryManager.getBufferSize());
	}

	@Test
	public void testBuilder_blockSize() {
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().blockSize(34567).build();
		assertEquals(true, list.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) list.getMemoryManager();
		assertEquals(34567, memoryManager.getBlockSize());
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilder_prepare() {
		Builder<Integer> builder = new ImmutableHugeHashSet.Builder<Integer>();
		builder.add(1);
		builder.element(Integer.class);
	}

	@Test
	public void testBuilder_add() {
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).add(1).build();
		assertEquals(1, list.size());
	}

	@Test
	public void testBuilder_addAll_vararg() {
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).addAll(1, 2, 3).build();
		assertEquals(3, list.size());
	}

	@Test
	public void testBuilder_addAll_Collection() {
		ImmutableHugeHashSet<Integer> list = new ImmutableHugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).addAll(Arrays.asList(1, 2, 3)).build();
		assertEquals(3, list.size());
	}
}
