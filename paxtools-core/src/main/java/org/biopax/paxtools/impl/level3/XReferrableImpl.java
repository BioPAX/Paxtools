package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
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
public abstract class XReferrableImpl extends L3ElementImpl implements XReferrable
{
// ------------------------------ FIELDS ------------------------------

	/**
	 * This variable stores the external references to the owner object.
	 */
	private Set<Xref> xref;

// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Default constructor.
	 *
	 */
	public XReferrableImpl()
	{
		this.xref = new HashSet<Xref>();
	}

// -------------------------- OTHER METHODS --------------------------

	public void addXref(Xref xref)
	{
		this.xref.add(xref);

		xref.getXrefOf().add(this);
	}

	public Set<Xref> getXref()
	{
		return xref;
	}

	public void removeXref(Xref xref)
	{
		this.xref.remove(xref);
		xref.getXrefOf().remove(this);
	}

	public void setXref(Set<Xref> xref)
	{
        this.xref = xref;
    }

}
