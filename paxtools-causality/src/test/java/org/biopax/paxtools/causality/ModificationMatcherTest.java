package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.util.Histogram;
import org.biopax.paxtools.causality.util.Kronometre;
import org.biopax.paxtools.causality.util.TermCounter;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ModificationMatcherTest
{
	@Test
	@Ignore
	public void testModificationOverlap() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/PC.owl"));

		ModificationMatcher mm = new ModificationMatcher();
		Histogram hist = mm.getModificationFeatureOverlapHistogram(model);
		hist.print();
		for (Provenance prov : mm.tc.keySet())
		{
			System.out.println("prov = " + prov);
			mm.tc.get(prov).print(1);
		}
	}

	@Test
	@Ignore
	public void countMods() throws Throwable
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/PC.owl"));

		TermCounter tc = new TermCounter();
		Set<SequenceModificationVocabulary> set = new HashSet<SequenceModificationVocabulary>();
		for (ModificationFeature mf : model.getObjects(ModificationFeature.class))
		{
			SequenceModificationVocabulary type = mf.getModificationType();
			if (type != null)
			{
				set.add(type);
//				Set<String> term = type.getTerm();
//				if (term != null && !term.isEmpty())
//				{
//					String t = term.iterator().next();
//					tc.addTerm(t);
//				}
			}
		}

//		for (SequenceModificationVocabulary type : set)
//		{
//			Set<String> term = type.getTerm();
//			if (term != null && !term.isEmpty())
//			{
//				String t = term.iterator().next();
//				tc.addTerm(t);
//			}
//		}
//
//		tc.print();

		for (SequenceModificationVocabulary voc : set)
		{
			if (!voc.getTerm().isEmpty())
				System.out.println(voc.getTerm().iterator().next() + "\t" + voc.getRDFId());
		}
	}

	@Test
	@Ignore
	public void testDebug() throws FileNotFoundException
	{
		Kronometre k = new Kronometre();
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/PC.owl"));
		System.out.print("Model loaded in");
		k.stop();
		k.print();
		k.start();

		Map<Provenance, Set<BioSource>> map = new HashMap<Provenance, Set<BioSource>>();
		for (ProteinReference prt : model.getObjects(ProteinReference.class))
		{
			BioSource organism = prt.getOrganism();
			for (SimplePhysicalEntity spe : prt.getEntityReferenceOf())
			{
				for (Provenance prov : spe.getDataSource())
				{
					if (prov.getDisplayName() != null && prov.getDisplayName().equals("panther"))
					{
						System.out.print("");
					}

					if (!map.containsKey(prov)) map.put(prov, new HashSet<BioSource>());
					map.get(prov).add(organism);
				}
			}
		}

		for (Provenance prov : map.keySet())
		{
			System.out.println("Provenance = " + prov.getDisplayName());

			System.out.print("Organisms:");
			for (BioSource src : map.get(prov))
			{
				if (src != null) System.out.print(" " + src.getDisplayName() + ",");
				else System.out.print(" null,");
			}
			System.out.println("\n");
		}

	}
}
