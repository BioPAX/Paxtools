package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaReference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= RnaReference.class)
@Indexed
@DynamicUpdate @DynamicInsert
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
