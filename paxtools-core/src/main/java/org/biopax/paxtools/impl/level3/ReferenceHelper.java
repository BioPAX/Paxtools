package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;

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
	private Set<Xref> xref;

// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Default constructor.
	 *
	 * @param owner object which this referenceHelper belongs
	 */
	ReferenceHelper(XReferrable owner)
	{
		this.owner = owner;
		this.xref = new HashSet<Xref>();
	}

// -------------------------- OTHER METHODS --------------------------

	void addXref(Xref xref)
	{
		this.xref.add(xref);

		xref.getXrefOf().add(owner);
	}

	Set<Xref> getXref()
	{
		return xref;
	}

	void removeXref(Xref xref)
	{
		this.xref.remove(xref);
		xref.getXrefOf().remove(owner);
	}

	void setXref(Set<Xref> xref)
	{
        this.xref = xref;
    }
}
