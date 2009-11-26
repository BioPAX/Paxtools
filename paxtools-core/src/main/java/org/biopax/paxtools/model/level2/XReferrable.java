package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 */
public interface XReferrable extends Level2Element
{

	void addXREF(xref XREF);

	Set<xref> getXREF();

	void removeXREF(xref XREF);

	void setXREF(Set<xref> XREF);

	Set<unificationXref> findCommonUnifications(XReferrable that);

	Set<relationshipXref> findCommonRelationships(XReferrable that);

	Set<publicationXref> findCommonPublications(XReferrable that);

}
