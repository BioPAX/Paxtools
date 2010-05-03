package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Indexed
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
    @Transient
	public Class<? extends SequenceLocation> getModelInterface()
	{
		return SequenceLocation.class;
	}

	//
	// SequenceLocation interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property Region-TYPE
    @ManyToMany(targetEntity = SequenceRegionVocabularyImpl.class)
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
