package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;

import java.util.HashSet;
import java.util.Set;

class SequenceLocationImpl extends L3ElementImpl implements
                                                              SequenceLocation
{
	private Set<SequenceRegionVocabulary> regionType;


	/**
	 * Constructor.
	 */
	public SequenceLocationImpl()
	{
		this.regionType = new HashSet<SequenceRegionVocabulary>();
    }

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends SequenceLocation> getModelInterface()
	{
		return SequenceLocation.class;
	}

	//
	// SequenceLocation interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property Region-TYPE

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
		this.regionType.remove(regionType);
	}

	public void setRegionType(Set<SequenceRegionVocabulary> regionType)
	{
        this.regionType = regionType;
	}





}
