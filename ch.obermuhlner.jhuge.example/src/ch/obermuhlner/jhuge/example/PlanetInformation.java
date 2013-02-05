package ch.obermuhlner.jhuge.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import ch.obermuhlner.jhuge.collection.HugeHashMap;
import ch.obermuhlner.jhuge.collection.HugeHashMap.Builder;

/**
 * Example application to demonstrate using a {@link HugeHashMap}.
 */
public class PlanetInformation {

	private Map<Integer, PlanetInfo> planetMap = null;
	
	private int nextPlanetId = 0;

	private long initialUsedMemory;

	private void setupInitialUsedMemory() {
		System.gc();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long totalMemory = Runtime.getRuntime().totalMemory();
		initialUsedMemory = totalMemory - freeMemory;
	}
	
	private void reset(boolean useHeap) {
		nextPlanetId = 0;
		
		if (useHeap) {
			planetMap = new HashMap<Integer, PlanetInfo>();
			
		} else {
			Builder<Integer, PlanetInfo> builder = new HugeHashMap.Builder<Integer, PlanetInfo>();
			planetMap = builder.build();
		}
		
		addMorePlanets(10);
	}
	
	private void addMorePlanets(int count) {
		for (int i = 0; i < count; i++) {
			int id = nextPlanetId++;
			planetMap.put(id, createPlanetInfo(id));
		}
	}
	
	private void runShell() {
		boolean finished = false;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		setupInitialUsedMemory();

		while (! finished) {
			System.out.println();
			System.out.print("> ");
			String[] command = readCommandLine(reader);
			
			try {
				finished = executeCommandLine(command);
			} catch (RuntimeException exception) {
				exception.printStackTrace();
			}
		}
	}

	private boolean executeCommandLine(String[] command) {
		boolean finished = false;
		
		if (command.length == 0) {
			// does nothing
			
		} else if (command[0].equals("help")) {
			System.out.println("Commands:");
			System.out.println("  help");
			System.out.println("  exit");
			System.out.println("  heap");
			System.out.println("  reset onheap");
			System.out.println("  reset offheap");
			System.out.println("  info");
			System.out.println("  more");
			System.out.println("  planets");
			System.out.println("  planet id <id>");
			System.out.println("  planet name <id>");

		} else if (command[0].equals("exit")) {
			System.out.println("Goodbye.");
			finished = true;
			
		} else if (command[0].equals("heap")) {
			System.gc();
			long freeMemory = Runtime.getRuntime().freeMemory();
			long totalMemory = Runtime.getRuntime().totalMemory();
			long usedMemory = totalMemory - freeMemory;
			long additionalUsedMemory = usedMemory - initialUsedMemory;
			System.out.println("Used  : " + usedMemory + " bytes (" + asMegaBytes(usedMemory) + " MB)");
			System.out.println("Free  : " + freeMemory + " bytes (" + asMegaBytes(freeMemory) + " MB)");
			System.out.println("Total : " + totalMemory + " bytes (" + asMegaBytes(totalMemory) + " MB)");
			System.out.println("Additional Used : " + additionalUsedMemory + " bytes (" + asMegaBytes(additionalUsedMemory) + " MB)");
			
		} else if (command[0].equals("reset")) {
			boolean useHeap = command.length > 1 ? "onheap".equals(command[1]) : false;
			reset(useHeap);
			System.out.println("Resetted to use " + (useHeap ? "onheap" : "offheap") + " storage.");
			
		} else if (command[0].equals("info")) {
			System.out.println(planetMap.size() + " planets found.");
			
		} else if (command[0].equals("more")) {
			int count = command.length > 1 ? Integer.parseInt(command[1]) : 10;
			addMorePlanets(count);
			System.out.println("Added " + count + " more planets.");
			
		} else if (command[0].equals("planets")) {
			for (Entry<Integer, PlanetInfo> entry : planetMap.entrySet()) {
				System.out.println(entry.getKey() + " : " + entry.getValue().getName());
			}
			
		} else if (command[0].equals("planet") && command[1].equals("id")) {
			PlanetInfo planetInfo = planetMap.get(Integer.parseInt(command[2]));
			print(planetInfo);
			
		} else {
			System.out.println("Unknown command: " + Arrays.toString(command));
		}
		
		return finished;
	}
	
	private static double asMegaBytes(long bytes) {
		return bytes / 1024.0 / 1024.0;
	}

	private static void print(PlanetInfo planetInfo) {
		System.out.println("ID   : " + planetInfo.getId());
		System.out.println("X    : " + planetInfo.getX());
		System.out.println("Y    : " + planetInfo.getY());
		System.out.println("Z    : " + planetInfo.getZ());
		System.out.println("Name : " + planetInfo.getName());
		System.out.println("Description : ");
		System.out.println(planetInfo.getDescription());
	}

	private static String[] readCommandLine(BufferedReader reader) {
		try {
			String line = reader.readLine();
			String[] command = line.split("[ \t]+");
			return command;
		} catch (IOException e) {
			e.printStackTrace();
			return new String [] { "exit" };
		}
	}

	/**
	 * Starts the planet info application
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		PlanetInformation planetInformation = new PlanetInformation();
		planetInformation.reset(false);
		planetInformation.runShell();
	}
	
	private static PlanetInfo createPlanetInfo(int id) {
		Random random = new Random(id);

		random.nextInt();
		random.nextInt();
		random.nextInt();
		
		double x = random.nextGaussian() * 100;
		double y = random.nextGaussian() * 100;
		double z = random.nextGaussian() * 10;

		String name = randomName(random);
		
		StringBuilder description = new StringBuilder();
		description.append("The planet ");
		description.append(name);
		description.append(" is ");
		description.append(choose(random, "boring", "interesting", "colorful", "bleak", "deadly", "exotic"));
		description.append(".");
		
				
		return new PlanetInfo(x, y, z, id, name, description.toString());
	}
	
	private static String randomName(Random random) {
		StringBuilder result = new StringBuilder();

		if (random.nextInt(500) == 0) {
			result.append(choose(random, "qu"));
		}
		if (random.nextInt(5) == 0) {
			result.append(choose(random, "a", "e", "i", "o", "u", "y"));
		}
		int syllabes = random.nextInt(3) + 2;
		for (int i = 0; i < syllabes; i++) {
			result.append(choose(random, "b", "c", "d", "g", "h", "k", "p", "qu", "t", "x", "z"));
			result.append(choose(random, "a", "e", "i", "o", "u", "y"));
			if (random.nextInt(3) == 0) {
				result.append(choose(random, "f", "l", "m", "n", "r", "s"));
			}
		}
		return result.toString();
	}

	private static <T> T choose(Random random, T... elements) {
		return choose(random.nextInt(elements.length), elements);
	}
	
	private static <T> T choose(int index, T... elements) {
		return elements[index % elements.length];
	}

	/**
	 * Stores information about a planet.
	 */
	public static class PlanetInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final double x;
		private final double y;
		private final double z;
		
		private final int id;
		private final String name;
		private final String description;

		/**
		 * Constructs a {@link PlanetInfo}.
		 * 
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param z the z coordinate
		 * @param id the ID
		 * @param name the name
		 * @param description the description
		 */
		public PlanetInfo(double x, double y, double z, int id, String name, String description) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.id = id;
			this.name = name;
			this.description = description;
		}

		/**
		 * Returns the x coordinate.
		 *
		 * @return the x coordinate
		 */
		public double getX() {
			return x;
		}

		/**
		 * Returns the y coordinate.
		 *
		 * @return the y coordinate
		 */
		public double getY() {
			return y;
		}

		/**
		 * Returns the z coordinate.
		 *
		 * @return the z coordinate
		 */
		public double getZ() {
			return z;
		}
		
		/**
		 * Returns the id.
		 * 
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * Returns the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the description.
		 *
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
	}
}
