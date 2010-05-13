package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.NucleicAcidReference;

import javax.persistence.Entity;

@Entity
public abstract class NucleicAcidReferenceImpl extends SequenceEntityReferenceImpl 
	implements NucleicAcidReference
{
	public NucleicAcidReferenceImpl() {
	}
}
