package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcid;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;

@Entity @Proxy(proxyClass = NucleicAcid.class)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class NucleicAcidImpl extends SimplePhysicalEntityImpl implements NucleicAcid
{
	public NucleicAcidImpl()
	{
	}
}
