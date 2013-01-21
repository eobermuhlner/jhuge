package ch.obermuhlner.jhuge.collection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests a few strings for hash code collisions.
 */
public class HashCodeCollisionsTest {

	/**
	 * A string that has hash code collisions with the other constants in this class.
	 */
	public static final String COLLISION_0 = "AaAa";
	/**
	 * A string that has hash code collisions with the other constants in this class.
	 */
	public static final String COLLISION_1 = "AaBB";
	/**
	 * A string that has hash code collisions with the other constants in this class.
	 */
	public static final String COLLISION_2 = "BBAa";
	/**
	 * A string that has hash code collisions with the other constants in this class.
	 */
	public static final String COLLISION_3 = "BBBB";

	/**
	 * Test for hash code collisions.
	 */
	@Test
	public void testHashCodeCollisions() {
		{
			// prove that the constants COLLISION_0 to COLLISITION_3 strings actually have hash code collisions
			assertEquals(COLLISION_0.hashCode(), COLLISION_0.hashCode());
			assertEquals(COLLISION_0.hashCode(), COLLISION_1.hashCode());
			assertEquals(COLLISION_0.hashCode(), COLLISION_2.hashCode());
			assertEquals(COLLISION_0.hashCode(), COLLISION_3.hashCode());
		}
	}
}
