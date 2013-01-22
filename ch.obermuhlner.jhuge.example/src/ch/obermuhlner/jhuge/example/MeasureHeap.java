package ch.obermuhlner.jhuge.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.obermuhlner.jhuge.collection.HugeArrayList;
import ch.obermuhlner.jhuge.collection.HugeHashMap;
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
	private static HugeArrayList<String> staticHugeArrayList;
	@SuppressWarnings("unused")
	private static HugeArrayList<String> staticFastHugeArrayList;
	@SuppressWarnings("unused")
	private static ImmutableHugeHashSet<String> staticImmutableHugeHashSet;
	@SuppressWarnings("unused")
	private static HashMap<Integer, String> staticHashMap;
	@SuppressWarnings("unused")
	private static HugeHashMap<Integer, String> staticHugeHashMap;
	
	private static void measureHeapMemory() {
		int count = 10000;
		List<String> dataList = new ArrayList<String>();
		for (int i = 0; i < count; i++) {
			dataList.add(randomString(i));
		}
		Map<String, String> dataMap = new HashMap<String, String>();
		for (int i = 0; i < count; i++) {
			dataMap.put("key"+i, randomString(i));
		}
		
		final int bufferSize = 10 * 1024 * 1024;
		staticArrayList = new ArrayList<String>(dataList);
		staticHugeArrayList = new HugeArrayList.Builder<String>().bufferSize(bufferSize).addAll(dataList).build();
		staticFastHugeArrayList = new HugeArrayList.Builder<String>().bufferSize(bufferSize).faster().addAll(dataList).build();
//		staticImmutableHugeHashSet = new ImmutableHugeHashSet.Builder<String>().bufferSize(bufferSize).addAll(dataList).build();
//		staticHashMap = new HashMap<Integer, String>(dataMap);
//		staticHugeHashMap = new HugeHashMap.Builder<Integer, String>().bufferSize(bufferSize).putAll(dataMap).build();
		
		System.out.println("Ready to take heapdump.");
		
		try {
			Thread.sleep(100 * 1000);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}

		System.out.println("Goodbye.");
	}
	
	private static String randomString(int value) {
		return "X" + value;
	}
}
