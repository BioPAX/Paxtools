package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.analysis.Exhaustive;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.causality.model.Path;
import org.biopax.paxtools.causality.model.PathUser;
import org.biopax.paxtools.causality.wrapper.Graph;
import org.biopax.paxtools.causality.wrapper.PhysicalEntityWrapper;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.algorithm.Direction;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class CausalityTests
{
	@Test
	public void testExhaustiveReach()
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(getClass().getResourceAsStream("AR-TP53.owl"));

		Graph graph = new Graph(model, null);

		Node source = (Node) graph.getGraphObject(
			model.getByID("http://pid.nci.nih.gov/biopaxpid_35449"));

		final Set<String> visit = new HashSet<String>(Arrays.asList(
			"http://pid.nci.nih.gov/biopaxpid_21741", //GNB1/GNG2
			"http://pid.nci.nih.gov/biopaxpid_31826", //Gi family/GTP
			"http://pid.nci.nih.gov/biopaxpid_678", //GDP
			"http://pid.nci.nih.gov/biopaxpid_17089", //p38alpha-beta-active
			"http://pid.nci.nih.gov/biopaxpid_62292", //p53
			"http://pid.nci.nih.gov/biopaxpid_17092", //p38 beta
			"http://pid.nci.nih.gov/biopaxpid_3848", //p38 alpha
			"http://pid.nci.nih.gov/biopaxpid_42702", //p53
			"http://pid.nci.nih.gov/biopaxpid_42438" //p53 (tetramer)
		));

		final Set<String> invalidVisited = new HashSet<String>();

		Exhaustive ex = new Exhaustive(source, Direction.DOWNSTREAM, 3, new PathUser()
		{
			Set<String> noVisit = new HashSet<String>(Arrays.asList(
				"http://pid.nci.nih.gov/biopaxpid_685", //GTP
				"http://pid.nci.nih.gov/biopaxpid_35409", //Gi family/GNB1/GNG2/GDP
				"http://pid.nci.nih.gov/biopaxpid_21151", //p38alpha-beta
				"http://pid.nci.nih.gov/biopaxpid_2166", //p53
				"http://pid.nci.nih.gov/biopaxpid_17220", //p53
				"http://pid.nci.nih.gov/biopaxpid_42384" //p53 (tetramer)
			));

			@Override
			public void processPath(Path path)
			{
				Node node = path.getLastNode();
				if (node instanceof PhysicalEntityWrapper)
				{
					PhysicalEntityWrapper wr = (PhysicalEntityWrapper) node;
					PhysicalEntity pe = wr.getPhysicalEntity();
					visit.remove(pe.getRDFId());
					if (noVisit.contains(pe.getRDFId()))
					{
						invalidVisited.add(pe.getRDFId());
					}
				}
			}
		});
		
		ex.run();
		
		assertTrue(visit.isEmpty());
		assertTrue(invalidVisited.isEmpty());
	}
}
