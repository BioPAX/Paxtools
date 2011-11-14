package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.RnaReference;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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
