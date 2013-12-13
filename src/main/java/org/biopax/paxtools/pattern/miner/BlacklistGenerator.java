package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.ChemicalNameNormalizer;
import org.biopax.paxtools.pattern.util.RelType;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class generates a blacklist for the given model. It is important that the given model is the
 * very big integrated corpus. It won't work on tiny little model.
 *
 * @author Ozgun Babur
 */
public class BlacklistGenerator
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
	public BlacklistGenerator(Decider decider)
	{
		this.decider = decider;
	}

	/**
	 * Default constructor.
	 */
	public BlacklistGenerator()
	{
		this(new Decider()
		{
			@Override
			public boolean isUbique(int neighborSize, int upstrOnly, int dwstrOnly)
			{
				return neighborSize >= 30;
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
	 */
	public Blacklist generateBlacklist(Model model)
	{
		ChemicalNameNormalizer normalizer = new ChemicalNameNormalizer(model);
		SIFSearcher searcher = new SIFSearcher(new Fetcher(normalizer), SIFType.USED_TO_PRODUCE);

		Set<SIFInteraction> sifs = searcher.searchSIF(model);

		// read interactions into maps

		Map<String, Set<String>> upstrMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> dwstrMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> neighMap = new HashMap<String, Set<String>>();

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


		Blacklist blacklist = new Blacklist();

		// populate the blacklist

		for (SmallMoleculeReference smr : model.getObjects(SmallMoleculeReference.class))
		{
			String name = normalizer.getName(smr);

			int neighSize = neighMap.containsKey(name) ? neighMap.get(name).size() : 0;
			int upstrOnly = upstrMap.containsKey(name) ? upstrMap.get(name).size() : 0;
			int dwstrOnly = dwstrMap.containsKey(name) ? dwstrMap.get(name).size() : 0;

			if (decider.isUbique(neighSize, upstrOnly, dwstrOnly))
			{
				blacklist.addEntry(smr.getRDFId(),
					decider.getScore(neighSize, upstrOnly, dwstrOnly),
					decider.getContext(neighSize, upstrOnly, dwstrOnly));
			}
		}

		blacklist.write("blacklist.txt");

		return blacklist;
	}

	/**
	 * Class to fetch the ID of the small molecule.
	 */
	class Fetcher implements IDFetcher
	{
		ChemicalNameNormalizer normalizer;

		Fetcher(ChemicalNameNormalizer normalizer)
		{
			this.normalizer = normalizer;
		}

		@Override
		public String fetchID(BioPAXElement ele)
		{
			if (ele instanceof SmallMoleculeReference)
			{
				return normalizer.getName((SmallMoleculeReference) ele);
			}

			return null;
		}
	}

	/**
	 * The class to decide if a molecule is ubique, its score and its context of ubiquity.
	 */
	static interface Decider
	{
		public boolean isUbique(int neighborSize, int upstrOnly, int dwstrOnly);
		public int getScore(int neighborSize, int upstrOnly, int dwstrOnly);
		public RelType getContext(int neighborSize, int upstrOnly, int dwstrOnly);
	}

}
