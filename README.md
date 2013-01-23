JHuge - Java Huge Collections
=============================

JHuge provides several collections that follow the Java Collection API but store the data in external memory outside of the Java heap.

Storing data outside of the Java heap can alleviate the problems with garbage collection becoming slow with a large Java heap.

Currently the following implementations are available:
- HugeArrayList<E> implements List<E>
- HugeHashSet implements Set<E>
- ImmutableHugeHashSet<E> implements Set<E>
- HugeHashMap<K, V> implements Map<K, V>

The collections in JHuge are designed to be flexible.
You can:
- provide alternate memory manager
- provide custom converters for the elements you store
- trade some Java heap consumption for improved speed


Examples
--------
 
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
			list.add(createSomeExampleString(i));
		}

		for (String string : list) {
			System.out.println(string);
		}
```
 
 
```Java
		// Simple HugeHashMap example.
		
		HugeHashMap.Builder<Integer, String> builder = new HugeHashMap.Builder<Integer, String>();
		
		Map<Integer, String> map = builder.build();
		
		for (int i = 0; i < 100; i++) {
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
```
 


