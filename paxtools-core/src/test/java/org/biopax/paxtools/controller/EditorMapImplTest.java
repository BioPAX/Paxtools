package org.biopax.paxtools.controller;


import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EditorMapImplTest
{
	@Test
	public void testGetKnownSubClassesOf() throws Exception
	{
		Set<PropertyEditor<? extends BioPAXElement,?>> organism = SimpleEditorMap.L3
				.getSubclassEditorsForProperty("organism", BioPAXElement.class);
		assertTrue(organism.size() == 3);
	}

	@Test
	public void testSomePropertyRange() {
		PropertyEditor<? extends BioPAXElement,?> entityReferencePropEditor
				= SimpleEditorMap.L3.getEditorForProperty("entityReference", Protein.class);
		assertEquals(EntityReference.class, entityReferencePropEditor.getRange());
		//TODO: entityReferencePropEditor.getRange() should probably be specific sub-class, ProteinReference here, taking OWL restrictions into account...
		entityReferencePropEditor
				= SimpleEditorMap.L3.getEditorForProperty("entityReference", Gene.class);
		assertNull(entityReferencePropEditor);
	}
}
