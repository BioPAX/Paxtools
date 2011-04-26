package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Provenance;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class ProvenanceImpl extends NamedImpl implements Provenance
{
	public ProvenanceImpl() {
	}

	@Transient
    public Class<? extends Provenance> getModelInterface()
	{
		return Provenance.class;
	}

}
