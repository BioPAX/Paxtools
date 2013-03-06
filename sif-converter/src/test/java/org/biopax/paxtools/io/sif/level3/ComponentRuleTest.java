package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.COMPONENT_OF;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.IN_SAME_COMPONENT;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 */
public class ComponentRuleTest
{
	@Test
	public void testComponentRule()
	{
		MockFactory mock = new MockFactory(BioPAXLevel.L3);
		final Model model = mock.createModel();

		Protein[] p = mock.create(model, Protein.class, 3);
		ProteinReference[] pr = mock.create(model, ProteinReference.class, 3);
		mock.bindArrays("entityReference",p,pr);

		Complex[] cp = mock.create(model, Complex.class, 3);

		mock.bindInPairs("component",
		                 cp[1],cp[2],
		                 cp[1],p[1],
		                 cp[1],p[2]);

		HashMap options = new HashMap();
		options.put(IN_SAME_COMPONENT, true);
		options.put(COMPONENT_OF, true);

		ComponentRule rule = new ComponentRule();
		SimpleInteractionConverter sic = new SimpleInteractionConverter(options, rule);
		InteractionSetL3 interactions = (InteractionSetL3) sic.inferInteractions(model);

		Map<BioPAXElement, Group> e2g = interactions.getGroupMap().getMap();

		List<SimpleInteraction> expected =
				Arrays.asList(new SimpleInteraction(e2g.get(cp[2]), e2g.get(cp[1]), COMPONENT_OF),
				              new SimpleInteraction(pr[1], e2g.get(cp[1]), COMPONENT_OF),
				              new SimpleInteraction(pr[2], e2g.get(cp[1]), COMPONENT_OF),
				              new SimpleInteraction(pr[1], pr[2], IN_SAME_COMPONENT),
				              new SimpleInteraction(pr[1], e2g.get(cp[2]), IN_SAME_COMPONENT),
				              new SimpleInteraction(pr[2], e2g.get(cp[2]), IN_SAME_COMPONENT));
		assertThat(true, is(interactions.containsAll(expected)));

	}
}

