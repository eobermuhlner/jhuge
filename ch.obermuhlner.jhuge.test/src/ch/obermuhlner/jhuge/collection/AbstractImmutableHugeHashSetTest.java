package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ch.obermuhlner.jhuge.collection.ImmutableHugeHashSet.Builder;
import ch.obermuhlner.jhuge.converter.CompactConverter;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.IntegerConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to test {@link ImmutableHugeHashSet}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractImmutableHugeHashSetTest extends AbstractSetTest {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected <E> Set<E> createSet(E... initial) {
		MemoryManager memoryManager = createMemoryManager();
		Converter<E> converter = new CompactConverter(null);
		
		Builder<E> builder = new ImmutableHugeHashSet.Builder<E>().memoryManager(memoryManager).element(converter);
		builder.addAll(Arrays.asList(initial));
		return builder.build();
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
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().build();
		assertEquals(true, set.getMemoryManager() instanceof MemoryMappedFileManager);
		assertEquals(true, set.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}
		
	@Test
	public void testBuilder_classLoader() {
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().classLoader(getClass().getClassLoader()).build();
		assertNotNull(set);
	}
	
	@Test
	public void testBuilder_elementClass() {
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().element(Integer.class).build();
		assertEquals(true, (set.getElementConverter() instanceof IntegerConverter));
		assertEquals(true, set.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
	}

	@Test
	public void testBuilder_elementConverter() {
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().element(new IntegerConverter()).build();
		assertEquals(true, (set.getElementConverter() instanceof IntegerConverter));
	}

	@Test
	public void testBuilder_bufferSize() {
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().bufferSize(1234).build();
		assertEquals(true, set.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) set.getMemoryManager();
		assertEquals(1234, memoryManager.getBufferSize());
	}

	@Test
	public void testBuilder_blockSize() {
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().blockSize(3456).build();
		assertEquals(true, set.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) set.getMemoryManager();
		assertEquals(3456, memoryManager.getBlockSize());
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilder_prepare() {
		Builder<Integer> builder = new ImmutableHugeHashSet.Builder<Integer>();
		builder.add(1);
		builder.element(Integer.class);
	}

	@Test
	public void testBuilder_add() {
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).add(1).build();
		assertEquals(1, set.size());
	}

	@Test
	public void testBuilder_addAll_vararg() {
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).addAll(1, 2, 3).build();
		assertEquals(3, set.size());
	}

	@Test
	public void testBuilder_addAll_Collection() {
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).addAll(Arrays.asList(1, 2, 3)).build();
		assertEquals(3, set.size());
	}

	@Test
	public void testBuilder_addAll_Set() {
		Set<Integer> initial = new HashSet<Integer>(Arrays.asList(1, 2, 3));
		
		ImmutableHugeHashSet<Integer> set = new ImmutableHugeHashSet.Builder<Integer>().memoryManager(createMemoryManager()).addAll(initial).build();
		assertEquals(3, set.size());
	}
}
