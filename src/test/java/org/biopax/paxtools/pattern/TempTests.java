package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.CatalysisDirectionType;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.pattern.c.*;
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
		Pattern p = new Pattern(3, Pathway.class);
		p.addConstraint(new PathConstraint("Pathway/pathwayComponent:Conversion"), 0, 1);
		p.addConstraint(new Field("Conversion/conversionDirection", ConversionDirectionType.REVERSIBLE), 1);
		p.addConstraint(new PathConstraint("Conversion/controlledOf:Catalysis"), 1, 2);
		p.addConstraint(new Empty(new PathConstraint("Catalysis/catalysisDirection")), 2);
		p.addConstraint(new Empty(new PathConstraint("Pathway/pathwayComponent:Pathway")), 0);

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
