package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.pattern.constraint.*;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class TempTests
{
	Model model;

	@Before
	public void setUp() throws Exception
	{
		SimpleIOHandler h = new SimpleIOHandler();
		model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/PC.owl"));
	}

	@Test
	public void namesOfPathwaysThatContainReversibleReactions()
	{
		Pattern p = new Pattern(Pathway.class, "Pathway");
		p.addConstraint(new PathConstraint("Pathway/pathwayComponent:Conversion"), "Pathway", "Conv");
		p.addConstraint(new Field("Conversion/conversionDirection", ConversionDirectionType.REVERSIBLE), "Conv");
		p.addConstraint(new PathConstraint("Conversion/controlledOf:Catalysis"), "Conv", "Cat");
		p.addConstraint(new Empty(new PathConstraint("Catalysis/catalysisDirection")), "Cat");
		p.addConstraint(new Empty(new PathConstraint("Pathway/pathwayComponent:Pathway")), "Pathway");

		List<Pathway> pats = new ArrayList<Pathway>(
			Searcher.searchAndCollect(model, p, 0, Pathway.class));

		Collections.sort(pats, new Comparator<Pathway>()
		{
			@Override
			public int compare(Pathway o1, Pathway o2)
			{
				return new Integer(o1.getPathwayComponent().size()).compareTo(
					o2.getPathwayComponent().size());
			}
		});

		for (Pathway pat : pats)
		{
			System.out.println(pat.getPathwayComponent().size() + "\t" + pat.getDisplayName());
			System.out.println(pat.getRDFId());
		}
	}
}
