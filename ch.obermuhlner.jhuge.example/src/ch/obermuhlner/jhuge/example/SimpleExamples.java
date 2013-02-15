package ch.obermuhlner.jhuge.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ch.obermuhlner.jhuge.collection.HugeArrayList;
import ch.obermuhlner.jhuge.collection.HugeArrayList.Builder;
import ch.obermuhlner.jhuge.collection.HugeHashMap;
import ch.obermuhlner.jhuge.collection.ImmutableHugeHashSet;

/**
 * Application to show some simple examples using the JHuge collections. 
 */
public class SimpleExamples {
	private static Random random = new Random();

	private static final int N = 10;
	
	/**
	 * Starts the simple example application.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		exampleHugeArrayList();
		exampleConfiguredHugeArrayList();
		exampleImmutableHugeHashSet();
		exampleHugeHashMap();
		exampleHugeHashMap_Collection();
	}

	private static void exampleHugeArrayList() {
		final int n = N;

		List<String> list = new HugeArrayList.Builder<String>().build();
		
		list.add("This is a small text.");
		list.add("But you could as well store many large documents.");
		list.add("No matter how many of these texts you put into the list, the Java heap will never grow.");
		list.add("Because they are stored in external memory outside of the Java heap.");
		list.add("When you access an element of the list it is restored and becomes again available as Java object.");
		
		for (int i = 0; i < n; i++) {
			list.add(createSomeExampleString(i));
		}
		
		for (String string : list) {
			System.out.println(string);
		}
	}

	private static void exampleConfiguredHugeArrayList() {
		final int n = N;

		Builder<String> builder = new HugeArrayList.Builder<String>();
		builder.faster(); // elements are still stored off-heap but access infrastructure is now in Java heap to improve performance
		builder.capacity(n); // give a hint about the expected capacity of 100 elements (can still grow)

		List<String> list = builder.build();

		for (int i = 0; i < n; i++) {
			list.add(createSomeExampleString(i));
		}
		
		for (String string : list) {
			System.out.println(string);
		}
	}

	private static void exampleImmutableHugeHashSet() {
		final int n = N;

		ch.obermuhlner.jhuge.collection.ImmutableHugeHashSet.Builder<Integer> builder = new ImmutableHugeHashSet.Builder<Integer>();
		builder.capacity(n); // give a hint about the expected capacity of 100 elements (can still grow)
		builder.element(Integer.class); // give a hint at the type of elements, so that the conversion and storage can be optimized 

		// add elements to the Builder, since the built set is immutable and not more elements can be added to it
		for (int i = 0; i < n; i++) {
			builder.add(createRandomInteger(n * 3));
		}
		
		Set<Integer> set = builder.build();
		
		for (int i = 0; i < 100; i++) {
			if (set.contains(i)) {
				System.out.println("Contains " + i);
			} else {
				System.out.println("Does not contain " + i);
			}
		}
	}
	
	private static void exampleHugeHashMap() {
		final int n = N;

		ch.obermuhlner.jhuge.collection.HugeHashMap.Builder<Integer, String> builder = new HugeHashMap.Builder<Integer, String>();
		
		Map<Integer, String> map = builder.build();
		
		for (int i = 0; i < n; i++) {
			map.put(i, createSomeExampleString(i));
		}
		
		int index = 0;
		for (Integer key : map.keySet()) {
			System.out.print(key);
			if (++index % 20 == 0) {
				System.out.println();
			} else {
				System.out.print(" ");
			}
		}
	}

	private static void exampleHugeHashMap_Collection() {
		Map<String, Collection<String>> map = new HugeHashMap.Builder<String, Collection<String>>().build();

		List<String> studentsList = new ArrayList<String>();
		map.put("students", studentsList);
		
		studentsList.add("Jim");
		System.out.println(map.get("students"));
		
		map.put("students", studentsList);
		System.out.println(map.get("students"));
	}

	private static String createSomeExampleString(int i) {
		return "This is the string #" + i + ". It is not a very large string, but it could be much bigger.";
	}

	private static int createRandomInteger(int range) {
		return random.nextInt(range);
	}
}
