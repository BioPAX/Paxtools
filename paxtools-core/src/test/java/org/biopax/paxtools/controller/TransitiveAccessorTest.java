package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 */
public class TransitiveAccessorTest
{
	@Test
	public void transitiveGet()
	{
		Model model = new MockFactory(BioPAXLevel.L3).createModel();
		Complex outer = model.addNew(Complex.class, "outer");
		Complex inner = model.addNew(Complex.class, "inner");
		Complex innermost = model.addNew(Complex.class, "innermost");
		Protein base = model.addNew(Protein.class, "base");

		outer.addComponent(inner);
		inner.addComponent(innermost);
		innermost.addComponent(base);

		ObjectPropertyEditor<Complex, PhysicalEntity> pe =
				(ObjectPropertyEditor<Complex, PhysicalEntity>) SimpleEditorMap.get(BioPAXLevel.L3).getEditorForProperty(
						"component", Complex.class);
		TransitivePropertyAccessor<PhysicalEntity, Complex> ta = TransitivePropertyAccessor.create(pe);
		Set components = ta.getValueFromBean(outer);
		assertEquals(3,components.size());
		assertTrue(components.contains(base));
		assertTrue(components.contains(inner));
		assertTrue(components.contains(innermost));
		
		
		//create a loop (bad idea, but it does happen in real data, not only with 'component' prop.)
		components.clear();
		innermost.addComponent(outer);
		inner.addComponent(outer);
		ta = TransitivePropertyAccessor.create(pe);
		components = ta.getValueFromBean(outer); //should not fail or loop infinitely
		assertTrue(components.size() == 4);
		assertTrue(components.contains(base));
		assertTrue(components.contains(inner));
		assertTrue(components.contains(innermost));
		assertTrue(components.contains(outer));
	}
}
