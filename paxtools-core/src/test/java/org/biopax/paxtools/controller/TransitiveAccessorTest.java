package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 */
public class TransitiveAccessorTest
{
	@Test
	public void testTransitiveGet()
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
		TransitivePropertyAccessor<Complex, PhysicalEntity> ta = TransitivePropertyAccessor.create(pe);
		Set components = ta.getValueFromBean(outer);
		assertThat(true, is(components.size() == 3));
		assertThat(true, is(components.contains(base)));
		assertThat(true, is(components.contains(inner)));
		assertThat(true, is(components.contains(innermost)));

	}
}
