package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_UTILILTY_CLASS)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class SequenceLocationImpl extends L3ElementImpl
	implements SequenceLocation
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

}
