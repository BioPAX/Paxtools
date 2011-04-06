package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnificationXrefImplTest {

	@Test
	public final void testEquals() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();

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
		
		
    	UnificationXref x6 = null;
    	UnificationXref x7 = null;
    	assertTrue(x6 == x7);
    	
    	x6 = x1;
    	assertFalse(x6 == x7); 
    	assertTrue(x1 == x6); // same reference (address)
    	assertTrue(x1.equals(x6)); // - for sure
    	
    	x7 = x1;
    	assertTrue(x6 == x7);
    	assertTrue(x6.equals(x7));
    	assertTrue(x1.equals(x7));
    	assertTrue(x1.equals(x6));
    	
    	assertFalse(x1 == x4);
    	assertFalse(x1.equals(x4));
    	assertTrue(x1.isEquivalent(x4)); // x4 is a copy of x1
    	
    	assertTrue(x1.isEquivalent(x5)); // x5 has different rdfId but same db/id
    	
    	assertFalse(x1.isEquivalent(x3)); // not the same db/id
	}
	
}
