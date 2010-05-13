package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcid;

import javax.persistence.Entity;

@Entity
public abstract class NucleicAcidImpl extends SimplePhysicalEntityImpl implements NucleicAcid {
	public NucleicAcidImpl() {
	}
}
