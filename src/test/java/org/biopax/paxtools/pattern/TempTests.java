package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.constraint.*;
import org.biopax.paxtools.pattern.util.HGNC;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
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
		model = h.convertFromOWL(new FileInputStream("All-Human-Data.owl"));
	}

	@Test
	@Ignore
	public void namesOfPathwaysThatContainReversibleReactions()
	{
		Pattern p = new Pattern(Pathway.class, "Pathway");
		p.add(new PathConstraint("Pathway/pathwayComponent:Conversion"), "Pathway", "Conv");
		p.add(new Field("Conversion/conversionDirection", ConversionDirectionType.REVERSIBLE), "Conv");
		p.add(new PathConstraint("Conversion/controlledOf:Catalysis"), "Conv", "Cat");
		p.add(new Empty(new PathConstraint("Catalysis/catalysisDirection")), "Cat");
		p.add(new Empty(new PathConstraint("Pathway/pathwayComponent:Pathway")), "Pathway");

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
	@Ignore
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

		Searcher.searchInFile(p, "All-Human-Data.owl", "Captured-conseq-catalysis.owl", 100, 1);
	}

	@Test
	@Ignore
	public void capturePattern() throws Throwable
	{
		Pattern p = PatternBox.degradation();

		Searcher.searchInFile(p, "All-Human-Data.owl", "Captured-controls-degradation.owl", 100, 1);
	}

	@Test
	@Ignore
	public void extractSignalink() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/signal-db/signalink-raw.txt"));

		Set<String> set = new HashSet<String>();
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split(";");

			if (token.length < 20) continue;

			String type = token[13];
			String source = token[0];
			String target = token[6];

			if (type.contains("undirected")) continue;
			if (!type.contains("directed")) continue;

			if (!source.isEmpty()) source = HGNC.getSymbol(source);
			if (!target.isEmpty()) target = HGNC.getSymbol(target);

			if (source != null && !source.isEmpty() && target != null && !target.isEmpty())
			{
				String s = source + "\t" + target;
				if (!set.contains(s))
				{
					set.add(s);
				}
			}
		}

		reader.close();

		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/ozgun/Desktop/signal-db/signalink.txt"));

		for (String s : set)
		{
			writer.write(s + "\n");
		}

		writer.close();

		System.out.println("set.size() = " + set.size());
	}

	@Test
	@Ignore
	public void debugPattern() throws Throwable
	{

		Pattern p = PatternBox.expressionWithTemplateReac();

		Match m = new Match(p.size());
		ProteinReference er = (ProteinReference) model.getByID("http://identifiers.org/uniprot/P42229");
		PhysicalEntity pe = (PhysicalEntity) model.getByID("http://pid.nci.nih.gov/biopaxpid_10369");
		m.set(er, 0);
		m.set(pe, 1);
		List<Match> result = Searcher.search(m, p);
		System.out.println("result.size() = " + result.size());
	}
}
