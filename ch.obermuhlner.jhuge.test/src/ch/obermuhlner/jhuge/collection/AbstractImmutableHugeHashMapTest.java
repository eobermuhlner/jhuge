package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ch.obermuhlner.jhuge.collection.ImmutableHugeHashMap.Builder;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.IntegerConverter;
import ch.obermuhlner.jhuge.converter.LongConverter;
import ch.obermuhlner.jhuge.converter.ZipCompressionConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to test {@link ImmutableHugeHashMap}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractImmutableHugeHashMapTest extends AbstractMapTest {

	@Override
	protected <K, V> java.util.Map<K,V> createMap(Pair<K,V>... initial) {
		MemoryManager memoryManager = createMemoryManager();

		Builder<K, V> builder = new ImmutableHugeHashMap.Builder<K, V>();
		builder.memoryManager(memoryManager);
		
		for (Pair<K, V> pair : initial) {
			builder.put(pair.getValue1(), pair.getValue2());
		}
		
		return builder.build();
	}

	protected abstract MemoryManager createMemoryManager();

	@Override
	protected boolean supportsMutable() {
		return false;
	}
	
	@Override
	protected boolean supportsNullKeys() {
		return true;
	}

	@Override
	protected boolean supportsNullValues() {
		return true;
	}

	@Test
	public void testBuilder_default() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().build();
		assertEquals(true, map.getMemoryManager() instanceof MemoryMappedFileManager);
		assertEquals(true, map.getKeyConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
		assertEquals(true, map.getValueConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}
		
	@Test
	public void testBuilder_classLoader() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().classLoader(getClass().getClassLoader()).build();
		assertNotNull(map);
	}
	
	@Test
	public void testBuilder_keyClass() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().key(Integer.class).build();
		assertEquals(true, (map.getKeyConverter() instanceof IntegerConverter));
		assertEquals(true, map.getKeyConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
		assertEquals(true, map.getValueConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}

	@Test
	public void testBuilder_valueClass() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().value(Long.class).build();
		assertEquals(true, map.getKeyConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
		assertEquals(true, (map.getValueConverter() instanceof LongConverter));
		assertEquals(true, map.getValueConverter().getClass().isAssignableFrom(Converters.bestConverter(Long.class).getClass()));
	}

	@Test
	public void testBuilder_keyClass_valueClass() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().key(Integer.class).value(Long.class).build();
		assertEquals(true, (map.getKeyConverter() instanceof IntegerConverter));
		assertEquals(true, map.getKeyConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
		assertEquals(true, (map.getValueConverter() instanceof LongConverter));
		assertEquals(true, map.getValueConverter().getClass().isAssignableFrom(Converters.bestConverter(Long.class).getClass()));
	}

	@Test
	public void testBuilder_keyConverter() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().key(new IntegerConverter()).build();
		assertEquals(true, (map.getKeyConverter() instanceof IntegerConverter));
	}

	@Test
	public void testBuilder_valueConverter() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().value(new LongConverter()).build();
		assertEquals(true, (map.getValueConverter() instanceof LongConverter));
	}

	@Test
	public void testBuilder_compressKey() {
		ImmutableHugeHashMap<Integer, Long> list = new ImmutableHugeHashMap.Builder<Integer, Long>().compressKey().build();
		assertEquals(true, (list.getKeyConverter() instanceof ZipCompressionConverter));
	}
	
	@Test
	public void testBuilder_compressValue() {
		ImmutableHugeHashMap<Integer, Long> list = new ImmutableHugeHashMap.Builder<Integer, Long>().compressValue().build();
		assertEquals(true, (list.getValueConverter() instanceof ZipCompressionConverter));
	}
	
	@Test
	public void testBuilder_bufferSize() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().bufferSize(1234).build();
		assertEquals(true, map.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) map.getMemoryManager();
		assertEquals(1234, memoryManager.getBufferSize());
	}

	@Test
	public void testBuilder_blockSize() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().blockSize(3456).build();
		assertEquals(true, map.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) map.getMemoryManager();
		assertEquals(3456, memoryManager.getBlockSize());
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilder_prepare() {
		Builder<Integer, Long> builder = new ImmutableHugeHashMap.Builder<Integer, Long>();
		builder.put(1, 2L);
		builder.key(Integer.class);
	}

	@Test
	public void testBuilder_put() {
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().memoryManager(createMemoryManager()).put(1, 10L).build();
		assertEquals(1, map.size());
	}

	@Test
	public void testBuilder_putAll() {
		Map<Integer, Long> initial = new HashMap<Integer, Long>();
		initial.put(1, 11L);
		initial.put(2, 12L);
		initial.put(3, 13L);
		
		ImmutableHugeHashMap<Integer, Long> map = new ImmutableHugeHashMap.Builder<Integer, Long>().memoryManager(createMemoryManager()).putAll(initial).build();
		assertEquals(3, map.size());
	}
}
