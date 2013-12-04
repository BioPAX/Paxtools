package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.constraint.*;
import org.biopax.paxtools.pattern.miner.*;
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
//		SimpleIOHandler h = new SimpleIOHandler();
//		model = h.convertFromOWL(new FileInputStream("All-Human-Data.owl"));
	}

	@Test
	@Ignore
	public void namesOfPathwaysThatContainReversibleReactions()
	{
		Pattern p = new Pattern(Pathway.class, "Pathway");
		p.add(new PathConstraint("Pathway/pathwayComponent:Conversion"), "Pathway", "Conv");
		p.add(new Field("Conversion/conversionDirection", Field.Operation.INTERSECT,
			ConversionDirectionType.REVERSIBLE), "Conv");
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


		Pattern p = PatternBox.catalysisPrecedes(ubiq);

		Searcher.searchInFile(p, "All-Human-Data.owl", "Captured-conseq-catalysis.owl", 100, 1);
	}

	@Test
	@Ignore
	public void capturePattern() throws Throwable
	{
		Pattern p = PatternBox.controlsDegradation();

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


	@Test
	@Ignore
	public void searchAndWriteWithMiners() throws Throwable
	{
		Set<String> blacklist = readBlacklist();
		SIFMiner[] miner = new SIFMiner[]{
			new ControlsStateChangeOfMiner(),
			new CSCOButIsParticipantMiner(),
			new CSCOBothControllerAndParticipantMiner(),
			new CSCOThroughControllingSmallMoleculeMiner(blacklist),
			new CSCOThroughBindingSmallMoleculeMiner(blacklist)};

		for (int i = 0; i < miner.length; i++)
		{
			System.out.println("i = " + i);
			Map<BioPAXElement,List<Match>> matches =
				Searcher.search(model, miner[i].getPattern(), null);

			FileOutputStream os = new FileOutputStream(miner[i].getName() + ".txt");
			miner[i].writeResult(matches, os);
		}
	}

	@Test
	@Ignore
	public void printVennIntersections() throws FileNotFoundException
	{
		String[] file = new String[]{"changes-state-of.txt", "cso-but-a-participant.txt",
			"cso-both-ctrl-part.txt", "cso-through-controlling-small-mol.txt",
			"cso-through-binding-small-mol.txt"};
		String[] let = new String[]{"A", "B", "C", "D", "E"};

		Set<String>[] sets = new Set[file.length];

		for (int i = 0; i < file.length; i++)
		{
			sets[i] = readPairsFromSIFFile(file[i], true);
		}

		for (int i = 1; i < Math.pow(2, file.length); i++)
		{
			Set<Set<String>> intersectSets = new HashSet<Set<String>>();
			Set<Set<String>> subtractSets = new HashSet<Set<String>>();
			String name = "";

			for (int j = 0; j < file.length; j++)
			{
				if ((i / (int) Math.pow(2, j)) % 2 == 1)
				{
					intersectSets.add(sets[j]);
					name += let[j];
				}
				else
				{
					subtractSets.add(sets[j]);
				}
			}

			boolean first = true;
			Set<String> set = new HashSet<String>();
			for (Set<String> inset : intersectSets)
			{
				if (first)
				{
					set.addAll(inset);
					first = false;
				}
				else set.retainAll(inset);
			}
			for (Set<String> subset : subtractSets)
			{
				set.removeAll(subset);
			}

			System.out.println(name + "\t" + set.size());
		}

		Set<String> all = new HashSet<String>();
		for (Set<String> set : sets)
		{
			all.addAll(set);
		}
		System.out.println("Total: " + all.size());
	}

	@Test
	@Ignore
	public void checkOverlap() throws Throwable
	{
		Map<SIFType, Set<String>> map = readSIFFile("/home/ozgun/Desktop/PC.sif");

		List<SIFType> types = new ArrayList<SIFType>(Arrays.asList(SIFType.values()));
		types.retainAll(map.keySet());

		printOverlaps(map, types.subList(0, 8), false);
		printOverlaps(map, types.subList(8, 12), true);
	}

	private void printOverlaps(Map<SIFType, Set<String>> map, List<SIFType> types,
		boolean tryReversing)
	{
		for (SIFType type : types)
		{
			System.out.print("\t" + type.getTag());
		}
		for (SIFType type : types)
		{
			System.out.print("\n" + type.getTag());

			for (SIFType type2 : types)
			{
				Set<String> set = new HashSet<String>(map.get(type));
				set.retainAll(map.get(type2));
				int size = set.size();

				if (tryReversing)
				{
					set = new HashSet<String>(map.get(type));
					set.retainAll(negatePairs(map.get(type2)));
					if (set.size() > size) size = set.size();
				}

				if (!type.isDirected() && !type2.isDirected()) size /= 2;
				System.out.print("\t" + size);
			}
		}
		System.out.println("\n");
	}

	private Set<String> negatePairs(Set<String> set)
	{
		Set<String> neg = new HashSet<String>();
		for (String pair : set)
		{
			String[] tok = pair.split("\t");
			neg.add(tok[1] + "\t" + tok[0]);
		}
		return neg;
	}


	private Set<String> readPairsFromSIFFile(String file, boolean directed) throws FileNotFoundException
	{
		Scanner sc = new Scanner(new File(file));
		Set<String> set = new HashSet<String>();

		while(sc.hasNextLine())
		{
			String line = sc.nextLine();

			String[] token = line.split("\t");
			if (token.length >= 3)
			{
				set.add(token[0] + "\t" + token[2]);
				if (!directed) set.add(token[2] + "\t" + token[0]);
			}
		}
		return set;
	}

	private Map<SIFType, Set<String>> readSIFFile(String file) throws FileNotFoundException
	{
		Map<SIFType, Set<String>> map = new HashMap<SIFType, Set<String>>();
		Scanner sc = new Scanner(new File(file));

		while(sc.hasNextLine())
		{
			String line = sc.nextLine();

			String[] token = line.split("\t");
			if (token.length >= 3)
			{
				SIFType type = SIFType.typeOf(token[1]);
				if (type == null) continue;

				if (!map.containsKey(type)) map.put(type, new HashSet<String>());

				map.get(type).add(token[0] + "\t" + token[2]);
				if (!type.isDirected()) map.get(type).add(token[2] + "\t" + token[0]);
			}
		}
		return map;
	}

	private Set<String> readBlacklist() throws FileNotFoundException
	{
		Set<String> black = new HashSet<String>();
		Scanner sc = new Scanner(new File("blacklist.txt"));
		while(sc.hasNextLine())
		{
			black.add(sc.nextLine());
		}
		return black;
	}
}
