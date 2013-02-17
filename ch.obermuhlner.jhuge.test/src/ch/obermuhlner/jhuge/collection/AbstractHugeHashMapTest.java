package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ch.obermuhlner.jhuge.collection.HugeHashMap.Builder;
import ch.obermuhlner.jhuge.converter.Converters;
import ch.obermuhlner.jhuge.converter.IntegerConverter;
import ch.obermuhlner.jhuge.converter.LongConverter;
import ch.obermuhlner.jhuge.converter.ZipCompressionConverter;
import ch.obermuhlner.jhuge.memory.MemoryManager;
import ch.obermuhlner.jhuge.memory.MemoryMappedFileManager;

/**
 * Abstract base class to test {@link HugeHashMap}.
 */
@SuppressWarnings("javadoc")
public abstract class AbstractHugeHashMapTest extends AbstractMutableMapTest {

	@Override
	protected <K, V> Map<K, V> createEmptyMap() {
		MemoryManager memoryManager = createMemoryManager();

		Builder<K, V> builder = new HugeHashMap.Builder<K, V>();
		builder.memoryManager(memoryManager);
		if (isFaster()) {
			builder.faster();
		}
		return builder.build();
	}

	protected abstract boolean isFaster();
	
	protected abstract MemoryManager createMemoryManager();

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
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().build();
		assertEquals(true, map.getMemoryManager() instanceof MemoryMappedFileManager);
		assertEquals(true, map.getKeyConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
		assertEquals(true, map.getValueConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}
		
	@Test
	public void testBuilder_classLoader() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().classLoader(getClass().getClassLoader()).build();
		assertNotNull(map);
	}
	
	@Test
	public void testBuilder_keyClass() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().key(Integer.class).build();
		assertEquals(true, (map.getKeyConverter() instanceof IntegerConverter));
		assertEquals(true, map.getKeyConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
		assertEquals(true, map.getValueConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
	}

	@Test
	public void testBuilder_valueClass() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().value(Long.class).build();
		assertEquals(true, map.getKeyConverter().getClass().isAssignableFrom(Converters.bestConverter(null).getClass()));
		assertEquals(true, (map.getValueConverter() instanceof LongConverter));
		assertEquals(true, map.getValueConverter().getClass().isAssignableFrom(Converters.bestConverter(Long.class).getClass()));
	}

	@Test
	public void testBuilder_keyClass_valueClass() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().key(Integer.class).value(Long.class).build();
		assertEquals(true, (map.getKeyConverter() instanceof IntegerConverter));
		assertEquals(true, map.getKeyConverter().getClass().isAssignableFrom(Converters.bestConverter(Integer.class).getClass()));
		assertEquals(true, (map.getValueConverter() instanceof LongConverter));
		assertEquals(true, map.getValueConverter().getClass().isAssignableFrom(Converters.bestConverter(Long.class).getClass()));
	}

	@Test
	public void testBuilder_keyConverter() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().key(new IntegerConverter()).build();
		assertEquals(true, (map.getKeyConverter() instanceof IntegerConverter));
	}

	@Test
	public void testBuilder_valueConverter() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().value(new LongConverter()).build();
		assertEquals(true, (map.getValueConverter() instanceof LongConverter));
	}

	@Test
	public void testBuilder_compressKey() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().compressKey().build();
		assertEquals(true, (map.getKeyConverter() instanceof ZipCompressionConverter));
	}

	@Test
	public void testBuilder_bufferSize() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().bufferSize(1234).build();
		assertEquals(true, map.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) map.getMemoryManager();
		assertEquals(1234, memoryManager.getBufferSize());
	}

	@Test
	public void testBuilder_blockSize() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().blockSize(3456).build();
		assertEquals(true, map.getMemoryManager() instanceof MemoryMappedFileManager);
		MemoryMappedFileManager memoryManager = (MemoryMappedFileManager) map.getMemoryManager();
		assertEquals(3456, memoryManager.getBlockSize());
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilder_prepare() {
		Builder<Integer, Long> builder = new HugeHashMap.Builder<Integer, Long>();
		builder.put(1, 2L);
		builder.key(Integer.class);
	}

	@Test
	public void testBuilder_add() {
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().memoryManager(createMemoryManager()).put(1, 10L).build();
		assertEquals(1, map.size());
	}

	@Test
	public void testBuilder_addAll() {
		Map<Integer, Long> initial = new HashMap<Integer, Long>();
		initial.put(1, 11L);
		initial.put(2, 12L);
		initial.put(3, 13L);
		
		HugeHashMap<Integer, Long> map = new HugeHashMap.Builder<Integer, Long>().memoryManager(createMemoryManager()).putAll(initial).build();
		assertEquals(3, map.size());
	}
}
