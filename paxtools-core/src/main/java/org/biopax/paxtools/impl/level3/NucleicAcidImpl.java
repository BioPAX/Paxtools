package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcid;

import javax.persistence.Entity;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class NucleicAcidImpl extends SimplePhysicalEntityImpl implements NucleicAcid {
	public NucleicAcidImpl() {
	}
}
