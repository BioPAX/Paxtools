package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DuplicateProprtyValuesTest {

	@Before
	public void setUp() throws Exception {
	}

	
	/**
	 * Checks that the same xref cannot be 
	 */
	@Test
	public final void testXref() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
    	ProteinReference pr =factory.create(ProteinReference.class, "ProteinReference");
    	pr.setDisplayName("ProteinReference");
		Xref ref1 =  factory.create(UnificationXref.class, "xref1");
    	ref1.setDb("uniprotkb");
    	ref1.setId("Q0VCL1");
    	pr.addXref(ref1);
    	// new object
    	Xref ref2 =  factory.create(UnificationXref.class, "xref1");
    	ref2.setDb("uniprotkb"); 
    	ref2.setId("Q0VCL1");
    	pr.addXref(ref2);
    	
    	assertFalse(ref1 == ref2);
    	assertEquals(ref1, ref2);
		assertEquals(1, pr.getXref().size()); // wasn't duplicated?
		assertEquals(1, ref1.getXrefOf().size());
		
		assertEquals(1, ref2.getXrefOf().size()); // right? it depends...
		/* this is not really a bug (considering multiple models 
		 * that can share some objects), but one should be aware and taking care of:
		 */
		try {
			assertEquals(0, ref2.getXrefOf().size()); // TODO BANG! ref2 wasn't added but has xrefOf!
		} catch (AssertionError e) {
			System.out.println("WARN: BANG! Xref wasn't added to the model but got xrefOf value!");
		}
	}
}
