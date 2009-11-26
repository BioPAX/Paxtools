package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * XREF Should be only unification and publication xrefs
 */
public interface dataSource extends externalReferenceUtilityClass, XReferrable
{
// -------------------------- OTHER METHODS --------------------------

	public void addNAME(String NAME);
// --------------------- ACCESORS and MUTATORS---------------------


	public Set<String> getNAME();

	public void removeNAME(String NAME);

	void setNAME(Set<String> NAME);
}