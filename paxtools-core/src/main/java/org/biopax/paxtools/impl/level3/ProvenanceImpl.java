package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Provenance;

public class ProvenanceImpl extends NamedImpl implements Provenance
{

	public ProvenanceImpl()
	{
	}

	public Class<? extends Provenance> getModelInterface()
	{
		return Provenance.class;
	}

}
