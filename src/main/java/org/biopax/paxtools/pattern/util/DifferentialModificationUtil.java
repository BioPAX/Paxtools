package org.biopax.paxtools.pattern.util;

import org.biopax.paxtools.model.level3.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * THis class is used for comparing modification features of two PhysicalEntity objects of same
 * EntityReference.
 *
 * @author Ozgun Babur
 */
public class DifferentialModificationUtil
{
	/**
	 * Gets the differential features.
	 * @param before first entity
	 * @param after second entity
	 * @return array of differential features. index 0: gained features, index 1: lost features
	 */
	public static Set<ModificationFeature>[] getChangedModifications(PhysicalEntity before,
		PhysicalEntity after)
	{
		Set<Modification> set1 = collectFeatures(before);
		Set<Modification> set2 = collectFeatures(after);

		Set<Modification> temp = new HashSet<Modification>(set1);
		set1.removeAll(set2);
		set2.removeAll(temp);

		return new Set[]{collectFeatures(set2), collectFeatures(set1)};
	}

	private static Set<Modification> collectFeatures(PhysicalEntity pe)
	{
		Set<Modification> set = new HashSet<Modification>();

		for (EntityFeature f : pe.getFeature())
		{
			if (f instanceof ModificationFeature)
				set.add(new Modification((ModificationFeature) f));
		}
		return set;
	}

	private static Set<ModificationFeature> collectFeatures(Set<Modification> wrappers)
	{
		if (wrappers.isEmpty()) return Collections.emptySet();

		Set<ModificationFeature> set = new HashSet<ModificationFeature>();

		for (Modification wrapper : wrappers)
		{
			set.add(wrapper.mf);
		}
		return set;
	}

	/**
	 * Wrapper for ModificationFeature.
	 */
	private static class Modification
	{
		ModificationFeature mf;

		Modification(ModificationFeature mf)
		{
			this.mf = mf;
		}

		@Override
		public int hashCode()
		{
			int code = 0;

			SequenceModificationVocabulary type = mf.getModificationType();
			if (type != null)
			{
				for (String term : type.getTerm())
				{
					code += term.hashCode();
				}
			}

			SequenceLocation loc = mf.getFeatureLocation();
			if (loc != null)
			{
				if (loc instanceof SequenceInterval)
				{
					SequenceSite begin = ((SequenceInterval) loc).getSequenceIntervalBegin();
					SequenceSite end = ((SequenceInterval) loc).getSequenceIntervalEnd();

					if (begin != null) code += begin.getSequencePosition();
					if (end != null) code += end.getSequencePosition();
				}
				else if (loc instanceof SequenceSite)
				{
					code += ((SequenceSite) loc).getSequencePosition();
				}
			}
			return code;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Modification)) return false;

			Modification m = (Modification) obj;

			if (mf == m.mf) return true;

			if (mf.getModificationType() == null || m.mf.getModificationType() == null)
				return false;

			if (!mf.getModificationType().getTerm().containsAll(
				m.mf.getModificationType().getTerm())) return false;

			if (mf.getFeatureLocation() == null && m.mf.getFeatureLocation() == null) return true;

			if (mf.getFeatureLocation() == null || m.mf.getFeatureLocation() == null) return false;

			return mf.getFeatureLocation().equivalenceCode() ==
				m.mf.getFeatureLocation().equivalenceCode();
		}
	}
}
