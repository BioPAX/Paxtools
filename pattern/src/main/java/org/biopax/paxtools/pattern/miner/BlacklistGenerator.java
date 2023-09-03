package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.ChemicalNameNormalizer;
import org.biopax.paxtools.pattern.util.RelType;

import java.util.*;

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
		SIFSearcher searcher = new SIFSearcher(new Fetcher(normalizer), SIFEnum.USED_TO_PRODUCE);

		Set<SIFInteraction> sifs = searcher.searchSIF(model);

		// read interactions into maps

		Map<String, Set<String>> upstrMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> dwstrMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> neighMap = new HashMap<String, Set<String>>();

		for (SIFInteraction sif : sifs)
		{
			String source = sif.sourceID;
			String target = sif.targetID;

			if (!neighMap.containsKey(source)) neighMap.put(source, new HashSet<>());
			if (!neighMap.containsKey(target)) neighMap.put(target, new HashSet<>());
			if (!dwstrMap.containsKey(source)) dwstrMap.put(source, new HashSet<>());
			if (!dwstrMap.containsKey(target)) dwstrMap.put(target, new HashSet<>());
			if (!upstrMap.containsKey(source)) upstrMap.put(source, new HashSet<>());
			if (!upstrMap.containsKey(target)) upstrMap.put(target, new HashSet<>());

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

			Set<String> temp = new HashSet<>(upstr);
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

//			if (neighSize > 30) System.out.println(name + "\t" + neighSize + "\t" + upstrOnly + "\t" + dwstrOnly);

			if (decider.isUbique(neighSize, upstrOnly, dwstrOnly))
			{
				blacklist.addEntry(smr.getUri(),
					decider.getScore(neighSize, upstrOnly, dwstrOnly),
					decider.getContext(neighSize, upstrOnly, dwstrOnly));
			}
		}

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
		public Set<String> fetchID(BioPAXElement ele)
		{
			if (ele instanceof SmallMoleculeReference)
			{
				return Collections.singleton(normalizer.getName((SmallMoleculeReference) ele));
			}

			return null;
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
