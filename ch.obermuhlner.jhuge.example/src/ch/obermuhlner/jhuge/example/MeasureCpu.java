package ch.obermuhlner.jhuge.example;

import java.util.Collection;
import java.util.Map;

import ch.obermuhlner.jhuge.collection.HugeArrayList;

/**
 * Application to measure the CPU performance of several {@link Collection} and {@link Map} implementations.
 */
public class MeasureCpu {

	/**
	 * Starts the CPU measurement application.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		measureClear();
	}

	private static void measureClear() {
		HugeArrayList<Integer> list = new HugeArrayList.Builder<Integer>().build();
		
		for (int i = 0; i < 10000; i++) {
			list.add(i);
		}
		
		StopWatch stopWatch = new StopWatch();
		list.clear();
		System.out.println("clear() in " + stopWatch);
	}
	
	private static class StopWatch {
		private long startTime = System.nanoTime();

		double getElapsedMilliseconds() {
			long endTime = System.nanoTime();
			
			return (endTime - startTime) / 1000000.0;
		}

		@Override
		public String toString() {
			return getElapsedMilliseconds() + " ms";
		}
	}
}
