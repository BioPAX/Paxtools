package org.biopax.paxtools.controller;


import org.biopax.paxtools.model.BioPAXElement;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 */
public class EditorMapImplTest
{
	@Test
	public void testGetKnownSubClassesOf() throws Exception
	{
		Set<PropertyEditor<? extends BioPAXElement,?>> organism = SimpleEditorMap.L3.getSubclassEditorsForProperty(
				"organism", BioPAXElement.class);
		assertTrue(organism.size() == 3);
	}

}
