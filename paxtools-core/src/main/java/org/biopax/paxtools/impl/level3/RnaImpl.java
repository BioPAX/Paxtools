package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Rna;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= Rna.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RnaImpl extends NucleicAcidImpl  implements Rna
{

	public RnaImpl() {
	}
// --------------------- Interface BioPAXElement ---------------------

	@Transient
    public Class<? extends Rna> getModelInterface()
	{
		return Rna.class;
	}
}
