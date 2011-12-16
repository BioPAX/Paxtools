package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaReference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RnaReferenceImpl extends NucleicAcidReferenceImpl
	implements RnaReference
{

	public RnaReferenceImpl() {
	}

	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
    @Transient
	public Class<? extends RnaReference> getModelInterface()
	{
		return RnaReference.class;
	}
}
