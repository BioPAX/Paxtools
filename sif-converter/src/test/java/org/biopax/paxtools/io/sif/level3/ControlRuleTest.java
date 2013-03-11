package org.biopax.paxtools.io.sif.level3;


import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.METABOLIC_CATALYSIS;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.STATE_CHANGE;

/**

 */
public class ControlRuleTest
{
	@Test
	public void testControlRule()
	{

		MockFactory mock = new MockFactory(BioPAXLevel.L3);
		final Model model = mock.createModel();

		/**
		 * p0      p1     sm1
		 *    \    |    /
		 *     == br0 =>p7(p0')
		 *    /
		 * sm0
		 *
		 *
		 * p2      p4
		 *    \    |
		 *     == ca0=>c0{p5(p2'),p6(p3')}
		 *    /
		 * p3
		 *
		 *
		 */

		Protein[] p = mock.create(model, Protein.class, 8);
		ProteinReference[] pr = mock.create(model, ProteinReference.class, 5);
		mock.bindArrays("entityReference", Arrays.copyOfRange(p, 0, 5), pr);

		mock.bindInPairs("entityReference",
		                 p[7], pr[0],
		                 p[5], pr[2],
		                 p[6], pr[3]);


		SmallMolecule[] sm = mock.create(model, SmallMolecule.class, 2);
		SmallMoleculeReference[] smr = mock.create(model, SmallMoleculeReference.class, 2);
		mock.bindArrays("entityReference", sm, smr);

		Complex[] c = mock.create(model, Complex.class, 1);
		mock.bindInPairs("component",
		                 c[0], p[5],
		                 c[0], p[6]);

		BiochemicalReaction[] br = mock.create(model, BiochemicalReaction.class, 1);
		ComplexAssembly[] ca = mock.create(model, ComplexAssembly.class, 1);

		mock.bindInPairs(mock.editor("left", Conversion.class),
		                 br[0], p[0],
		                 br[0], sm[0],
		                 ca[0], p[2],
		                 ca[0], p[3]);
		mock.bindInPairs(mock.editor("right", Conversion.class),
		                 br[0], p[7],
		                 br[0], sm[1],
		                 ca[0], c[0]);

		Catalysis[] cx = mock.create(model, Catalysis.class, 2);

		mock.bindInPairs("controller",
		                 cx[0], p[1],
		                 cx[1], p[4]);
		mock.bindInPairs("controlled",
		                 cx[0], br[0],
		                 cx[1], ca[0]);

		HashMap options = new HashMap();
		options.put(METABOLIC_CATALYSIS, true);
		options.put(STATE_CHANGE, true);
		ControlRule rule = new ControlRule();
		SimpleInteractionConverter sic = new SimpleInteractionConverter(options, rule);
		InteractionSetL3 interactions = (InteractionSetL3) sic.inferInteractions(model);

		List<SimpleInteraction> expected =
				Arrays.asList(
						new SimpleInteraction(pr[1], pr[0], STATE_CHANGE),
				        new SimpleInteraction(pr[4], pr[2], STATE_CHANGE),
				        new SimpleInteraction(pr[4], pr[3], STATE_CHANGE),
				        new SimpleInteraction(pr[1], smr[0], METABOLIC_CATALYSIS),
				        new SimpleInteraction(pr[1],smr[1], METABOLIC_CATALYSIS));
		assertTrue(interactions.containsAll(expected));


	}



}
