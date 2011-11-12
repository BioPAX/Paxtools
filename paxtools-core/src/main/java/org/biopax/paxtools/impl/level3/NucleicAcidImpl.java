package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcid;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class NucleicAcidImpl extends SimplePhysicalEntityImpl implements NucleicAcid {
	public NucleicAcidImpl() {
	}
}
