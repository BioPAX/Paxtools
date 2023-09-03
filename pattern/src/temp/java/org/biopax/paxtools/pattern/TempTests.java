package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.pattern.constraint.*;
import org.biopax.paxtools.pattern.miner.*;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.HGNC;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Ozgun Babur
 */
//@Ignore
public class TempTests
{
	Model model;

	private TempTests()
	{
//		SimpleIOHandler h = new SimpleIOHandler();
//		model = h.convertFromOWL(new FileInputStream("All-Data.owl"));
//		model = h.convertFromOWL(new FileInputStream("HumanCyc.owl"));
//		model = h.convertFromOWL(new FileInputStream("/home/ozgun/Downloads/Pathway Commons.7.NCI Pathway Interaction Database: Pathway.BIOPAX.owl"));
	}

	private void compareSIFFIles() throws IOException
	{
		Set<String> set7 = load("/home/ozgun/Temp/sif7.txt");
		Set<String> set8 = load("/home/ozgun/Temp/sif8.txt");

		set8.removeAll(set7);
		for (String s : set8)
		{
			System.out.println(s);
		}
	}

	private Set<String> load(String file) throws FileNotFoundException
	{
		Set<String> set = new HashSet<>();
		Scanner sc = new Scanner(new File(file));
		while (sc.hasNextLine())
		{
			String[] token = sc.nextLine().split("\t");
			set.add(token[0] + "\t" + token[1] + "\t" + token[2]);
		}
		return set;
	}

	private void generateSIF() throws IOException
	{
//		String base = "http://www.pathwaycommons.org/pc2/downloads/";
		String base = "http://pathwaycommons.baderlab.org/downloads/";
		SimpleIOHandler h = new SimpleIOHandler();
//		Model model = h.convertFromOWL(new GZIPInputStream(new URL(base + "Pathway%20Commons.8.pid.BIOPAX.owl.gz").openStream()));
		Model model = h.convertFromOWL(new GZIPInputStream(new URL(base + "Pathway%20Commons.8.Detailed.BIOPAX.owl.gz").openStream()));

//		SIFSearcher searcher = new SIFSearcher(new CommonIDFetcher(), SIFEnum.CONTROLS_STATE_CHANGE_OF);
//		searcher.setBlacklist(new Blacklist(new URL(base + "blacklist.txt").openStream()));

		BlacklistGenerator2 gen = new BlacklistGenerator2();
		gen.generateBlacklist(model);


//		searcher.searchSIF(model, new FileOutputStream("/home/ozgun/Temp/sif2.txt"), true);
	}

	private void checkSomethingOnModel()
	{
		for (PathwayStep step : model.getObjects(PathwayStep.class))
		{
			for (Process prc : step.getStepProcess())
			{
				if (prc instanceof Conversion)
				{
					System.out.println(prc.getUri());
					System.exit(0);
				}
			}
		}

//		for (SmallMolecule sm : model.getObjects(SmallMolecule.class))
//		{
//			for (Interaction inter : sm.getParticipantOf())
//			{
//				if (inter instanceof Conversion)
//				{
//					Conversion cnv = (Conversion) inter;
//
//					if (cnv.getLeft().contains(sm) && cnv.getRight().contains(sm))
//					{
//						System.out.println(sm.getDisplayName() + "\t" + cnv.getUri());
//					}
//				}
//			}
//		}
	}

	private void namesOfPathwaysThatContainReversibleReactions()
	{
		Pattern p = new Pattern(Pathway.class, "Pathway");
		p.add(new PathConstraint("Pathway/pathwayComponent:Conversion"), "Pathway", "Conv");
		p.add(new Field("Conversion/conversionDirection", Field.Operation.INTERSECT,
			ConversionDirectionType.REVERSIBLE), "Conv");
		p.add(new PathConstraint("Conversion/controlledOf:Catalysis"), "Conv", "Cat");
		p.add(new Empty(new PathConstraint("Catalysis/catalysisDirection")), "Cat");
		p.add(new Empty(new PathConstraint("Pathway/pathwayComponent:Pathway")), "Pathway");

		List<Pathway> pats = new ArrayList<>(
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
			System.out.println(pat.getUri());
		}
	}

	private void capturePattern() throws Throwable
	{
		Pattern p = PatternBox.controlsStateChangeThroughDegradation();

		Searcher.searchInFile(p, "All-Human-Data.owl", "Captured-controls-degradation.owl", 100, 1);
	}

	private void extractSignalink() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/signal-db/signalink-raw.txt"));

		Set<String> set = new HashSet<>();
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

	private void debugPattern() throws Throwable
	{

		Pattern p = PatternBox.controlsExpressionWithTemplateReac();

		Match m = new Match(p.size());
		ProteinReference er = (ProteinReference) model.getByID("identifiers.org/uniprot/P42229");
		PhysicalEntity pe = (PhysicalEntity) model.getByID("http://pid.nci.nih.gov/biopaxpid_10369");
		m.set(er, 0);
		m.set(pe, 1);
		List<Match> result = Searcher.search(m, p);
		System.out.println("result.size() = " + result.size());
	}


	private void searchAndWriteWithMiners() throws Throwable
	{
		Blacklist blacklist = new Blacklist("blacklist.txt");
		SIFMiner[] miner = new SIFMiner[]{
			new ControlsStateChangeOfMiner(),
			new CSCOButIsParticipantMiner(),
			new CSCOBothControllerAndParticipantMiner(),
			new CSCOThroughControllingSmallMoleculeMiner(),
			new CSCOThroughBindingSmallMoleculeMiner()};

		for (SIFMiner m : miner)
		{
			m.setBlacklist(blacklist);
		}

		for (int i = 0; i < miner.length; i++)
		{
			System.out.println("i = " + i);
			Map<BioPAXElement,List<Match>> matches =
				Searcher.search(model, miner[i].getPattern(), null);

			FileOutputStream os = new FileOutputStream(miner[i].getName() + ".txt");
			miner[i].writeResult(matches, os);
		}
	}

	private void printVennIntersections() throws FileNotFoundException
	{
//		SimpleIOHandler h = new SimpleIOHandler();
//		Model model = h.convertFromOWL(new FileInputStream("All-Data.owl"));
//
//		BlacklistGenerator gen = new BlacklistGenerator();
//		Blacklist black = gen.generateBlacklist(model);
//
//		SIFMiner[] miners = new SIFMiner[]{
//			new ControlsStateChangeOfMiner(),
//			new CSCOButIsParticipantMiner(),
//			new CSCOBothControllerAndParticipantMiner(),
//			new CSCOThroughControllingSmallMoleculeMiner(),
//			new CSCOThroughBindingSmallMoleculeMiner()};
//
//		for (SIFMiner m : miners) m.setBlacklist(black);

		String[] file = new String[]{"changes-state-of.txt", "cso-but-is-participant.txt",
			"cso-both-ctrl-part.txt", "cso-through-controlling-small-mol.txt",
			"cso-through-binding-small-mol.txt"};

//		assert miners.length == file.length;
//
//		for (int i = 0; i < file.length; i++)
//		{
//			SIFSearcher searcher = new SIFSearcher(miners[i]);
//			searcher.searchSIF(model, new FileOutputStream(file[i]), false);
//		}

		String[] let = new String[]{"A", "B", "C", "D", "E"};

		Set<String>[] sets = new Set[file.length];

		for (int i = 0; i < file.length; i++)
		{
			sets[i] = readPairsFromSIFFile(file[i], true);
		}

		for (int i = 1; i < Math.pow(2, file.length); i++)
		{
			Set<Set<String>> intersectSets = new HashSet<>();
			Set<Set<String>> subtractSets = new HashSet<>();
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
			Set<String> set = new HashSet<>();
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

		Set<String> all = new HashSet<>();
		for (Set<String> set : sets)
		{
			all.addAll(set);
		}
		System.out.println("Total: " + all.size());
	}

	private void checkOverlap() throws Throwable
	{
//		Map<SIFType, Set<String>> map = readSIFFile("/home/ozgun/Projects/chibe/portal-cache/PC.sif");
		Map<SIFType, Set<String>> map = readSIFFile("/home/ozgun/PC.sif");

		List<SIFType> types = new ArrayList<>(Arrays.asList(SIFEnum.values()));
		types.retainAll(map.keySet());

		printOverlaps(map, types.subList(0, 8), false);
		printOverlaps(map, types.subList(8, 12), true);
		printOverlaps(map, types.subList(12, 14), true);
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
				Set<String> set = new HashSet<>(map.get(type));
				set.retainAll(map.get(type2));
				int size = set.size();

				if (tryReversing)
				{
					set = new HashSet<>(map.get(type));
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
		Set<String> neg = new HashSet<>();
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
		Set<String> set = new HashSet<>();

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
				SIFType type = SIFEnum.typeOf(token[1]);
				if (type == null) continue;

				if (!map.containsKey(type)) map.put(type, new HashSet<>());

				map.get(type).add(token[0] + "\t" + token[2]);
				if (!type.isDirected()) map.get(type).add(token[2] + "\t" + token[0]);
			}
		}
		return map;
	}

	private Map<SIFType, Set<String>> readSIFFile2(String file) throws FileNotFoundException
	{
		Map<SIFType, Set<String>> map = new HashMap<SIFType, Set<String>>();
		Scanner sc = new Scanner(new File(file));

		while(sc.hasNextLine())
		{
			String line = sc.nextLine();

			String[] token = line.split("\t");
			if (token.length >= 3)
			{
				SIFType type = SIFEnum.typeOf(token[1]);
				if (type == null) continue;

				if (!map.containsKey(type)) map.put(type, new HashSet<>());

				map.get(type).add(token[0] + "\t" + type.getTag() +  "\t" + token[2]);
			}
		}
		return map;
	}

	public void separateInteractions() throws IOException
	{
		Map<SIFType, Set<String>> map = readSIFFile2("/home/ozgun/Projects/chibe/portal-cache/PC.sif");

		String outDir = "/home/ozgun/Desktop/BinIntStats/";
		String dirDir = outDir + "directed/";
		String undirDir = outDir + "undirected/";

		new File(dirDir + "/stats/").mkdirs();
		new File(undirDir + "/stats/").mkdirs();

		for (SIFType type : map.keySet())
		{
			String file = (type.isDirected() ? dirDir : undirDir) + type.getTag() + ".sif";

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			for (String s : map.get(type))
			{
				writer.write(s + "\n");
			}

			writer.close();
		}

	}

	private void tempCode() throws FileNotFoundException
	{
		String dir = System.getProperty("java.io.tmpdir") + File.separator;
		System.out.println("dir = " + dir);
	}

	private void compareModels(Model m1, Model m2)
	{
		for (BioPAXElement e1 : m1.getObjects())
		{
			BioPAXElement e2 = m2.getByID(e1.getUri());

			if (!e1.equals(e2)) System.out.println("Not equal!!");
		}
		System.out.println("End");
	}

	private static void compareSIFs() throws FileNotFoundException
	{
		Set<String> set1 = new HashSet<>();
		Scanner sc = new Scanner(new File("/home/ozgun/Projects/causality/PC/controls-expression-of.txt"));
		while (sc.hasNextLine())
		{
			String[] tok = sc.nextLine().split("\t");
			set1.add(tok[0] + " " + tok[1]);
		}

		Set<String> set2 = new HashSet<>();
		sc = new Scanner(new File("/home/ozgun/Projects/chibe/PC/controls-expression-of.txt"));
		while (sc.hasNextLine())
		{
			String[] tok = sc.nextLine().split("\t");
			set2.add(tok[0] + " " + tok[1]);
		}

		set1.removeAll(set2);
		for (String s : set1)
		{
			System.out.println(s);
		}
	}

	private void generateBlacklist() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		model = h.convertFromOWL(new FileInputStream("/home/ozgun/Downloads/Pathway Commons.7.NCI Pathway Interaction Database: Pathway.BIOPAX.owl"));
		BlacklistGenerator3 bg = new BlacklistGenerator3(new FileInputStream("blacklist-names.txt"));
		Blacklist blacklist = bg.generateBlacklist(model);
		blacklist.write("blacklist-temp.txt");
	}

	public static void main(String[] args) throws FileNotFoundException
	{
//		compareSIFs();
		TempTests tt = new TempTests();
		tt.generateBlacklist();
	}

}
