package org.biopax.paxtools.controller;
/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 25, 2008
 * Time: 1:03:41 AM
 */

import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.DeltaG;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleEditorMapTest
{
    @Test
    public void testSimpleEditorMap() throws Exception
    {
        for (BioPAXLevel level : BioPAXLevel.values())
        {
            SimpleEditorMap simpleEditorMap = new SimpleEditorMap(level);
            assertNotNull(simpleEditorMap);
            assertEquals(level, simpleEditorMap.getLevel());
	        System.out.println("initialized for " + level );
	        System.out.println(simpleEditorMap);
        }
    }
    
	/**
	 * Currently (before 10-Apr-2011), it seems impossible
	 * to clear a single-cardinality property by
	 * using the corresponding property editor
	 * TODO Is this what we really want? (i.e., cannot set to 'unknown'/null)
	 */
    @Test
    public void testClearSingularProperty() {    	
    	BioPAXFactory fac = BioPAXLevel.L3.getDefaultFactory();
    	EditorMap em = new SimpleEditorMap(BioPAXLevel.L3);
    	
    	// test - for a singular object property
    	ProteinReference pr = fac.create(ProteinReference.class, "PR");
    	BioSource bs = fac.create(BioSource.class, "BS");
    	PropertyEditor editor = em.getEditorForProperty("organism", ProteinReference.class);
    	pr.setOrganism(bs);
    	
    	pr.setOrganism(null);
    	assertNull(pr.getOrganism());
    	
    	pr.setOrganism(bs);
    	
    	editor.removeValueFromBean(bs, pr); // no effect (this is correct result)
    	assertEquals(bs, pr.getOrganism());
    	
    	editor.setValueToBean(null, pr);
    	/*
    	assertNotNull(pr.getOrganism()); // no effect! Is this the behavior we want?
    	*/
    	// after re-factoring (PropertyEditor)
    	assertNull(pr.getOrganism());
    	assertTrue(editor.isUnknown(pr.getOrganism()));
    	
    	// test for a singular primitive property
    	editor = em.getEditorForProperty("ph", DeltaG.class);
    	
    	DeltaG dg = fac.create(DeltaG.class, "DG");
    	assertNotNull(dg.getPh());
    	assertTrue(editor.isUnknown(dg.getPh()));
    	
    	dg.setPh(6.5f);
    	assertFalse(editor.isUnknown(dg.getPh()));
    	
    	editor.setValueToBean(null, dg);
    	//assertFalse(editor.isUnknown(dg.getPh())); // no effect: cannot go back to 'unknown' by using null!
    	assertTrue(editor.isUnknown(dg.getPh())); // after re-factoring
    	
    	editor.setValueToBean(BioPAXElement.UNKNOWN_FLOAT, dg);
    	assertTrue(editor.isUnknown(dg.getPh()));
    }
}