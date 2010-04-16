package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.Xref;

import java.util.Set;

class ProvenanceImpl extends XReferrableImpl implements Provenance
{

	private final NameHelper nameHelper;

	/**
	 * Constructor.
	 */
	public ProvenanceImpl()
	{
		this.nameHelper = new NameHelper();
	}

	//
	// BioPAXElement, Xreferrable implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends Provenance> getModelInterface()
	{
		return Provenance.class;
	}


// ------------------------ INTERFACE METHODS ------------------------



//
// named interface implementation
//
/////////////////////////////////////////////////////////////////////////////

public Set<String> getName()
{
	return nameHelper.getName();
}

public void setName(Set<String> name)
{
	nameHelper.setName(name);
}

public void addName(String name)
{
	nameHelper.addName(name);
}

public void removeName(String name)
{
	nameHelper.removeName(name);
}

public String getDisplayName()
{
	return nameHelper.getDisplayName();
}

public void setDisplayName(String displayName)
{
	nameHelper.setDisplayName(displayName);
}

public String getStandardName()
{
	return nameHelper.getStandardName();
}

public void setStandardName(String standardName)
{
	nameHelper.setStandardName(standardName);
}

}
