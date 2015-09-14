package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * XREF Should be only unification and publication xrefs
 */
public interface dataSource extends externalReferenceUtilityClass, XReferrable
{
// -------------------------- OTHER METHODS --------------------------

	void addNAME(String NAME);
// --------------------- ACCESORS and MUTATORS---------------------


	Set<String> getNAME();

	void removeNAME(String NAME);

	void setNAME(Set<String> NAME);
}