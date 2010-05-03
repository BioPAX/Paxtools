package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Provenance;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed
public class ProvenanceImpl extends NamedImpl implements Provenance
{

	@Transient
    public Class<? extends Provenance> getModelInterface()
	{
		return Provenance.class;
	}

}
