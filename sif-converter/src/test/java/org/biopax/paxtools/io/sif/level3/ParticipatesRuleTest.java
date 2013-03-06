package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.*;

/**
 */
public class ParticipatesRuleTest
{
	@Test
	public void testParticipatesRule()
	{

		MockFactory mock = new MockFactory(BioPAXLevel.L3);
		final Model model = mock.createModel();

		/**
		 * p0             sm1
		 *    \          /
		 *     == br0 =>p7(p0')
		 *    /
		 * sm0
		 *
		 *
		 * p2
		 *    \
		 *     == ca0=>c0{p5(p2'),p6(p3')}
		 *    /
		 * p3
		 *
		 *
		 * p0===p1
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

				MolecularInteraction[] mi = mock.create(model, MolecularInteraction.class, 1);
				mi[0].addParticipant(p[1]);
				mi[0].addParticipant(p[2]);

				HashMap options = new HashMap();
				options.put(REACTS_WITH, true);
				options.put(INTERACTS_WITH, true);
				ParticipatesRule rule = new ParticipatesRule();
				SimpleInteractionConverter sic = new SimpleInteractionConverter(options, rule);
				InteractionSetL3 interactions = (InteractionSetL3) sic.inferInteractions(model);

				Map<BioPAXElement, Group> e2g = interactions.getGroupMap().getMap();

				List<SimpleInteraction> expected =
						Arrays.asList(
							new SimpleInteraction(pr[0], smr[0], REACTS_WITH),
							new SimpleInteraction(pr[0],smr[1],REACTS_WITH),
							new SimpleInteraction(smr[0], smr[1], REACTS_WITH),
							new SimpleInteraction(pr[2], e2g.get(c[0]), REACTS_WITH),
							new SimpleInteraction(pr[2], pr[3], REACTS_WITH),
							new SimpleInteraction(pr[3], e2g.get(c[0]), REACTS_WITH),
							new SimpleInteraction(pr[1], pr[2], INTERACTS_WITH));
				assertTrue(interactions.containsAll(expected));
	}
}
