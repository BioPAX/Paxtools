package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.ChemicalNameNormalizer;
import org.biopax.paxtools.pattern.util.RelType;

import java.io.*;
import java.util.*;

/**
 * This class generates a blacklist for the given model. It is important that the given model is the
 * very big integrated corpus. It won't work on tiny little model.
 *
 * @author Ozgun Babur
 */
public class BlacklistGenerator2
{
	/**
	 * For deciding if the molecule is ubiquitous and for determining the score and context.
	 */
	private Decider decider;

	/**
	 * Constructor with decider. This decider should be optimized for the specific resource that the
	 * user deals with.
	 * @param decider decides if the molecule is ubique
	 */
	public BlacklistGenerator2(Decider decider)
	{
		this.decider = decider;
	}

	final String MAPPING_FILE = "chem-name-mapping.txt";
	final String WHITELIST_FILE = "whitelist.txt";

	/**
	 * Default constructor.
	 */
	public BlacklistGenerator2()
	{
		this(new Decider()
		{
			@Override
			public boolean isUbique(int neighborSize, int upstrOnly, int dwstrOnly)
			{
				return neighborSize >= 50;
			}

			@Override
			public int getScore(int neighborSize, int upstrOnly, int dwstrOnly)
			{
				return neighborSize;
			}

			@Override
			public RelType getContext(int neighborSize, int upstrOnly, int dwstrOnly)
			{
				if (upstrOnly > 10 * dwstrOnly) return RelType.OUTPUT;
				else if (dwstrOnly > 10 * upstrOnly) return RelType.INPUT;
				else return null;
			}
		});
	}

	/**
	 * Generates the blacklist.
	 * @param model model to use
	 * @return the blacklist
	 * @throws IOException when there is an I/O problem
	 */
	public Blacklist generateBlacklist(Model model) throws IOException
	{
		Map<String, String> nameMapping = readNameMapping();
		if (nameMapping == null)
		{
			generateNameMappingFileToCurate(model);
			throw new RuntimeException("Small molecule name mapping file not found. Generated a " +
				"mapping file, but it needs manual curation before use.\nPlease go over some top " +
				"portion of this file and delete invalid lines and any uncurated bottom part.\n" +
				"After that, you can rerun this method.");
		}

		SIFSearcher searcher = new SIFSearcher(new Fetcher(nameMapping), SIFEnum.USED_TO_PRODUCE);

		Set<SIFInteraction> sifs = searcher.searchSIF(model);

		// read interactions into maps

		Map<String, Set<String>> upstrMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> dwstrMap = new HashMap<String, Set<String>>();
		final Map<String, Set<String>> neighMap = new HashMap<String, Set<String>>();

		for (SIFInteraction sif : sifs)
		{
			String source = sif.sourceID;
			String target = sif.targetID;

			if (!neighMap.containsKey(source)) neighMap.put(source, new HashSet<String>());
			if (!neighMap.containsKey(target)) neighMap.put(target, new HashSet<String>());
			if (!dwstrMap.containsKey(source)) dwstrMap.put(source, new HashSet<String>());
			if (!dwstrMap.containsKey(target)) dwstrMap.put(target, new HashSet<String>());
			if (!upstrMap.containsKey(source)) upstrMap.put(source, new HashSet<String>());
			if (!upstrMap.containsKey(target)) upstrMap.put(target, new HashSet<String>());

			neighMap.get(source).add(target);
			neighMap.get(target).add(source);
			dwstrMap.get(source).add(target);
			upstrMap.get(target).add(source);
		}

		// remove intersection of upstream and downstream

		for (String name : neighMap.keySet())
		{
			if (!upstrMap.containsKey(name) || !dwstrMap.containsKey(name)) continue;

			Set<String> upstr = upstrMap.get(name);
			Set<String> dwstr = dwstrMap.get(name);

			Set<String> temp = new HashSet<String>(upstr);
			upstr.removeAll(dwstr);
			dwstr.removeAll(temp);
		}

//		writeTheGuideRankingToTuneTheDecider(model, nameMapping, upstrMap, dwstrMap, neighMap);
//		if (true) return null;

		Set<String> white = readWhitelist();

		Blacklist blacklist = new Blacklist();

		// populate the blacklist

		Fetcher nameFetcher = new Fetcher(nameMapping);
		for (SmallMoleculeReference smr : model.getObjects(SmallMoleculeReference.class))
		{
			Set<String> names = nameFetcher.fetchID(smr);
			if (names.isEmpty()) continue;
			String name = names.iterator().next();

			if (white != null && white.contains(name)) continue;

			int neighSize = neighMap.containsKey(name) ? neighMap.get(name).size() : 0;
			int upstrOnly = upstrMap.containsKey(name) ? upstrMap.get(name).size() : 0;
			int dwstrOnly = dwstrMap.containsKey(name) ? dwstrMap.get(name).size() : 0;

			if (decider.isUbique(neighSize, upstrOnly, dwstrOnly))
			{
				blacklist.addEntry(smr.getUri(),
					decider.getScore(neighSize, upstrOnly, dwstrOnly),
					decider.getContext(neighSize, upstrOnly, dwstrOnly));
			}
		}

		return blacklist;
	}

	private void writeTheGuideRankingToTuneTheDecider(Model model, Map<String, String> nameMapping, Map<String, Set<String>> upstrMap, Map<String, Set<String>> dwstrMap, final Map<String, Set<String>> neighMap) throws IOException
	{
		// Sort to degree

		List<String> names = new ArrayList<String>(neighMap.keySet());
		Collections.sort(names, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				return new Integer(neighMap.get(o2).size()).compareTo(neighMap.get(o1).size());
			}
		});

		SIFSearcher searcher2 = new SIFSearcher(new Fetcher(nameMapping), new ChemicalAffectsThroughControlMiner());

		Set<SIFInteraction> sifs2 = searcher2.searchSIF(model);

		// read interactions into maps

		final Map<String, Set<String>> affectMap = new HashMap<String, Set<String>>();

		for (SIFInteraction sif : sifs2)
		{
			String source = sif.sourceID;
			String target = sif.targetID;

			if (!affectMap.containsKey(source)) affectMap.put(source, new HashSet<String>());
			affectMap.get(source).add(target);
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter("ubique-stats.txt"));

		for (String name : names)
		{
			writer.write(name + "\t" + neighMap.get(name).size() + "\t" +
				upstrMap.get(name).size() + "\t" + dwstrMap.get(name).size() + "\t|\t" +
				(affectMap.containsKey(name) ? affectMap.get(name).size() : 0) +  "\n");
		}

		writer.close();
	}

	private void generateNameMappingFileToCurate(Model model) throws IOException
	{
		SIFSearcher searcher = new SIFSearcher(new Fetcher(null), SIFEnum.USED_TO_PRODUCE);

		Set<SIFInteraction> sifs = searcher.searchSIF(model);

		// read interactions into maps

		final Map<String, Set<String>> neighMap = new HashMap<String, Set<String>>();

		for (SIFInteraction sif : sifs)
		{
			String source = sif.sourceID;
			String target = sif.targetID;

			if (!neighMap.containsKey(source)) neighMap.put(source, new HashSet<String>());
			if (!neighMap.containsKey(target)) neighMap.put(target, new HashSet<String>());

			neighMap.get(source).add(target);
			neighMap.get(target).add(source);
		}

		// Sort to degree

		List<String> names = new ArrayList<String>(neighMap.keySet());
		Collections.sort(names, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				return new Integer(neighMap.get(o2).size()).compareTo(neighMap.get(o1).size());
			}
		});

		Set<Set<String>> bag = collectNameSets(model);
		Map<String, Set<Set<String>>> nameToSets = getNameToSetsMap(bag);
		Map<Set<String>, Set<Set<String>>> intersectionMap = getIntersectionMap(bag);

		// Find and record name mapping

		String dir = System.getProperty("java.io.tmpdir") + File.separator;

		BufferedWriter writer = new BufferedWriter(new FileWriter(dir + MAPPING_FILE));

		List<String> remaining = new ArrayList<String>();
		for (String name : names)
		{
			remaining.add(name.toLowerCase());
		}

		for (String name : names)
		{
			name = name.toLowerCase();
			remaining.remove(name);

			Map<String, Set<String>[]> mappings = getMappingsAndBasis(
				name, remaining, nameToSets, intersectionMap);

			for (String mapped : mappings.keySet())
			{
				Set<String>[] s = mappings.get(mapped);
				writer.write(name + "\t" + mapped + "\t" + s[0] + "\t" + s[1] + "\t" + s[2] + "\n");
			}
		}

		writer.close();
		System.out.println("A small molecule name mapping file is generated (" + dir +
			MAPPING_FILE + "). Please curate the top portion of this file, delete invalid lines, " +
			"copy the curated file into the working directory, and re-run this code.");
	}

	private Set<Set<String>> collectNameSets(Model model)
	{
		Set<Set<String>> bag = new HashSet<Set<String>>();
		for (SmallMoleculeReference smr : model.getObjects(SmallMoleculeReference.class))
		{
			Set<String> set = new HashSet<String>();

			for (String name : smr.getName())
			{
				set.add(name.toLowerCase());
			}

			for (SimplePhysicalEntity sm : smr.getEntityReferenceOf())
			{
				for (String name : sm.getName())
				{
					set.add(name.toLowerCase());
				}
			}

			doSomeCleaning(set);
			enrichWithModifications(set);
			bag.add(set);
		}
		return bag;
	}

	private void doSomeCleaning(Set<String> set)
	{
		Set<String> rem = new HashSet<String>();
		for (String s : set)
		{
			if (s.contains("pathway") || s.contains("participant")) rem.add(s);
		}
		set.removeAll(rem);
	}

	private void enrichWithModifications(Set<String> set)
	{
		for (String s : new HashSet<String>(set))
		{
			if (s.endsWith("-)") || s.endsWith("+)"))
			{
				set.add(s.substring(0, s.lastIndexOf("(")).trim());
			}
			else if (s.endsWith(" zwitterion"))
			{
				set.add(s.substring(0, s.lastIndexOf(" ")).trim());
			}
		}
	}

	private Map<String, Set<Set<String>>> getNameToSetsMap(Set<Set<String>> bag)
	{
		Map<String, Set<Set<String>>> map = new HashMap<String, Set<Set<String>>>();

		for (Set<String> set : bag)
		{
			for (String name : set)
			{
				if (!map.containsKey(name)) map.put(name, new HashSet<Set<String>>());
				map.get(name).add(set);
			}
		}
		return map;
	}

	private Set<String> getCommon(Set<String> set1, Set<String> set2)
	{
		Set<String> comm = new HashSet<String>(set1);
		comm.retainAll(set2);
		return comm;
	}

	private List<String> getCommon(List<String> list1, Set<String> set2)
	{
		List<String> comm = new ArrayList<String>();
		for (String s : list1)
		{
			if (set2.contains(s)) comm.add(s);
		}
		return comm;
	}

	private Map<Set<String>, Set<Set<String>>> getIntersectionMap(Set<Set<String>> bag)
	{
		Map<Set<String>, Set<Set<String>>> map = new HashMap<Set<String>, Set<Set<String>>>();

		for (Set<String> set : bag)
		{
			for (Set<String> oth : bag)
			{
				if (set == oth) continue;

				if (!getCommon(set, oth).isEmpty())
				{
					if (!map.containsKey(set)) map.put(set, new HashSet<Set<String>>());
					map.get(set).add(oth);
				}
			}
		}
		return map;
	}

	private Map<String, Set<String>[]> getMappingsAndBasis(String name, List<String> consider,
		Map<String, Set<Set<String>>> nameToSets, Map<Set<String>, Set<Set<String>>> intersectionMap)
	{
		Map<String, Set<String>[]> map = new HashMap<String, Set<String>[]>();

		for (Set<String> set : nameToSets.get(name))
		{
			if (intersectionMap.containsKey(set))
			{
				for (Set<String> other : intersectionMap.get(set))
				{
					List<String> common = getCommon(consider, other);

					for (String target : common)
					{
						map.put(target, new Set[]{getCommon(set, other), set, other});
					}
				}
			}
		}
		return map;
	}

	private Map<String, String> readNameMapping() throws FileNotFoundException
	{
		if (!new File(MAPPING_FILE).exists()) return null;

		Map<String, String> map = new HashMap<String, String>();

		Scanner sc = new Scanner(new File(MAPPING_FILE));
		while (sc.hasNextLine())
		{
			String[] token = sc.nextLine().split("\t");

			map.put(token[1], token[0]);
		}
		return map;
	}

	private Set<String> readWhitelist() throws FileNotFoundException
	{
		if (!new File(WHITELIST_FILE).exists())
		{
			System.out.println("No whitelist file found (" + WHITELIST_FILE + "). " +
				"Not whitelisting anything.");
			return null;
		}
		Set<String> names = new HashSet<String>();
		Scanner sc = new Scanner(new File(WHITELIST_FILE));
		while (sc.hasNextLine())
		{
			String name = sc.nextLine().split("\t")[0];
			names.add(name);
		}
		return names;
	}

	/**
	 * Class to fetch the ID of the small molecule.
	 */
	class Fetcher extends CommonIDFetcher
	{
		Map<String, String> nameMap;

		public Fetcher(Map<String, String> nameMap)
		{
			this.nameMap = nameMap;
		}

		@Override
		public Set<String> fetchID(BioPAXElement ele)
		{
			if (ele instanceof SmallMoleculeReference)
			{
				SmallMoleculeReference smr = (SmallMoleculeReference) ele;
				String name = null;
				if (smr.getDisplayName() != null) name = smr.getDisplayName();
				else if (smr.getStandardName() != null) name = smr.getStandardName();
				else if (!smr.getName().isEmpty()) name = smr.getName().iterator().next();

				if (name != null)
				{
					name = name.toLowerCase();
					if (nameMap != null && nameMap.containsKey(name))
						name = nameMap.get(name);
					return Collections.singleton(name);
				}
			}

			return super.fetchID(ele);
		}
	}

	/**
	 * The class to decide if a molecule is ubique, its score and its context of ubiquity.
	 */
	static interface Decider
	{
		/**
		 * Tells if the molecule is ubique in at least one context.
		 * @param neighborSize number of neighbors in the used-to-produce network
		 * @param upstrOnly number of upstream neighbors in the used-to-produce network, that are not also at downstream
		 * @param dwstrOnly number of downstream neighbors in the used-to-produce network, that are not also at upstream
		 */
		public boolean isUbique(int neighborSize, int upstrOnly, int dwstrOnly);

		/**
		 * Gets the ubiquity score of the ubique molecule. This score is used for comparing ubiques
		 * and deciding the most essential reactants of a reaction if all reactants are ubique.
		 * @param neighborSize number of neighbors in the used-to-produce network
		 * @param upstrOnly number of upstream neighbors in the used-to-produce network, that are not also at downstream
		 * @param dwstrOnly number of downstream neighbors in the used-to-produce network, that are not also at upstream
		 */
		public int getScore(int neighborSize, int upstrOnly, int dwstrOnly);

		/**
		 * Gets the context of ubiquity. A molecule can be ubiquitously consumed, or can be
		 * ubiquitously produced, or both. When it is both, this method has to return null.
		 * @param neighborSize number of neighbors in the used-to-produce network
		 * @param upstrOnly number of upstream neighbors in the used-to-produce network, that are not also at downstream
		 * @param dwstrOnly number of downstream neighbors in the used-to-produce network, that are not also at upstream
		 */
		public RelType getContext(int neighborSize, int upstrOnly, int dwstrOnly);
	}



}
