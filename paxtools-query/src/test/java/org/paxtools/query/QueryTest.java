package org.paxtools.query;

import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.CommonStreamQuery;
import org.biopax.paxtools.query.algorithm.PoIQuery;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
@Ignore
public class QueryTest
{
	static final String DIR = "../biopax/Level3/examples/";
	static final SimpleEditorMap EM = new SimpleEditorMap(BioPAXLevel.L3);
	static BioPAXIOHandler handler =  new SimpleIOHandler(BioPAXLevel.L3);

	@Test
	public void testQuery() throws Throwable
	{
		Model model = handler.convertFromOWL(new FileInputStream(DIR +
			"temp2.owl"));

		Set<BioPAXElement> set = findElements(model,
			"Z-2",
			"D-1");

//		Set<BioPAXElement> result = QueryExecuter.runNeighborhood(set, model, 2, true, true);
		Set<BioPAXElement> result = QueryExecuter.runGOI(set, model, 10);
		
		Model clonedModel = excise(model, result);
		handler.convertToOWL(clonedModel, new FileOutputStream(DIR + "temp.owl"));
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
	
	@Test
	public void testNeignborhood() throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/merge-bmp.owl"); // this is classpath - no need to use a "separator"
		Model model = handler.convertFromOWL(in);

		BioPAXElement s1 = model.getByID("HTTP://WWW.REACTOME.ORG/BIOPAX#BMP_TYPE_II_RECEPTOR_DIMER__PLASMA_MEMBRANE__1_9606");
		BioPAXElement t1 = model.getByID("HTTP://WWW.REACTOME.ORG/BIOPAX#DIMERIC_BMP2__EXTRACELLULAR_REGION__1_9606");

		Set<BioPAXElement> source = new HashSet<BioPAXElement>();
		Set<BioPAXElement> target = new HashSet<BioPAXElement>();
		source.add(s1);
		source.add(t1);

		Set<BioPAXElement> result = QueryExecuter.runCommonStreamWithPOI(source, model, CommonStreamQuery.DOWNSTREAM, 2);
			System.out.println("result.size() = " + result.size());

		Model ex = excise(model, result);
		handler.convertToOWL(ex, new FileOutputStream("QueryResult.owl"));		
	}
}
