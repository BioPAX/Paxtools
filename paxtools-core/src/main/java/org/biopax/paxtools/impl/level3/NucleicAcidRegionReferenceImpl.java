package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcidReference;
import org.biopax.paxtools.model.level3.NucleicAcidRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


public abstract class NucleicAcidRegionReferenceImpl extends NucleicAcidReferenceImpl
		implements NucleicAcidRegionReference
{

	private SequenceLocation absoluteRegion;

	private Set<SequenceRegionVocabulary> regionType;

	private Set<NucleicAcidReference> subRegionOf;

	public NucleicAcidRegionReferenceImpl()
	{
		regionType = BPCollections.I.createSafeSet();
		subRegionOf = BPCollections.I.createSafeSet();
	}

	public Set<NucleicAcidReference> getSubRegionOf()
	{
		return subRegionOf;
	}

	public SequenceLocation getAbsoluteRegion()
	{
		return this.absoluteRegion;
	}

	public void setAbsoluteRegion(SequenceLocation absoluteRegion)
	{
		this.absoluteRegion = absoluteRegion;

	}

	public Set<SequenceRegionVocabulary> getRegionType()
	{
		return this.regionType;
	}

	public void addRegionType(SequenceRegionVocabulary regionType)
	{
		if (regionType != null) this.regionType.add(regionType);
	}

	public void removeRegionType(SequenceRegionVocabulary regionType)
	{
		if (regionType != null) this.regionType.remove(regionType);
	}

}
