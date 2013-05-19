package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Degradation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= Degradation.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DegradationImpl extends ConversionImpl implements Degradation
{
	public DegradationImpl() {
	}
	
    @Transient
	public Class<? extends Degradation> getModelInterface()
    {
        return Degradation.class;
    }

}
