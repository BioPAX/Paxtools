package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.util.DifferentialModificationUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * This class checks if there exists a desired type of modification change among two PhysicalEntity.
 *
 * var0: First simple PhysicalEntity
 * Var1: Second simple PhysicalEntity
 *
 * @author Ozgun Babur
 */
public class ModificationChangeConstraint extends ConstraintAdapter
{
	/**
	 * Partial names of the features to be considered.
	 */
	protected String[] featureSubstring;

	/**
	 * Gain or loss?
	 */
	protected Type type;

	/**
	 * Constructor with the desired change and maps to activating and inactivating features. If the
	 * feature substrings are not provided, any feature is qualified.
	 *
	 * @param type either gain, loss, or any
	 * @param featureSubstring partial names of the features desired to be changed
	 */
	public ModificationChangeConstraint(Type type, String... featureSubstring)
	{
		super(2);

		this.type = type;

		for (int i = 0; i < featureSubstring.length; i++)
		{
			featureSubstring[i] = featureSubstring[i].toLowerCase();
		}

		this.featureSubstring = featureSubstring;
	}

	/**
	 * Checks the any of the changed modifications match to any of the desired modifications.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if a modification change is among desired modifications
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		PhysicalEntity pe1 = (PhysicalEntity) match.get(ind[0]);
		PhysicalEntity pe2 = (PhysicalEntity) match.get(ind[1]);

		Set<ModificationFeature>[] mods =
			DifferentialModificationUtil.getChangedModifications(pe1, pe2);

		Set<String> terms;

		if (type == Type.GAIN) terms = collectTerms(mods[0]);
		else if (type == Type.LOSS) terms = collectTerms(mods[1]);
		else terms = collectTerms(mods);

		return termsContainDesired(terms);
	}

	private Set<String> collectTerms(Set<ModificationFeature>... mods)
	{
		Set<String> terms = new HashSet<String>();
		collectTerms(mods[0], terms);
		if (mods.length > 1) collectTerms(mods[1], terms);
		return terms;
	}

	private void collectTerms(Set<ModificationFeature> mods, Set<String> terms)
	{
		for (ModificationFeature mf : mods)
		{
			SequenceModificationVocabulary type = mf.getModificationType();
			if (type != null)
			{
				for (String term : type.getTerm())
				{
					terms.add(term.toLowerCase());
				}
			}
		}
	}

	/**
	 * Checks if any element in the set contains the term.
	 * @param terms changed terms
	 * @return true if any changed terms contains a desired substring
	 */
	private boolean termsContainDesired(Set<String> terms)
	{
		if (terms.isEmpty()) return false;
		if (featureSubstring.length == 0) return true;

		for (String term : terms)
		{
			for (String sub : featureSubstring)
			{
				if (term.contains(sub)) return true;
			}
		}
		return false;
	}

	public enum Type
	{
		GAIN,
		LOSS,
		ANY
	}
}
