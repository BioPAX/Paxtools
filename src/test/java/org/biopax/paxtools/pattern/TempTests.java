package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.constraint.*;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.*;

/**
 * @author Ozgun Babur
 */
//@Ignore
public class TempTests
{
	Model model;

	@Before
	public void setUp() throws Exception
	{
		SimpleIOHandler h = new SimpleIOHandler();
		model = h.convertFromOWL(new FileInputStream("All-Data.owl"));
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

	@Test
	public void captureConseqCataSamples() throws Throwable
	{
		Set<String> ubiq = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader("ubiquitous-ids.txt"));

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			ubiq.add(line);
		}

		reader.close();


		Pattern p = PatternBox.consecutiveCatalysis(ubiq);

		Searcher.searchInFile(p, "All-Data.owl", "Captured.owl", 100, 1);
	}

	@Test
	public void capturePattern() throws Throwable
	{
		Pattern p = PatternBox.controlsStateChange(true);

		Searcher.searchInFile(p, "All-Data.owl", "Captured-state-change.owl", 100, 1);
	}

	@Test
	public void debugPattern() throws Throwable
	{
		Pattern p = PatternBox.expressionWithTemplateReac();

		Match m = new Match(p.labelMap.size());
		ProteinReference er = (ProteinReference) model.getByID("http://identifiers.org/uniprot/P42229");
		PhysicalEntity pe = (PhysicalEntity) model.getByID("http://pid.nci.nih.gov/biopaxpid_10369");
		m.set(er, 0);
		m.set(pe, 1);
		List<Match> result = Searcher.search(m, p);
		System.out.println("result.size() = " + result.size());
	}
}
