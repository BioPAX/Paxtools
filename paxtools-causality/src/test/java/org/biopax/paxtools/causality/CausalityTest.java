package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.analysis.BFS;
import org.biopax.paxtools.causality.analysis.Exhaustive;
import org.biopax.paxtools.causality.model.*;
import org.biopax.paxtools.causality.util.Histogram;
import org.biopax.paxtools.causality.wrapper.Graph;
import org.biopax.paxtools.causality.wrapper.PhysicalEntityWrapper;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.SimpleMerger;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.query.model.GraphObject;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class CausalityTest
{
	@Test
	public void testExhaustiveReach()
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(getClass().getResourceAsStream("AR-TP53.owl"));

		Graph graph = new Graph(model, null);

		Node source = (Node) graph.getGraphObject(model.getByID(ar_p53_sourceID));

		final Set<String> visit = new HashSet<String>(ar_p53_visitList);
		final Set<String> invalidVisited = new HashSet<String>();
		final Set<String> noVisit = new HashSet<String>(ar_p53_noVisitList);

		PathUser user = new PathUser()
		{
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
		};
		
		new Exhaustive(source, Direction.DOWNSTREAM, 3, user).run();

		for (String s : invalidVisited) System.out.println("invalid visited = " + s);
		for (String s : visit) System.out.println("failed to visit = " + s);

		assertTrue(visit.isEmpty());
		assertTrue(invalidVisited.isEmpty());
		
		visit.addAll(ar_p53_visitList);

		new Exhaustive(source, Direction.DOWNSTREAM, 2, user).run();

		assertTrue(visit.size() == 3);
		assertTrue(invalidVisited.isEmpty());
	}

	final static String ar_p53_sourceID = "http://pid.nci.nih.gov/biopaxpid_35449";
	final static List<String> ar_p53_visitList = Arrays.asList(
		"http://pid.nci.nih.gov/biopaxpid_21741", //GNB1/GNG2
		"http://pid.nci.nih.gov/biopaxpid_31826", //Gi family/GTP
		"http://pid.nci.nih.gov/biopaxpid_623", //Gi family
		"http://pid.nci.nih.gov/biopaxpid_685", //GTP
		"http://pid.nci.nih.gov/biopaxpid_678", //GDP
		"http://pid.nci.nih.gov/biopaxpid_641", //GNAZ
		"http://pid.nci.nih.gov/biopaxpid_17089", //p38alpha-beta-active
		"http://pid.nci.nih.gov/biopaxpid_17092", //p38 beta
		"http://pid.nci.nih.gov/biopaxpid_3848", //p38 alpha
		"http://pid.nci.nih.gov/biopaxpid_62292", //p53
		"http://pid.nci.nih.gov/biopaxpid_42702", //p53
		"http://pid.nci.nih.gov/biopaxpid_42438" //p53 (tetramer)
	);

	final static List<String> ar_p53_noVisitList = Arrays.asList(
		"http://pid.nci.nih.gov/biopaxpid_35409", //Gi family/GNB1/GNG2/GDP
		"http://pid.nci.nih.gov/biopaxpid_21151", //p38alpha-beta
		"http://pid.nci.nih.gov/biopaxpid_2166", //p53
		"http://pid.nci.nih.gov/biopaxpid_17220", //p53
		"http://pid.nci.nih.gov/biopaxpid_42384" //p53 (tetramer)
	);

	@Test
	public void testBFSReach()
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(getClass().getResourceAsStream("AR-TP53.owl"));

		Graph graph = new Graph(model, null);

		Node source = (Node) graph.getGraphObject(model.getByID(ar_p53_sourceID));

		BFS bfs = new BFS(Collections.singleton(source), null, Direction.DOWNSTREAM, 3);
		Map<GraphObject, Integer> res = bfs.run();

		Set<String> visit = new HashSet<String>(ar_p53_visitList);

		for (GraphObject go : res.keySet())
		{
			if (go instanceof PhysicalEntityWrapper)
			{
				PhysicalEntityWrapper pew = (PhysicalEntityWrapper) go;

				assertTrue(pew.getPathSign() == 1);

				PhysicalEntity pe = pew.getPhysicalEntity();
				visit.remove(pe.getRDFId());
				assertFalse(ar_p53_noVisitList.contains(pe.getRDFId()));
			}
		}

		for (String s : visit) System.out.println("failed to visit = " + s);
		assertTrue(visit.isEmpty());

		bfs = new BFS(Collections.singleton(source), null, Direction.DOWNSTREAM, 2);
		res = bfs.run();

		visit = new HashSet<String>(ar_p53_visitList);

		for (GraphObject go : res.keySet())
		{
			if (go instanceof PhysicalEntityWrapper)
			{
				PhysicalEntityWrapper pew = (PhysicalEntityWrapper) go;

				assertTrue(pew.getPathSign() == 1);

				PhysicalEntity pe = pew.getPhysicalEntity();
				visit.remove(pe.getRDFId());
				assertFalse(ar_p53_noVisitList.contains(pe.getRDFId()));
			}
		}

		assertTrue(visit.size() == 3);
	}

	@Test
	public void testCausativePathSearch()
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(getClass().getResourceAsStream("AR-TP53.owl"));

		List<Path> paths = CausalityExecuter.findCausativePaths(model, new AlterationProvider()
		{
			final AlterationPack pack_AR = new AlterationPack();
			final AlterationPack pack_TP53 = new AlterationPack();
			final AlterationPack pack_GNAZ = new AlterationPack();

			@Override
			public AlterationPack getAlterations(Node node)
			{
				if (node instanceof PhysicalEntityWrapper)
				{
					PhysicalEntityWrapper pew = (PhysicalEntityWrapper) node;
					PhysicalEntity pe = pew.getPhysicalEntity();
					
					if (pe.getDisplayName().equals("AR"))
					{
						if (pack_AR.getSize() == 0)
						{
							pack_AR.put(Alteration.MUTATION,
								new Change[]{Change.ACTIVATING, Change.ACTIVATING});
							pack_AR.complete();
						}
						return pack_AR;
					}
					else if (pe.getDisplayName().equals("p53"))
					{
						if (pack_TP53.getSize() == 0)
						{
							pack_TP53.put(Alteration.PROTEIN_LEVEL,
								new Change[]{Change.ACTIVATING, Change.ACTIVATING});
							pack_TP53.complete();
						}
						return pack_TP53;
					}
					else if (pe.getDisplayName().equals("GNAZ"))
					{
						if (pack_GNAZ.getSize() == 0)
						{
							pack_GNAZ.put(Alteration.PROTEIN_LEVEL,
								new Change[]{Change.ACTIVATING, Change.ACTIVATING});
							pack_GNAZ.complete();
						}
						return pack_GNAZ;
					}
				}
				return null;
			}
		}, 3, 0.5, null);

		assertTrue(paths.size() == 7);

		for (Path path : paths)
		{
			System.out.println(path);
		}
	}

	@Test
	@Ignore
	public void loadTest() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model1 = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/nci.owl"));
		Model model2 = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/reactome.owl"));

		SimpleMerger m = new SimpleMerger(SimpleEditorMap.get(BioPAXLevel.L3));
		m.merge(model1, model2);
		
		h.convertToOWL(model1, new FileOutputStream("/home/ozgun/Desktop/all.owl"));
	}

	@Test
	@Ignore
	public void activityTest() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/humancyc.owl"));

		Map<EntityReference, Integer> complexCount = new HashMap<EntityReference, Integer>();
		Map<EntityReference, Integer> activityCount = new HashMap<EntityReference, Integer>();
		
		for (EntityReference er : model.getObjects(EntityReference.class))
		{
			for (SimplePhysicalEntity pe : er.getEntityReferenceOf())
			{
				for (Complex cmp : pe.getComponentOf())
				{
					if (associatedWithAConversion(cmp))
					{
						increaseCount(er, complexCount);
						if (!cmp.getControllerOf().isEmpty())
						{
							increaseCount(er, activityCount);
						}
					}
				}
			}
		}

		Histogram hist = new Histogram(0.1);

		for (EntityReference er : complexCount.keySet())
		{
			if (complexCount.get(er) < 1) continue;

			double ratio;

			if (!activityCount.containsKey(er))
			{
				ratio = 0;
			}
			else
			{
				ratio = activityCount.get(er) / (double) complexCount.get(er);
			}

			hist.count(ratio);

			if (ratio == 0 && complexCount.get(er) > 5)
			{
				for (String s : er.getName())
				{
					System.out.println("s = " + s);
				}
			}
		}

		hist.print();
	}
	
	private boolean associatedWithAConversion(PhysicalEntity pe)
	{
		for (Interaction inter : pe.getParticipantOf())
		{
			if (inter instanceof Conversion) return true;
		}
		return false;
	}
	
	private void increaseCount(EntityReference er, Map<EntityReference, Integer> count)
	{
		if (!count.containsKey(er)) count.put(er, 0);
		count.put(er, count.get(er) + 1);
	}
}
