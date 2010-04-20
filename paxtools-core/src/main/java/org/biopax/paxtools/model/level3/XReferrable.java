package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 */
public interface XReferrable extends Level3Element
{

	// Property XREF

	Set<Xref> getXref();

	void addXref(Xref xref);

	void removeXref(Xref xref);

}
