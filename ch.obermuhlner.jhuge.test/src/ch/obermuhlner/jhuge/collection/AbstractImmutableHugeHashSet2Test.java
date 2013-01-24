package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ch.obermuhlner.jhuge.collection.ImmutableHugeHashSet2.Builder;
import ch.obermuhlner.jhuge.converter.CompactConverter;
import ch.obermuhlner.jhuge.converter.Converter;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.IntegerConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to test {@link ImmutableHugeHashSet2}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractImmutableHugeHashSet2Test extends AbstractSetTest {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected <E> Set<E> createSet(E... initial) {
		MemoryManager memoryManager = createMemoryManager();
		Converter<E> converter = new CompactConverter(null);
		
		Builder<E> builder = new ImmutableHugeHashSet2.Builder<E>();
		builder.element(converter);
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
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().build();
		assertEquals(true, set.getMemoryManager() instanceof MemoryMappedFileManager);
		assertEquals(true, set.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}
		
	@Test
	public void testBuilder_classLoader() {
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().classLoader(getClass().getClassLoader()).build();
		assertNotNull(set);
	}
	
	@Test
	public void testBuilder_elementClass() {
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().element(Integer.class).build();
		assertEquals(true, (set.getElementConverter() instanceof IntegerConverter));
		assertEquals(true, set.getElementConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
	}

	@Test
	public void testBuilder_elementConverter() {
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().element(new IntegerConverter()).build();
		assertEquals(true, (set.getElementConverter() instanceof IntegerConverter));
	}

	@Test
	public void testBuilder_bufferSize() {
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().bufferSize(1234).build();
		assertEquals(true, set.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) set.getMemoryManager();
		assertEquals(1234, memoryManager.getBufferSize());
	}

	@Test
	public void testBuilder_blockSize() {
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().blockSize(3456).build();
		assertEquals(true, set.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) set.getMemoryManager();
		assertEquals(3456, memoryManager.getBlockSize());
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilder_prepare() {
		Builder<Integer> builder = new ImmutableHugeHashSet2.Builder<Integer>();
		builder.add(1);
		builder.element(Integer.class);
	}

	@Test
	public void testBuilder_add() {
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().memoryManager(createMemoryManager()).add(1).build();
		assertEquals(1, set.size());
	}

	@Test
	public void testBuilder_addAll_vararg() {
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().memoryManager(createMemoryManager()).addAll(1, 2, 3).build();
		assertEquals(3, set.size());
	}

	@Test
	public void testBuilder_addAll_Collection() {
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().memoryManager(createMemoryManager()).addAll(Arrays.asList(1, 2, 3)).build();
		assertEquals(3, set.size());
	}

	@Test
	public void testBuilder_addAll_Set() {
		Set<Integer> initial = new HashSet<Integer>(Arrays.asList(1, 2, 3));
		
		ImmutableHugeHashSet2<Integer> set = new ImmutableHugeHashSet2.Builder<Integer>().memoryManager(createMemoryManager()).addAll(initial).build();
		assertEquals(3, set.size());
	}
}
