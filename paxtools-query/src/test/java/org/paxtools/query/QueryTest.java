package org.paxtools.query;

import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.CommonStreamQuery;
import org.biopax.paxtools.query.algorithm.Direction;
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
	static final SimpleEditorMap EM = SimpleEditorMap.L3;
	static BioPAXIOHandler handler =  new SimpleIOHandler();

//	@Test
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
	public void testNeighborhood() throws IOException
	{
		long time = System.currentTimeMillis();

		InputStream in = getClass().getResourceAsStream("/Meiotic_Recombination.owl");
//		InputStream in = getClass().getResourceAsStream("/cpath2_main_pro.owl");
//		InputStream in = getClass().getResourceAsStream("/merge-bmp.owl");
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

//		Set<Pathway> pset = new HashSet<Pathway>();
//		for (Interaction inter : ((Protein) s1).getParticipantOf())
//		{
//			for (Pathway p : inter.getPathwayComponentOf())
//			{
//				pset.add(p);
//			}
//		}
//		for (Pathway p : pset)
//		{
//			if (!p.getName().isEmpty())
//			{
//				String name = p.getName().iterator().next();
//				System.out.println("p.name = " + name + "\tsize=" + p.getPathwayComponent().size());
//
//				Set<BioPAXElement> set = new HashSet<BioPAXElement>();
//				set.add(p);
//				Model ptw = excise(model, set);
//				handler.convertToOWL(ptw, new FileOutputStream(name.replace(" ", "_") + ".owl"));
//			}
//		}


//		Set<BioPAXElement> result = QueryExecuter.runNeighborhood(source, model, 1, Direction.BOTHSTREAM);
//		Set<BioPAXElement> result = QueryExecuter.runGOI(source, model, 3);
		Set<BioPAXElement> result = QueryExecuter.runCommonStreamWithPOI(source, model, Direction.DOWNSTREAM, 3);

		secs = (System.currentTimeMillis() - time);

		System.out.println("result.size() = " + result.size());
		System.out.println("milisecs = " + secs);

		Model ex = excise(model, result);
		handler.convertToOWL(ex, new FileOutputStream("QueryResult.owl"));		
	}
}
