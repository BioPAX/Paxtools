package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * This class helps with managing the bidirectional xref links.
 *
 * @author Emek Demir
 */
@Entity
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

	protected XReferrableImpl()
	{
		this.xref = new HashSet<Xref>();
	}

// -------------------------- OTHER METHODS --------------------------

	@ManyToMany(targetEntity = XrefImpl.class)
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

	private void setXref(Set<Xref> xref)
	{
        this.xref = xref;
    }

}
