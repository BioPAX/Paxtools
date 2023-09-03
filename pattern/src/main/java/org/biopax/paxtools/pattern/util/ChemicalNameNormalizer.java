package org.biopax.paxtools.pattern.util;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;

import java.util.*;

/**
 * This class is used for finding a standard name for a small molecule. During detection of
 * ubiquitous small molecules, we map the duplicated small molecules to one standard name, otherwise
 * their degree would be divided and this would spoil the detection method.
 *
 * @author Ozgun Babur
 */
public class ChemicalNameNormalizer
{
	/**
	 * Mapping from a small molecule to the one that contains the standard name.
	 */
	Map<SmallMoleculeReference, SmallMoleculeReference> map;

	/**
	 * Gets the standard name of the small molecule.
	 * @param smr the molecule to check standard name
	 * @return standard name
	 */
	public String getName(SmallMoleculeReference smr)
	{
		if (map.containsKey(smr)) return map.get(smr).getDisplayName();
		else return smr.getDisplayName();
	}

	/**
	 * Constructor that also infers all the mapping.
	 * @param model the big picture
	 */
	public ChemicalNameNormalizer(Model model)
	{
		map = new HashMap<>();
		Set<SmallMoleculeReference> standard = new HashSet<>();
		Set<SmallMoleculeReference> other = new HashSet<>();

		for (SmallMoleculeReference smr : model.getObjects(SmallMoleculeReference.class))
		{
			if (smr.getUri().startsWith("http://identifiers")) standard.add(smr);
			else other.add(smr);
		}

		System.out.println("Standard smr = " + standard.size());
		System.out.println("Other smr = " + other.size());

		Map<SmallMoleculeReference, Set<String>> smrNames = collectNames(false, standard, other);
		Map<SmallMoleculeReference, Set<String>> smNames = collectNames(true, standard, other);

		// Unify names of standards

		Map<SmallMoleculeReference, Set<SmallMoleculeReference>> standardSelfMatch =
			getSelfMatching(standard, smrNames, smNames, true);

		for (SmallMoleculeReference smr : standardSelfMatch.keySet())
		{
			Set<SmallMoleculeReference> matches = standardSelfMatch.get(smr);
			if (matches.size() == 1)
			{
				SmallMoleculeReference m = matches.iterator().next();

				if (smr.getDisplayName().length() <= m.getDisplayName().length())
				{
					map.put(smr, m);
					standard.remove(smr);
				}
			}
			else
			{
				System.out.print(smr.getDisplayName() + " matched more than one");
				for (SmallMoleculeReference match : matches)
				{
					System.out.print("\t" + match.getDisplayName());
				}
				System.out.println();
			}
		}

		Map<SmallMoleculeReference, Set<SmallMoleculeReference>> selfMatch =
			getSelfMatching(other, smrNames, smNames, false);

		enrichNamesWithMatchings(selfMatch, smrNames);
		enrichNamesWithMatchings(selfMatch, smNames);

		Set<SmallMoleculeReference> missed = new HashSet<>();
		Map<SmallMoleculeReference, Set<SmallMoleculeReference>> multiMap = new HashMap<>();

		for (SmallMoleculeReference smr : other)
		{
			Set<SmallMoleculeReference> matching = getMatching(smr, standard, smrNames, smNames);

			if (matching.size() == 1)
			{
				map.put(smr, matching.iterator().next());
			}
			else if (matching.size() > 1)
			{
				multiMap.put(smr, matching);
			}
			else
			{
				missed.add(smr);
			}
		}

		for (SmallMoleculeReference smr : multiMap.keySet())
		{
			if (isGeneric(smr)) continue;

			Set<SmallMoleculeReference> matches = multiMap.get(smr);
			SmallMoleculeReference rep = selectRepresentative(matches, map);
			map.put(smr, rep);

			for (SmallMoleculeReference match : matches)
			{
				if (match == rep) continue;
				if (map.containsKey(match))
				{
					if (map.get(match) == rep)
						continue;
//					System.out.println("Already matched " + match.getDisplayName() + " to " +
//						map.get(match).getDisplayName() + ". This one is " + rep.getDisplayName());
				}
				else if (map.values().contains(match))
				{
//					System.out.println(match.getDisplayName() + " was mapped from another chem");
				}
				else map.put(match, rep);
			}
		}

		Iterator<SmallMoleculeReference> iter = multiMap.keySet().iterator();
		while (iter.hasNext())
		{
			SmallMoleculeReference smr = iter.next();
			if (map.containsKey(smr)) iter.remove();
		}

		System.out.println("matchCnt = " + map.size());
		System.out.println("multiCnt = " + multiMap.size());
		System.out.println("missCnt = " + missed.size());
		System.out.println();
	}

	private Map<SmallMoleculeReference, Set<String>> collectNames(boolean peLevel,
		Set<SmallMoleculeReference>... sets)
	{
		Map<SmallMoleculeReference, Set<String>> map = new HashMap<>();

		for (Set<SmallMoleculeReference> set : sets)
		{
			for (SmallMoleculeReference smr : set)
			{
				map.put(smr, new HashSet<>());

				if (!peLevel)
				{
					for (String name : smr.getName())
					{
						map.get(smr).add(name.toLowerCase());
					}
				}
				else
				{
					for (SimplePhysicalEntity sm : smr.getEntityReferenceOf())
					{
						for (String name : sm.getName())
						{
							map.get(smr).add(name.toLowerCase());
						}
					}
				}
			}
		}
		return map;
	}

	private Set<SmallMoleculeReference> getNameNormalizedMatching(SmallMoleculeReference smr,
		Set<SmallMoleculeReference> smrs)
	{
		String name = null;
		String dispName = smr.getDisplayName().toLowerCase();
		if (dispName.endsWith("-)") || dispName.endsWith("+)"))
		{
			name = dispName.substring(0, dispName.lastIndexOf("(")).trim();
		}
		else if (dispName.endsWith(" zwitterion"))
		{
			name = dispName.substring(0, dispName.lastIndexOf(" ")).trim();
		}

		if (name == null) return Collections.emptySet();

		Set<SmallMoleculeReference> matching = new HashSet<>();

		for (SmallMoleculeReference ref : smrs)
		{
			if (ref.getDisplayName().toLowerCase().equals(name)) matching.add(ref);
		}

		return matching;
	}

	private Set<SmallMoleculeReference> getMatching(SmallMoleculeReference smr,
		Set<SmallMoleculeReference> standard, Map<SmallMoleculeReference, Set<String>> smrNames,
		Map<SmallMoleculeReference, Set<String>> smNames)
	{
		Set<SmallMoleculeReference> matching = new HashSet<>();

		for (SmallMoleculeReference std : standard)
		{
			if (std.getDisplayName() != null && smr.getDisplayName() != null &&
				std.getDisplayName().equalsIgnoreCase(smr.getDisplayName()))
				matching.add(std);
		}

		if (!matching.isEmpty()) return matching;

		for (SmallMoleculeReference std : standard)
		{
			for (String name : smrNames.get(smr))
			{
				if(smrNames.get(std).contains(name)) matching.add(std);
			}
		}

		if (!matching.isEmpty()) return matching;

		for (SmallMoleculeReference std : standard)
		{
			for (String name : smrNames.get(smr))
			{
				if(smNames.get(std).contains(name)) matching.add(std);
			}
		}

		if (!matching.isEmpty()) return matching;

		for (SmallMoleculeReference std : standard)
		{
			for (String name : smNames.get(smr))
			{
				if(smrNames.get(std).contains(name)) matching.add(std);
			}
		}

		if (!matching.isEmpty()) return matching;

		for (SmallMoleculeReference std : standard)
		{
			for (String name : smNames.get(smr))
			{
				if(smNames.get(std).contains(name)) matching.add(std);
			}
		}

		return matching;
	}

	private Map<SmallMoleculeReference, Set<SmallMoleculeReference>> getSelfMatching(
		Set<SmallMoleculeReference> smrs, Map<SmallMoleculeReference, Set<String>> smrNames,
		Map<SmallMoleculeReference, Set<String>> smNames, boolean normalizeName)
	{
		Map<SmallMoleculeReference, Set<SmallMoleculeReference>> map = new HashMap<>();

		for (SmallMoleculeReference smr : smrs)
		{
			Set<SmallMoleculeReference> matching = normalizeName ?
				getNameNormalizedMatching(smr, smrs) :
				getMatching(smr, smrs, smrNames, smNames);

			// it should at least detect itself, if it has a name
			assert normalizeName || !matching.isEmpty() || smr.getName().isEmpty();

			matching.remove(smr);
			if (!matching.isEmpty()) map.put(smr, matching);
		}

		return map;
	}

	private static final PathAccessor INTER_ACC =
		new PathAccessor("SmallMoleculeReference/entityReferenceOf/participantOf");

	private Map<SmallMoleculeReference, Integer> getInteractionCounts(
		Set<SmallMoleculeReference>... smrSets)
	{
		Map<SmallMoleculeReference, Integer> cnt = new HashMap<>();

		for (Set<SmallMoleculeReference> smrSet : smrSets)
		{
			for (SmallMoleculeReference smr : smrSet)
			{
				if (cnt.containsKey(smr)) continue;

				cnt.put(smr, INTER_ACC.getValueFromBean(smr).size());
			}
		}
		return cnt;
	}

	private void enrichNamesWithMatchings(
		Map<SmallMoleculeReference, Set<SmallMoleculeReference>> matchMap,
		Map<SmallMoleculeReference, Set<String>> names)
	{
		for (SmallMoleculeReference smr : matchMap.keySet())
		{
			for (SmallMoleculeReference match : matchMap.get(smr))
			{
				names.get(smr).addAll(names.get(match));
			}
		}
	}

	private boolean isGeneric(SmallMoleculeReference smr)
	{
		if (!smr.getMemberEntityReference().isEmpty()) return true;

		for (SimplePhysicalEntity sm : smr.getEntityReferenceOf())
		{
			if (!sm.getMemberPhysicalEntity().isEmpty()) return true;
		}

		return false;
	}

	private SmallMoleculeReference selectRepresentative(Set<SmallMoleculeReference> smrs,
		final Map<SmallMoleculeReference, SmallMoleculeReference> map)
	{
		List<SmallMoleculeReference> list = new ArrayList<>(smrs);
		final Map<SmallMoleculeReference, Integer> cnt = getInteractionCounts(smrs);

		Collections.sort(list, (o1, o2) -> {
			if (map.containsValue(o1))
			{
				if (!map.containsValue(o2)) return -1;
			}
			else
			{
				if (map.containsValue(o2)) return 1;
			}

			if (!cnt.get(o1).equals(cnt.get(o2))) return cnt.get(o2).compareTo(cnt.get(o1));

			if (o1.getDisplayName().endsWith(")"))
			{
				if (!o2.getDisplayName().endsWith(")")) return -1;
			}
			else if (o2.getDisplayName().endsWith(")")) return 1;

			return o1.getDisplayName().compareTo(o2.getDisplayName());
		});

		return list.get(0);
	}
}
