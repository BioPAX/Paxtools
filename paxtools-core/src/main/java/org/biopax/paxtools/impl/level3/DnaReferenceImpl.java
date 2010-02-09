package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaReference;
import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.model.level3.DnaRegionReference;

import java.util.Set;

class DnaReferenceImpl extends SequenceEntityReferenceImpl implements
		DnaReference
{
	private SequenceInterval genomicRegion;
	private Set<SequenceRegionVocabulary> regionType;
	private Set<DnaRegionReference> subRegion;

	

	@Override
	public Class<? extends DnaReference> getModelInterface()
	{                                        
		return DnaReference.class;
	}

	public SequenceInterval getGenomicRegion()
	{
		return genomicRegion;
	}

	public void setGenomicRegion(SequenceInterval genomicRegion)
	{
		this.genomicRegion = genomicRegion;
	}

	public void setRegionType(Set<SequenceRegionVocabulary> regionType)
	{
		this.regionType = regionType;
	}

	public Set<SequenceRegionVocabulary> getRegionType()
	{
		return regionType;
	}

	public void addRegionType(SequenceRegionVocabulary regionType)
	{
		this.regionType.add(regionType);
	}

	public void removeRegionType(SequenceRegionVocabulary regionType)
	{
		assert this.regionType.contains(regionType):
				"Trying to remove a region type that does not exist!";
		this.regionType.remove(regionType);
	}
	
	
	public void setSubRegion(Set<DnaRegionReference> subRegion)
	{
		this.subRegion = subRegion;
	}

	public Set<DnaRegionReference> getSubRegion()
	{
		return subRegion;
	}

	public void addSubRegion(DnaRegionReference subRegion)
	{
		this.subRegion.add(subRegion);
	}

	public void removeSubRegion(DnaRegionReference subRegion)
	{
		assert this.subRegion.contains(subRegion):
				"Trying to remove a sub region type that does not exist!";
		this.subRegion.remove(subRegion);
	}
	
}


