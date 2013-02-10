package ch.obermuhlner.jhuge.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ch.obermuhlner.jhuge.collection.HugeArrayList;
import ch.obermuhlner.jhuge.collection.HugeHashMap;
import ch.obermuhlner.jhuge.collection.HugeHashSet;
import ch.obermuhlner.jhuge.collection.ImmutableHugeHashMap;
import ch.obermuhlner.jhuge.collection.ImmutableHugeHashSet;

/**
 * Application to measure the Java heap of several {@link Collection} and {@link Map} implementations.
 * 
 * <p>Use jconsole or jvisualvm to take a heap dump of the running application and analyze it with MAT.</p>
 */
public class MeasureHeap {
	
	/**
	 * Starts the heap measurement application.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		measureHeapMemory();
	}

	@SuppressWarnings("unused")
	private static ArrayList<String> staticArrayList;
	@SuppressWarnings("unused")
	private static HashSet<String> staticHashSet;
	@SuppressWarnings("unused")
	private static HashMap<String, String> staticHashMap;

	@SuppressWarnings("unused")
	private static HugeArrayList<String> staticHugeArrayList;
	@SuppressWarnings("unused")
	private static HugeArrayList<String> staticFastHugeArrayList;

	@SuppressWarnings("unused")
	private static HugeHashSet<String> staticHugeHashSet;
	@SuppressWarnings("unused")
	private static HugeHashSet<String> staticFastHugeHashSet;
	@SuppressWarnings("unused")
	private static ImmutableHugeHashSet<String> staticImmutableHugeHashSet;
	@SuppressWarnings("unused")
	private static ImmutableHugeHashSet<String> staticFastImmutableHugeHashSet;

	@SuppressWarnings("unused")
	private static HugeHashMap<String, String> staticHugeHashMap;
	@SuppressWarnings("unused")
	private static HugeHashMap<String, String> staticFastHugeHashMap;
	@SuppressWarnings("unused")
	private static ImmutableHugeHashMap<String, String> staticImmutableHugeHashMap;
	@SuppressWarnings("unused")
	private static ImmutableHugeHashMap<String, String> staticFastImmutableHugeHashMap;
	
	private static void measureHeapMemory() {
		int count = 10000;
		
		final int bufferSize = 10 * 1024 * 1024;
		
		staticArrayList = fillCollection(new ArrayList<String>(), count);
		staticHashSet = fillCollection(new HashSet<String>(), count);
		staticHashMap = fillMap(new HashMap<String, String>(), count);

		staticHugeArrayList = fillCollection(new HugeArrayList.Builder<String>().bufferSize(bufferSize).build(), count);
		staticFastHugeArrayList = fillCollection(new HugeArrayList.Builder<String>().bufferSize(bufferSize).faster().build(), count);
		
		staticHugeHashSet = new HugeHashSet.Builder<String>().bufferSize(bufferSize).addAll(fillCollection(new ArrayList<String>(), count)).build();
		staticFastHugeHashSet = new HugeHashSet.Builder<String>().bufferSize(bufferSize).faster().addAll(fillCollection(new ArrayList<String>(), count)).build();
		staticImmutableHugeHashSet = new ImmutableHugeHashSet.Builder<String>().bufferSize(bufferSize).addAll(fillCollection(new ArrayList<String>(), count)).build();
		staticFastImmutableHugeHashSet = new ImmutableHugeHashSet.Builder<String>().bufferSize(bufferSize).faster().addAll(fillCollection(new ArrayList<String>(), count)).build();
		
		staticHugeHashMap = fillMap(new HugeHashMap.Builder<String, String>().bufferSize(bufferSize).build(), count);
		staticFastHugeHashMap = fillMap(new HugeHashMap.Builder<String, String>().bufferSize(bufferSize).faster().build(), count);
		staticImmutableHugeHashMap = new ImmutableHugeHashMap.Builder<String, String>().bufferSize(bufferSize).putAll(fillMap(new HashMap<String, String>(), count)).build();
		staticFastImmutableHugeHashMap = new ImmutableHugeHashMap.Builder<String, String>().bufferSize(bufferSize).faster().putAll(fillMap(new HashMap<String, String>(), count)).build();
		
		System.out.println("Ready to take heapdump.");
		System.out.println("Open the heapdump in MemoryAnalyzer and look for the static fields of MeasureHeap.");
		
		try {
			Thread.sleep(100 * 1000);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}

		System.out.println("Goodbye.");
	}

	private static <T extends Collection<String>> T fillCollection(T collection, int count) {
		for (int i = 0; i < count; i++) {
			collection.add(valueString(i));
		}
		return collection;
	}
	
	private static <T extends Map<String, String>> T fillMap(T map, int count) {
		for (int i = 0; i < count; i++) {
			map.put(keyString(i), valueString(i));
		}
		return map;
	}

	private static String keyString(int i) {
		return "key" + i;
	}
	
	private static String valueString(int value) {
		return "X" + value;
	}
}
