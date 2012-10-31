package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.util.Histogram;
import org.biopax.paxtools.causality.util.TermCounter;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
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
}
