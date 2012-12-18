package org.biopax.paxtools.causality;

import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.RelatedPEHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ChangeComparator
{
	// Effected change
	protected RelatedPEHandler a1;
	protected RelatedPEHandler a2;

	// Effecting change
	protected RelatedPEHandler b1;
	protected RelatedPEHandler b2;

	Evidence ev;

	public ChangeComparator(RelatedPEHandler a1, RelatedPEHandler a2, 
		RelatedPEHandler b1, RelatedPEHandler b2)
	{
		this.a1 = a1;
		this.a2 = a2;
		this.b1 = b1;
		this.b2 = b2;

		Evidence ev1 = compareFeatureChanges();
		Evidence ev2 = compareCompartmentChanges();
		
		if (ev1 == ev2) ev = ev1;
		else if (ev1 == Evidence.NONE) ev = ev2;
		else if (ev2 == Evidence.NONE) ev = ev1;
		else if (ev1 == Evidence.CONFLICTING) ev = ev1;
		else if (ev1.effecting() && ev2.effecting()) ev = Evidence.CONFLICTING;
	}

	public int getEdgeSign()
	{
		switch (ev)
		{
			case ACTIVATING: return 1;
			case INHIBITING: return -1;
			default: return 0;
		}
	}

	protected Set<ModificationFeature> getFeatureDiff(RelatedPEHandler h1, RelatedPEHandler h2,
		boolean gained)
	{
		Set<ModificationFeature> set1 = collectFeatures(h1);
		Set<ModificationFeature> set2 = collectFeatures(h2);

		if (gained)
		{
			set2.removeAll(set1);
			return set2;
		}
		else
		{
			set1.removeAll(set2);
			return set1;
		}
	}

	protected Set<ModificationFeature> collectFeatures(RelatedPEHandler h)
	{
		Set<ModificationFeature> set = new HashSet<ModificationFeature>();

		for (PhysicalEntity pe : h.pes)
		{
			collectFeatures(pe, set);
		}

		return set;
	}
	protected void collectFeatures(PhysicalEntity pe, Set<ModificationFeature> set)
	{
		for (EntityFeature ef : pe.getFeature())
		{
			if (ef instanceof ModificationFeature)
			{
				set.add((ModificationFeature) ef);
			}
		}
	}

	protected Evidence compareFeatureChanges()
	{
		return compareGainedAndLost(
			collectFeatures(b1), collectFeatures(b2), 
			collectFeatures(a1), collectFeatures(a2));
	}

	protected Evidence compareGainedAndLost(
		Set<ModificationFeature> gained1, Set<ModificationFeature> lost1,
		Set<ModificationFeature> gained2, Set<ModificationFeature> lost2)
	{
		boolean act = intersects(gained1, gained2) || intersects(lost1, lost2);
		boolean inh = intersects(gained1, lost2) || intersects(lost1, gained2);

		if (act) if (inh) return Evidence.CONFLICTING; else return Evidence.ACTIVATING;
		else if (inh) return Evidence.INHIBITING; else return Evidence.NONE;
	}

	private boolean intersects(Set set1, Set set2)
	{
		if (set1.isEmpty() || set2.isEmpty()) return false;

		for (Object o : set1)
		{
			if (set2.contains(o)) return true;
		}
		return false;
	}

	private Evidence compareCompartmentChanges()
	{
		String ca1 = a1.getCellularLocation();
		String ca2 = a2.getCellularLocation();
		String cb1 = b1.getCellularLocation();
		String cb2 = b2.getCellularLocation();
		
		if (cb2 != null && (cb1 == null || !cb2.equals(cb1)))
		{
			if (ca2 != null && (ca1 == null || !ca2.equals(ca1)) && cb2.equals(ca2))
				return Evidence.ACTIVATING;
			if (ca1 != null && (ca2 == null || !ca1.equals(ca2)) && cb2.equals(ca1))
				return Evidence.INHIBITING;
		}

		return Evidence.NONE;
	}

	enum Evidence
	{
		ACTIVATING(true), INHIBITING(true), CONFLICTING(false), NONE(false);

		private boolean effecting;

		Evidence(boolean effecting)
		{
			this.effecting = effecting;
		}

		public boolean effecting()
		{
			return effecting;
		}
	}

}
