# JHuge - Java Huge Collections

JHuge provides several collections that follow the Java Collection API but store the data in external memory outside of the Java heap.

Storing data outside of the Java heap can alleviate the problems with garbage collection becoming slow with a large Java heap.

Currently the following implementations are available:
- [HugeArrayList](http://eobermuhlner.github.com/jhuge/releases/release-0.1/javadoc/ch/obermuhlner/jhuge/collection/HugeArrayList.html)
- [HugeHashSet](http://eobermuhlner.github.com/jhuge/releases/release-0.1/javadoc/ch/obermuhlner/jhuge/collection/HugeHashSet.html)
- [HugeHashMap](http://eobermuhlner.github.com/jhuge/releases/release-0.1/javadoc/ch/obermuhlner/jhuge/collection/HugeHashMap.html)
- [ImmutableHugeArrayList](http://eobermuhlner.github.com/jhuge/releases/release-0.1/javadoc/ch/obermuhlner/jhuge/collection/ImmutableHugeArrayList.html)
- [ImmutableHugeHashSet](http://eobermuhlner.github.com/jhuge/releases/release-0.1/javadoc/ch/obermuhlner/jhuge/collection/ImmutableHugeHashSet.html)
- [ImmutableHugeHashMap](http://eobermuhlner.github.com/jhuge/releases/release-0.1/javadoc/ch/obermuhlner/jhuge/collection/ImmutableHugeHashMap.html)

The collections in JHuge are designed to be flexible.
You can:
- provide alternate memory manager
- provide custom converters for the elements you store
- trade some Java heap consumption for improved speed

## Release 0.2

Download the library and the source bundle:
- [ch.obermuhlner.jhuge_0.2.0.jar](http://eobermuhlner.github.com/jhuge/releases/release-0.2/ch.obermuhlner.jhuge_0.2.0.jar) - the JHuge library (OSGi bundle)
- [ch.obermuhlner.jhuge.source_0.2.0.jar](http://eobermuhlner.github.com/jhuge/releases/release-0.2/ch.obermuhlner.jhuge.source_0.2.0.jar) - the source bundle

Read the online [Javadoc JHuge 0.2](http://eobermuhlner.github.com/jhuge/releases/release-0.2/javadoc/).

The [Benchmark Report](http://eobermuhlner.github.com/jhuge/releases/release-0.2/report/) is also online.


## Examples
 
 ```Java
 		// Simple HugeArrayList example.
 		
		HugeArrayList<String> list = new HugeArrayList.Builder<String>().build();
		
		list.add("This is a small text.");
		list.add("But you could as well store many large documents.");
		list.add("No matter how many of these texts you put into the list, the Java heap will never grow.");
		list.add("Because they are stored in external memory outside of the Java heap.");
		list.add("When you access the content of the list the objects are restored and become again available as Java objects.");
		
		for (String string : list) {
			System.out.println(string);
		}
```


```Java
 		// A HugeArrayList with a little configuration in the Builder.
 		
		Builder<String> builder = new HugeArrayList.Builder<String>();
		builder.faster(); // elements are still stored off-heap but access infrastructure is now in Java heap to improve performance
		builder.capacity(200); // optimize for capacity of 200 elements (can still grow)

		for (int i = 0; i < 100; i++) {
			builder.add(createSomeExampleString(i)); // initial elements can be added to the Builder
		}
		
		HugeArrayList<String> list = builder.build();

		for (int i = 100; i < 200; i++) {
			list.add(createSomeExampleString(i)); // add more elements
		}

		for (String string : list) {
			System.out.println(string);
		}
```
 
 
```Java
		// ImmutableHugeHashMap example.
		
		Builder<Integer, String> builder = new ImmutableHugeHashMap.Builder<Integer, String>();
		builder.key(Integer.class); // give a hint about the type of keys to optimize conversion and storage
		builder.value(String.class); // give a hint about the type of values to optimize conversion and storage
		for (int i = 0; i < 100; i++) {
			builder.put(i, createSomeExampleString(i)); // elements must be added to the builder
		}
		
		Map<Integer, String> map = builder.build(); // once the Map is created it can no longer be modified
		
		int index = 0;
		for (Integer key : map.keySet()) {
			System.out.print(key);
			if (++index % 20 == 0) {
				System.out.println();
			} else {
				System.out.print(" ");
			}
		}
```
 
## Caveats

### Tradeoff Faster vs. Compact

The builder option `faster()` to trade some Java heap consumption against improved performance is fully implemented in the List implementations:
- HugeArrayList
- ImmutableHugeArrayList

The Set and Map implementations are functionally complete but currently only the `faster()` mode is implemented:
- HugeHashSet
- HugeHashMap
- ImmutableHugeHashSet
- ImmutableHugeHashMap

For these 4 classes the faster mode is the default and the compact mode is not yet implemented.
The faster mode still uses a standard HashMap to store the infrastructure.
With the next release this should be available.

### Converters

The default [CompactConverter](http://eobermuhlner.github.com/jhuge/releases/release-0.1/javadoc/ch/obermuhlner/jhuge/converter/CompactConverter.html)
is implemented but the custom converters for the specific classes are still incomplete.
Currently implemented are:
- [IntegerConverter](http://eobermuhlner.github.com/jhuge/releases/release-0.1/javadoc/ch/obermuhlner/jhuge/converter/IntegerConverter.html)
- [LongConverter](http://eobermuhlner.github.com/jhuge/releases/release-0.1/javadoc/ch/obermuhlner/jhuge/converter/LongConverter.html)

The CompactConverter can also be optimized in some important cases (e.g. String, BigDecimal).

### Benchmark Report

The current implementation start allocating memory blocks only when necessary.
This leads to the side effect that the very first write operation is much slower since allocating a direct memory buffer is expensive.

In the benchmark report this would show as a strong peak for the first write operation (typically at the 0 position of the x-axis).
Because this peak is so high it would make the chart unreadable (all other values would be glued to the bottom of the y-axis).

In order to still produce a meaningful chart a little cheating was done in the benchmarks.
The test data is set up in a way that ensures that there has always been a buffer allocated before benchmarking starts.

I plan a future Builder option so that buffer allocation and shrinking strategy can be configured.
Possible strategies would be something like:
- shrink-as-much-as-possible
- shrink-but-keep-always-one-buffer-allocated (this would be default)
- never-shrink-and-always-at-least-one-buffer-allocated

The benchmark cheat could then be removed and my bad conscience would be gone.

