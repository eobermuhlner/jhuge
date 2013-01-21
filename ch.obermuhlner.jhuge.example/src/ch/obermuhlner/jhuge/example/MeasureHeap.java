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
	private static ArrayList<Integer> staticArrayList;
	@SuppressWarnings("unused")
	private static HugeArrayList<Integer> staticHugeArrayList;
	@SuppressWarnings("unused")
	private static ImmutableHugeHashSet<Integer> staticImmutableHugeHashSet;
	@SuppressWarnings("unused")
	private static HashMap<Integer, Integer> staticHashMap;
	@SuppressWarnings("unused")
	private static HugeHashMap<Integer, Integer> staticHugeHashMap;
	
	private static void measureHeapMemory() {
		int count = 10000;
		List<Integer> dataList = new ArrayList<Integer>();
		for (int i = 0; i < count; i++) {
			dataList.add(new Integer(i));
		}
		Map<Integer, Integer> dataMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < count; i++) {
			dataMap.put(i, i);
		}
		
		staticArrayList = new ArrayList<Integer>(dataList);
		staticHugeArrayList = new HugeArrayList.Builder<Integer>().addAll(dataList).build();
		staticImmutableHugeHashSet = new ImmutableHugeHashSet.Builder<Integer>().addAll(dataList).build();
		staticHashMap = new HashMap<Integer, Integer>(dataMap);
		staticHugeHashMap = new HugeHashMap.Builder<Integer, Integer>().putAll(dataMap).build();
		
		System.out.println("Ready to take heapdump.");
		
		try {
			Thread.sleep(100 * 1000);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}

		System.out.println("Goodbye.");
	}
}
