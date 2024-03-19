package org.biopax.paxtools.impl.level3;

import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.UnificationXref;

import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.BiopaxSafeSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EqualsEtcTest {

	static final BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();

	@Test
	public final void equals() {
		// a BioPAX object (e.g., xref)
		UnificationXref x1 = factory.create(UnificationXref.class, "x1");
		x1.setDb("MI");
		x1.setId("MI:0492");

		// another xref - different ID, different properties
		UnificationXref x2 = factory.create(UnificationXref.class, "x2");
		x2.setDb("MI");
		x2.setId("MI:0493");

		// - same ID (x1), different property value
		UnificationXref x3 = factory.create(UnificationXref.class, "x1");
		x3.setDb("MI");
		x3.setId("MI:0493");

		// - a copy of x1 (a different java object)
		UnificationXref x4 = factory.create(UnificationXref.class, "x1");
		x4.setDb("MI");
		x4.setId("MI:0492");

		// - different ID, same properties (as of x1)
		UnificationXref x5 = factory.create(UnificationXref.class, "x5");
		x5.setDb("MI");
		x5.setId("MI:0492");

		UnificationXref x7 = x1;
		UnificationXref x6 = x1;

		assertTrue(x6 == x7);
		assertTrue(x6.equals(x7));
		assertFalse(x1 == x3);
		assertTrue(x1.equals(x3)); //same type and URI (prop. are ignored)
		assertTrue(x1.isEquivalent(x3)); // not the same db,id but same URI - equivalent because of equals (above)
		assertFalse(x1 == x4);
		assertTrue(x1.equals(x4)); //same type and URI
		assertTrue(x1.isEquivalent(x4)); // x4 is a copy of x1
		assertTrue(x1.isEquivalent(x5)); // x5 has different rdfId but same db/id
	}

	@Test
	public final void hashSetOfBpe() {
		UnificationXref x1 = factory.create(UnificationXref.class, "x1");
		x1.setDb("foo");
		x1.setId("foo");
		UnificationXref x2 = factory.create(UnificationXref.class, "x1");
		x2.setDb("bar");
		x2.setId("bar");

		Set<BioPAXElement> col = new HashSet<>();
		col.add(x1);
		assertTrue(col.contains(x1));
		assertTrue(col.contains(x2)); // different xref, same URI and type!
		col.add(x2); // - silently ignored (due to x1.equals(x2) returns true)!
		assertTrue(col.size() == 1);

		col.remove(x2); //actually removes x1 (equal object)
		assertTrue(col.isEmpty());

		//add back
		col.add(x2);

		Xref x3 = factory.create(RelationshipXref.class, "x1");
		// x3 is of different xref type than x1
		assertFalse(col.contains(x3));

		Xref x4 = factory.create(UnificationXref.class, "x1");
		// same xref class, properties, same URI as x1 - equals x1 -
		assertTrue(col.contains(x4));
	}


	@Test
	public final void biopaxSafeSet() {
		UnificationXref x1 = factory.create(UnificationXref.class, "x1");
		x1.setDb("foo");
		x1.setId("foo");

		//another xref using the same URI
		UnificationXref x2 = factory.create(UnificationXref.class, "x1");
		x2.setDb("bar");
		x2.setId("bar");

		Set<Xref> col = new BiopaxSafeSet<>();

		col.add(x1);
		assertTrue(col.contains(x1));
		assertFalse(col.contains(x2)); //not the same thing despite same uri and type

		col.add(x2); //duplicate URI object is ignored
		assertTrue(col.size() == 1); //did not add the second one

		x2 = factory.create(UnificationXref.class, "x2");
		x2.setDb("bar");
		x2.setId("bar");
		col.add(x2);
		assertTrue(col.size() == 2);

		Set<Xref> col1 = new BiopaxSafeSet<>();
		col1.addAll(col);
		assertTrue(col1.size() == 2);
		assertTrue(col1.contains(x1));
		assertTrue(col1.contains(x2));

		col1.remove(x2);
		assertTrue(col1.size() == 1);
		assertTrue(col1.contains(x1));
		assertFalse(col1.contains(x2));

  	assertFalse(col.contains("foo"));
		assertFalse(col.remove("foo"));
		assertFalse(col.contains(null));
		assertFalse(col.remove(null));
	}

}
