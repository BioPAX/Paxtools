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

import static org.biopax.paxtools.io.sif.BinaryInteractionType.SEQUENTIAL_CATALYSIS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class ConsecutiveCatalysisRuleTest
{

	/**
	 * This is the test graph
	 *
		 * sm0    p0     sm2                    p2
		 *    \    |    /                        |
		 *     == br0 =>sm3      p1     sm4 <==br2==sm5
		 *    /            \      |    /
		 * sm1               == br0 ==> sm6<==br3==>sm7
		 *                                      |
		 *                                     p3
	*/

	@Test
	public void testCompzzonentRule()
	{
		MockFactory mock = new MockFactory(BioPAXLevel.L3);
		final Model model = mock.createModel();

		Protein[] p = mock.create(model, Protein.class, 4);
		ProteinReference[] pr = mock.create(model, ProteinReference.class, 4);
		mock.bindArrays(mock.editor("entityReference", Protein.class),p,pr);

		SmallMolecule[] sm = mock.create(model, SmallMolecule.class,8);
		BiochemicalReaction[] br = mock.create(model, BiochemicalReaction.class, 4);
		br[0].setConversionDirection(ConversionDirectionType.LEFT_TO_RIGHT);
		br[1].setConversionDirection(ConversionDirectionType.LEFT_TO_RIGHT);
		br[2].setConversionDirection(ConversionDirectionType.RIGHT_TO_LEFT);
		br[3].setConversionDirection(ConversionDirectionType.REVERSIBLE);

		mock.bindInPairs(mock.editor("left",BiochemicalReaction.class),
		                 br[0],sm[0],
		                 br[0],sm[1],
		                 br[1],sm[3],
		                 br[2],sm[4],
		                 br[3],sm[6]);

		mock.bindInPairs(mock.editor("right",BiochemicalReaction.class),
		                 br[0],sm[2],
		                 br[0],sm[3],
		                 br[1],sm[4],
		                 br[1],sm[6],
		                 br[2],sm[5],
		                 br[3],sm[7]);

		Catalysis[] x = mock.create(model, Catalysis.class,4);
		mock.bindArrays(mock.editor("controlled",Catalysis.class),x,br);
		mock.bindArrays(mock.editor("controller",Catalysis.class),x,p);

		HashMap options = new HashMap();
		ConsecutiveCatalysisRule rule = new ConsecutiveCatalysisRule();
		SimpleInteractionConverter sic = new SimpleInteractionConverter(options, rule);
		InteractionSetL3 interactions = (InteractionSetL3) sic.inferInteractions(model);

		List<SimpleInteraction> expected =
				Arrays.asList(new SimpleInteraction(pr[0], pr[1],SEQUENTIAL_CATALYSIS),
				              new SimpleInteraction(pr[1], pr[3],SEQUENTIAL_CATALYSIS));
		assertTrue(interactions.containsAll(expected));
		assertEquals(2, interactions.size());

	}
}
