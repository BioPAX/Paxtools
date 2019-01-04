package org.biopax.paxtools.query;

import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.query.algorithm.LimitType;
import org.biopax.paxtools.query.wrapperL3.DataSourceFilter;
import org.biopax.paxtools.query.wrapperL3.Filter;
import org.biopax.paxtools.query.wrapperL3.OrganismFilter;
import org.biopax.paxtools.query.wrapperL3.UbiqueFilter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ozgun Babur
 */
public class QueryTest
{
	static BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	public void testQueries() throws Throwable
	{
		Model model = handler.convertFromOWL(QueryTest.class.getResourceAsStream(
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
		assertTrue(result.size() == 10);

		// check if bothstream and undirected neighborhood works properly

		source = findElements(model, "HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN1632_1_9606"); // ERT2
		PhysicalEntity check = (PhysicalEntity) model.getByID("HTTP://WWW.REACTOME.ORG/BIOPAX/48887#COMPLEX1115_1_9606"); // Activated RAF1 complex

		result = QueryExecuter.runNeighborhood(source, model, 3, Direction.BOTHSTREAM);
		assertFalse(result.contains(check));
		result = QueryExecuter.runNeighborhood(source, model, 3, Direction.UNDIRECTED);
		assertTrue(result.contains(check));


//		Model clonedModel = excise(model, result);
//		handler.convertToOWL(clonedModel, new FileOutputStream(
//			getClass().getResource("").getFile() + File.separator + "temp.owl"));
	}

	private Model excise(Model model, Set<BioPAXElement> result)
	{
		Completer c = new Completer(SimpleEditorMap.L3);

		result = c.complete(result, model);

		Cloner cln = new Cloner(SimpleEditorMap.L3, BioPAXLevel.L3.getDefaultFactory());

		return cln.clone(model, result);
	}

	protected static Set<BioPAXElement> findElements(Model model, String... ids)
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
	
	@Test
	@Ignore
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
	public void testFilters()
	{
		Model model = handler.convertFromOWL(this.getClass().getResourceAsStream(
			"raf_map_kinase_cascade_reactome.owl"));

		Set<BioPAXElement> source = findElements(model,
			"HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN2360_1_9606"); //MEK2
		Set<BioPAXElement> target = findElements(model,
			"HTTP://WWW.REACTOME.ORG/BIOPAX/48887#PROTEIN1631_1_9606"); //ERK1

		// test organism filter

		Filter f = new OrganismFilter(new String[]{"Homo sapiens"});
		Set<BioPAXElement> result = QueryExecuter.runPathsFromTo(
			source, target, model, LimitType.NORMAL, 2, f);
		assertTrue(!result.isEmpty());

		f = new OrganismFilter(new String[]{"Non-existing organism"});
		result = QueryExecuter.runPathsFromTo(source, target, model, LimitType.NORMAL, 2, f);
		assertTrue(result.isEmpty());

		
		f = new OrganismFilter(new String[]{"9606"});
		result = QueryExecuter.runPathsFromTo(
			source, target, model, LimitType.NORMAL, 2, f);
		assertTrue(!result.isEmpty());
		
		// test data source filter

		f = new DataSourceFilter(new String[]{"Reactome"});
		result = QueryExecuter.runPathsFromTo(source, target, model, LimitType.NORMAL, 2, f);
		assertTrue(!result.isEmpty());

		f = new DataSourceFilter(new String[]{"Some DB"});
		result = QueryExecuter.runPathsFromTo(source, target, model, LimitType.NORMAL, 2, f);
		assertTrue(result.isEmpty());

		// test both

		result = QueryExecuter.runPathsFromTo(source, target, model, LimitType.NORMAL, 2,
			new OrganismFilter(new String[]{"Homo sapiens"}), 
			new DataSourceFilter(new String[]{"Reactome"}));
		assertTrue(!result.isEmpty());

		// test ubique filter

		source = findElements(model,
			"HTTP://WWW.REACTOME.ORG/BIOPAX/48887#SMALLMOLECULE5_1_9606"); //ATP
		target = findElements(model,
			"HTTP://WWW.REACTOME.ORG/BIOPAX/48887#SMALLMOLECULE6_1_9606"); //ADP

		Set<String> ubiqueIDs = new HashSet<String>(Arrays.asList("Some ID", "Another ID"));

		result = QueryExecuter.runPathsFromTo(source, target, model, LimitType.NORMAL, 1,
			new UbiqueFilter(ubiqueIDs));

		assertTrue(!result.isEmpty());

		ubiqueIDs = new HashSet<String>(Arrays.asList(
			"HTTP://WWW.REACTOME.ORG/BIOPAX/48887#SMALLMOLECULE5_1_9606"));

		result = QueryExecuter.runPathsFromTo(source, target, model, LimitType.NORMAL, 1,
			new UbiqueFilter(ubiqueIDs));

		assertTrue(result.isEmpty());
	}
}
