package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.SetEquivalanceChecker;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This class helps with managing the bidirectional xref links.
 *
 * @author Emek Demir
 */
class ReferenceHelper implements Serializable
{
// ------------------------------ FIELDS ------------------------------

	private final XReferrable owner;

	/**
	 * This variable stores the external references to the owner object.
	 */
	private Set<xref> XREF;

// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Default constructor.
	 *
	 * @param owner object which this referenceHelper belongs
	 */
	ReferenceHelper(XReferrable owner)
	{
		this.owner = owner;
		this.XREF = new HashSet<xref>();
	}

// -------------------------- OTHER METHODS --------------------------

	void addXREF(xref XREF_INST)
	{
		this.XREF.add(XREF_INST);

		XREF_INST.isXREFof().add(owner);
	}

	Set<xref> getXREF()
	{
		return XREF;
	}

	void removeXREF(xref XREF_INST)
	{
		this.XREF.remove(XREF_INST);
		XREF_INST.isXREFof().remove(owner);
	}

	void setXREF(Set<xref> XREF)
	{
		for (xref xref : this.XREF)
		{
			xref.isXREFof().remove(this.owner);
		}

		this.XREF = XREF == null ? new HashSet<xref>() : XREF;
		for (xref xref : this.XREF)
		{
			xref.isXREFof().add(owner);
		}
	}

	Set<unificationXref> findCommonUnifications(XReferrable that)
	{
		return findIntersectionOfType(that, unificationXref.class);
	}

	Set<relationshipXref> findCommonRelationships(XReferrable that)
	{
		return findIntersectionOfType(that, relationshipXref.class);
	}

	Set<publicationXref> findCommonPublications(XReferrable that)
	{
		return findIntersectionOfType(that, publicationXref.class);
	}

	private <T extends xref> Set<T> findIntersectionOfType(XReferrable that, Class<T> type)
	{
		return SetEquivalanceChecker.findEquivalentIntersection(
                new ClassFilterSet<xref, T>(this.XREF, type),
                new ClassFilterSet<xref, T>(that.getXREF(), type));
	}




}
