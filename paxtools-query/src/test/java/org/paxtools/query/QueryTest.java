package org.paxtools.query;

import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.QueryExecuter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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

	@Test
	public void testQuery() throws Throwable
	{
		SimpleReader reader = new SimpleReader(BioPAXLevel.L3);

		Model model = reader.convertFromOWL(new FileInputStream(DIR +
			"temp2.owl"));

		Set<BioPAXElement> set = findElements(model,
			"Z-2",
			"D-1");

//		Set<BioPAXElement> result = QueryExecuter.runNeighborhood(set, model, 2, true, true);
		Set<BioPAXElement> result = QueryExecuter.runGOI(set, model, 10);


		Completer c = new Completer(EM);

		result = c.complete(result, model);

		Cloner cln = new Cloner(EM, BioPAXLevel.L3.getDefaultFactory());

		Model clonedModel = cln.clone(model, result);

		SimpleExporter se = new SimpleExporter(BioPAXLevel.L3);
		se.convertToOWL(clonedModel, new FileOutputStream(DIR + "temp.owl"));
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
}
