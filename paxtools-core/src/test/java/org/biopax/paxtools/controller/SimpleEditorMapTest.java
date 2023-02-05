package org.biopax.paxtools.controller;
/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 25, 2008
 * Time: 1:03:41 AM
 */

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.DeltaG;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class SimpleEditorMapTest
{
    @Test
    public void testSimpleEditorMap() throws Exception
    {
        for (BioPAXLevel level : BioPAXLevel.values())
        {
            SimpleEditorMap simpleEditorMap = SimpleEditorMap.valueOf(level.name());
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
	 */
    @Test
    public void testClearSingularProperty() {    	
    	BioPAXFactory fac = BioPAXLevel.L3.getDefaultFactory();
    	EditorMap em = SimpleEditorMap.L3;
    	
    	// test - for a singular object property
    	ProteinReference pr = fac.create(ProteinReference.class, "PR");
    	BioSource bs = fac.create(BioSource.class, "BS");
    	PropertyEditor editor = em.getEditorForProperty("organism", ProteinReference.class);

    	pr.setOrganism(bs);
    	editor.removeValueFromBean(bs, pr);
    	assertNull(pr.getOrganism());

		pr.setOrganism(bs);
    	editor.setValueToBean(null, pr);

    	// ok, after fixing/re-factoring (AbstractPropertyEditor)
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
    	assertTrue(editor.isUnknown(dg.getPh())); // fixed after re-factoring
    	
    	editor.setValueToBean(BioPAXElement.UNKNOWN_FLOAT, dg);
    	assertTrue(editor.isUnknown(dg.getPh()));
    }
    
	@Test
	public final void testComments() {
    	BioPAXFactory fac = BioPAXLevel.L3.getDefaultFactory();
    	EditorMap em = SimpleEditorMap.L3;
		ProteinReference pr1 = fac.create(ProteinReference.class, "pr1"); 
		pr1.addComment("one");
		pr1.addComment("two");
		assertEquals(2, pr1.getComment().size());
		
		pr1.getComment().clear();
		assertEquals(0, pr1.getComment().size());
		
		PropertyEditor commEditor = em.getEditorForProperty("comment", pr1.getModelInterface());
		assertNotNull(commEditor);
		assertTrue(commEditor.isMultipleCardinality());
		commEditor.setValueToBean("one", pr1);
		commEditor.setValueToBean("two", pr1);
		assertEquals(2, pr1.getComment().size());
	}

	/**
	 * This test helps understand paxtools simple IO
	 * BioPAX property editors better and use them 
	 * with confidence.
	 */
	@Test
	public void testClearInverseProperty() {    	
		BioPAXFactory fac = BioPAXLevel.L3.getDefaultFactory();
	    EditorMap em = SimpleEditorMap.L3;
	    
	    ProteinReference pr = fac.create(ProteinReference.class, "PR");
	    Protein p = fac.create(Protein.class, "P");
	    p.setEntityReference(pr);
	    assertNotNull(p.getEntityReference());
	    assertFalse(pr.getEntityReferenceOf().isEmpty());
	    
	    Set<ObjectPropertyEditor> prInvPropertyEditors = em.getInverseEditorsOf(pr);
//	    System.out.println("PR inv. props are:" + prInvPropertyEditors); 
	    ObjectPropertyEditor editor = null;
	    PropertyEditor foo = null;
	    for(PropertyEditor pe : prInvPropertyEditors) {
	    	if(pe.getProperty().equals("entityReference")) {
	    		// an inverse editor is actually normal editor, 
	    		// for which it's possible to get the inverse prop. accessor
	    		editor = (ObjectPropertyEditor) pe;
	    	} else if (pe.getProperty().equals("name")) {
	    		foo = pe;
	    	}
	    }
	    assertNotNull(editor);
	    assertNull(foo);
	    
	    assertTrue(editor.isInverseMultipleCardinality());
	    assertFalse(editor.isMultipleCardinality());
	    
	    ObjectPropertyEditor bar = null;
	    for(PropertyEditor pe : em.getEditorsOf(p)) {
	    	if(pe.getProperty().equals("entityReference")) {
	    		bar = (ObjectPropertyEditor) pe;
	    	} 
	    }
	    assertNotNull(bar);
	    // an inverse editor is actually normal editor
	    // (it matters how to use it though)
	    assertEquals(editor, bar);	    
	    
	    Set<ProteinReference> prs = editor.getValueFromBean(p);
	    assertEquals(1, prs.size());
	    assertEquals(pr, prs.iterator().next());
	    
	    Set<Protein> ps = editor.getInverseAccessor().getValueFromBean(pr);
	    assertEquals(1, ps.size());
	    assertEquals(p, ps.iterator().next());
	    
	    
	    // what if one wants to clear pr.entityReferenceOf() this way 
	    // (not having a reference to parent 'p' yet) 
	    for(Protein val : ps) {
	    	assertEquals(p, val); // loop body is called once anyway
	    	
	    	//a) should not change anything or fails
	    	
	    	if(editor.isInverseMultipleCardinality()) {//- wrong/useless use; it may lead to surprising results and (shelved for later...)
	    		try {
	    			editor.removeValueFromBean(val, pr); //wrong order! (it might not throw any exception...)
	    		} catch (Throwable e) {
	    			//assertions enabled or method changed (throws anexception now)
				}
	    		assertNotNull(p.getEntityReference()); //unchanged
	    		assertFalse(pr.getEntityReferenceOf().isEmpty());  //unchanged
	    	}
	    	
	    	if(editor.isMultipleCardinality()) {
	    		editor.removeValueFromBean(pr, val); 
	    		fail("correct use but never happens ;-)");
	    	}
	    	
	    	// let's clear it "by mistake";
	    	// it actually calls setValueToBean internally
	    	// so can be used always!
	    	editor.removeValueFromBean(pr, val); 
	    	
    		assertNull(p.getEntityReference());
    		assertTrue(pr.getEntityReferenceOf().isEmpty());
	    	
    		// well, let's set and clear it normal way, at last -
    		p.setEntityReference(pr);
    		if(!editor.isMultipleCardinality()) {
    			editor.setValueToBean(null, val); //good!
	    	} else 
	    		fail("impossible!");
    		
			assertNull(p.getEntityReference());
			assertTrue(pr.getEntityReferenceOf().isEmpty());
    		
	    	break;
	    }
	    
	}

}