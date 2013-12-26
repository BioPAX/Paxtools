package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.util.BPCollections;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Transient;
import java.util.Set;

@javax.persistence.Entity
@Proxy(proxyClass= SequenceLocation.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SequenceLocationImpl extends L3ElementImpl
	implements SequenceLocation
{
	private Set<SequenceRegionVocabulary> regionType;

	/**
	 * Constructor.
	 */
	public SequenceLocationImpl()
	{
		this.regionType = BPCollections.createSafeSet();
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
