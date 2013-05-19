package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.UnificationXref;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= UnificationXref.class)
@Indexed
@Boost(1.1f)
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UnificationXrefImpl extends XrefImpl implements UnificationXref {

	public UnificationXrefImpl() {
	}
	
    @Transient
    public Class<? extends UnificationXref> getModelInterface() {
        return UnificationXref.class;
    }
    
}
