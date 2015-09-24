package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcidReference;
import org.biopax.paxtools.model.level3.NucleicAcidRegionReference;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;

public abstract class NucleicAcidReferenceImpl extends SequenceEntityReferenceImpl implements NucleicAcidReference
{
	private Set<NucleicAcidRegionReference> subRegion;

	public NucleicAcidReferenceImpl()
	{
		this.subRegion = BPCollections.I.createSafeSet();
	}

	public Set<NucleicAcidRegionReference> getSubRegion()
	{
		return subRegion;
	}

	public void addSubRegion(NucleicAcidRegionReference regionReference)
	{
		if (regionReference != null)
		{
			subRegion.add(regionReference);
			regionReference.getSubRegionOf().add(this);
		}
	}

	public void removeSubRegion(NucleicAcidRegionReference regionReference)
	{
		if (regionReference != null)
		{
			subRegion.remove(regionReference);
		}
	}
}

