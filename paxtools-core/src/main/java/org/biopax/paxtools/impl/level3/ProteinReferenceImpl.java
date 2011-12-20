package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ProteinReference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
 @Proxy(proxyClass= ProteinReference.class)
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProteinReferenceImpl extends SequenceEntityReferenceImpl
        implements ProteinReference
{
	public ProteinReferenceImpl() {
	}
	
    @Transient
    public Class<? extends ProteinReference> getModelInterface()
    {
        return ProteinReference.class;
    }
}
