package org.biopax.paxtools.impl.level3;

import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.BiopaxSafeSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class EqualsEtcTest {

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
    	assertFalse(x1 == x3);   	
    	assertFalse(x1.equals(x4));
    	assertFalse(x1.equals(x3));
    	assertTrue(x1.isEquivalent(x4)); // x4 is a copy of x1
    	assertTrue(x1.isEquivalent(x5)); // x5 has different rdfId but same db/id
    	assertFalse(x1.isEquivalent(x3)); // not the same db/id but same URI - NOT equivalent
	}

	
	
	// If we override 'equals' and 'hashCode' methods only in BioPAXElementImpl
	// and only based upon URI as we once had already in 2010 (paxtools 2.0 and before), 
	// then the following test PASS (FUNNY, but it's our contract...)
	@Ignore //fails if basic 'equals' and 'hashCode' were not touched (inherited from Object)
	@Test
	public final void testCollectionOfBiopaxElementsCustom() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();

		UnificationXref x1 = factory.create(UnificationXref.class, "x1");
		x1.setDb("foo");
		x1.setId("foo");
    	UnificationXref x2 = factory.create(UnificationXref.class, "x1");
		x2.setDb("bar");
		x2.setId("bar");
		
		Set<BioPAXElement> col = new HashSet<BioPAXElement>();
		col.add(x1);
		assertTrue(col.contains(x1));
		assertTrue(col.contains(x2)); // different xref, same URI!!
		col.add(x2); //silently ignored
		assertFalse(col.size() == 2);
		
		col.remove(x2); //actually removes x1 too!!
		assertTrue(col.isEmpty()); //rediculous...
		
		// even more funny results
		Xref x3 = factory.create(RelationshipXref.class, "x1");
		col.add(x2);
		assertTrue(col.contains(x3)); // different xref class, properties, but same URI!!!
	}

	
	//@Ignore //fails if basic 'equals' and 'hashCode' were overridden in BioPAXElementImpl only (based on the URI string)
	@Test 
	public final void testCollectionOfBiopaxElementsStandard() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();

		UnificationXref x1 = factory.create(UnificationXref.class, "x1");
		x1.setDb("foo");
		x1.setId("foo");		
    	UnificationXref x2 = factory.create(UnificationXref.class, "x1");
		x2.setDb("bar");
		x2.setId("bar");
		
		// with a standard collection (NOT a mul. cardinality biopax property value)
		Set<BioPAXElement> col = new HashSet<BioPAXElement>();
		col.add(x1);
		assertTrue(col.contains(x1));
		assertFalse(col.contains(x2)); //if 'equals' and 'hashCode' were overridden, this might fail
		col.add(x2); //if 'equals' and 'hashCode' were overridden, this be ignored
		assertTrue(col.size() == 2);
		assertTrue(col.contains(x2)); // contains two xrefs with the same URI - OK
		Xref x3 = factory.create(RelationshipXref.class, "x1");
		assertFalse(col.contains(x3));//if 'equals' and 'hashCode' were overridden, this might fail too
			
		//now test a collection that is also a biopax property (using BiopaxSafeSet class)
		XReferrable owner = factory.create(Protein.class, "p1");
		owner.addXref(x1);
		owner.addXref(x2); //ignored
		assertTrue(owner.getXref().size() == 1);
		
		UnificationXref x4 = factory.create(UnificationXref.class, "x1");
		x4.setDb("foo");
		x4.setId("foo");
		owner.addXref(x4); //ignored
		assertTrue(owner.getXref().size() == 1);
		
		UnificationXref x5 = factory.create(UnificationXref.class, "x5");
		x5.setDb("foo");
		x5.setId("foo");
		owner.addXref(x5);
		assertTrue(owner.getXref().size() == 2);
	}
	
	
	@Test
	public final void testBiopaxSafeSet() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();

		UnificationXref x1 = factory.create(UnificationXref.class, "x1");
		x1.setDb("foo");
		x1.setId("foo");
		//another xref, same URI
    	UnificationXref x2 = factory.create(UnificationXref.class, "x1");
		x2.setDb("bar");
		x2.setId("bar");
		
		Set<Xref> col = new BiopaxSafeSet<Xref>();
		col.add(x1);
		assertTrue(col.contains(x1));
		assertEquals(x1, ((BiopaxSafeSet<Xref>)col).get("x1"));		
		
		// BiopaxSafeSet overrides 'contains'
		assertFalse(col.contains(x2));
		col.add(x2); //duplicate URI is ignored (warning is logged)
		assertTrue(col.size() == 1);
		
		x2 = factory.create(UnificationXref.class, "x2");
		x2.setDb("bar");
		x2.setId("bar");
		col.add(x2);
		assertTrue(col.size() == 2);
	
		Set<Xref> col1 = new BiopaxSafeSet<Xref>();
		col1.addAll(col);
		assertTrue(col1.size() == 2);
		assertTrue(col1.contains(x1));
		assertTrue(col1.contains(x2));
		
		col1.remove(x2);
		assertTrue(col1.size() == 1);
		assertTrue(col1.contains(x1));
		assertFalse(col1.contains(x2));
		assertEquals("foo", ((BiopaxSafeSet<Xref>)col1).get("x1").getDb());
			
	}
}
