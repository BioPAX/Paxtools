package org.paxtools.query;

import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.query.algorithm.LimitType;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class QueryTest
{
	static final SimpleEditorMap EM = SimpleEditorMap.L3;
	static BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	public void testQueries() throws Throwable
	{
		Model model = handler.convertFromOWL(this.getClass().getResourceAsStream(
			"raf_map_kinase_cascade_reactome.owl"));

		Set<BioPAXElement> source = findElements(model,
			"HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN2360_1_9606"); //MEK2

		Set<BioPAXElement> target = findElements(model,
			"HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN1631_1_9606"); //ERK1

		Set<BioPAXElement> result = QueryExecuter.runNeighborhood(
			source, model, 1, Direction.BOTHSTREAM);

		assertTrue(result.size() > 0);

		result = QueryExecuter.runPathsFromTo(source, target, model, LimitType.NORMAL, 2);
		assertTrue(result.size() > 0);

		result = QueryExecuter.runPathsFromTo(source, target, model, LimitType.NORMAL, 1);
		assertTrue(result.size() == 0);

		source.addAll(target);
		result = QueryExecuter.runPathsBetween(source, model, 2);
		assertTrue(result.size() > 0);

		result = QueryExecuter.runPathsBetween(source, model, 1);
		assertTrue(result.size() == 0);

		source = findElements(model,
			"HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN1630_1_9606", //phospho-Cdc2
			"HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN2359_1_9606"); //MEK

		result = QueryExecuter.runCommonStream(source, model, Direction.DOWNSTREAM, 1);
		assertTrue(result.size() == 1);

		result = QueryExecuter.runCommonStream(source, model, Direction.DOWNSTREAM, 2);
		assertTrue(result.size() == 5);

		result = QueryExecuter.runCommonStream(source, model, Direction.DOWNSTREAM, 3);
		assertTrue(result.size() == 10);

		source = findElements(model, "HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN1630_1_9606"); //phospho-Cdc2
		target = findElements(model, "HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN1624_1_9606"); //MEK1

		result = QueryExecuter.runPathsFromTo(source, target, model, LimitType.NORMAL, 3);
		assertTrue(result.size() == 7);

		Model clonedModel = excise(model, result);
		handler.convertToOWL(clonedModel, new FileOutputStream(
			getClass().getResource("").getFile() + File.separator + "temp.owl"));
	}

	private Model excise(Model model, Set<BioPAXElement> result)
	{
		Completer c = new Completer(EM);

		result = c.complete(result, model);

		Cloner cln = new Cloner(EM, BioPAXLevel.L3.getDefaultFactory());

		return cln.clone(model, result);
	}

	private static Set<BioPAXElement> findElements(Model model, String... ids)
	{
		Set<BioPAXElement> set = new HashSet<BioPAXElement>();

		for (String id : ids)
		{
			BioPAXElement bpe = model.getByID(id);

			if (bpe != null)
			{
				set.add(bpe);
			}
		}
		return set;
	}
	
//	@Test
	public void testQueryPerformance() throws IOException
	{
		long time = System.currentTimeMillis();

		InputStream in = getClass().getResourceAsStream("/Meiotic_Recombination.owl");
		Model model = handler.convertFromOWL(in);

		System.out.print("Read the model in ");
		long secs = (System.currentTimeMillis() - time) / 1000;
		System.out.println(secs + " secs");
		time = System.currentTimeMillis();

		BioPAXElement s1 = model.getByID("HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN6022_1_9606");
//		BioPAXElement s1 = model.getByID("HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN6017_1_9606");
		BioPAXElement t1 = model.getByID("HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN6020_1_9606");

		Set<BioPAXElement> source = new HashSet<BioPAXElement>();
//		Set<BioPAXElement> target = new HashSet<BioPAXElement>();
		source.add(s1);
		source.add(t1);

		Set<BioPAXElement> result = QueryExecuter.runCommonStreamWithPOI(
			source, model, Direction.DOWNSTREAM, 3, null);

		secs = (System.currentTimeMillis() - time);

		System.out.println("result.size() = " + result.size());
		System.out.println("milisecs = " + secs);

		Model ex = excise(model, result);
		handler.convertToOWL(ex, new FileOutputStream("QueryResult.owl"));		
	}

	@Test
	@Ignore
	public void testLevel2Neighborhood() throws Throwable
	{
		Model model = handler.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/cpath2_prepared.owl"));

		Set<BioPAXElement> source = findElements(model,
			"http://pid.nci.nih.gov/biopaxpid_75022");

		Set<BioPAXElement> result = QueryExecuter.runNeighborhood(source, model, 1, Direction.UPSTREAM, HCUbiq);
		System.out.println("result.size() = " + result.size());
		Model ex = excise(model, result);
		handler.convertToOWL(ex, new FileOutputStream("/home/ozgun/Desktop/temp.owl"));
	}

	static Set<String> HCUbiq = new HashSet<String>(Arrays.asList(
		"http://biocyc.org/biopax/biopax-level3SmallMolecule159666",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule135584",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131446",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131465",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131548",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule131525",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule132137",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule127479",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule132532",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule137847",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule137835",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule137826",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule137582",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule126025",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule125519",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule165158",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule165340",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule129851",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule129842",
		"http://biocyc.org/biopax/biopax-level3SmallMolecule129864"
	));
	
	@Test
	@Ignore
	public void getUbiques() throws Throwable
	{
		Model model = handler.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/humancyc.owl.txt"));

		BioPAXElement ele = model.getByID("http://biocyc.org/biopax/biopax-level3SmallMolecule173158");

		int i = 0;
		for (PhysicalEntity pe : model.getObjects(SmallMolecule.class))
		{
			if (pe.getParticipantOf().size() > 30)
			{
				i++;
//				System.out.println(pe.getDisplayName());
				System.out.println("\"" + pe.getRDFId() + "\",");
			}
		}
		System.out.println("i = " + i);
	}

	@Test
	@Ignore
	/**
	 * Below method is for using during debugging queries.
	 */
	public void testForDebug() throws Throwable
	{
		Model model = handler.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/temp.owl"));

		Set<BioPAXElement> source = new HashSet<BioPAXElement>();
		source.add(model.getByID("http://pid.nci.nih.gov/biopaxpid_7379"));

		Set<BioPAXElement> target = new HashSet<BioPAXElement>();
		target.add(model.getByID("http://pid.nci.nih.gov/biopaxpid_73642"));

		Set<BioPAXElement> result = QueryExecuter.runPathsFromTo(
			source, target, model, LimitType.NORMAL, 1);

		System.out.println("result.size() = " + result.size());
	}


}
