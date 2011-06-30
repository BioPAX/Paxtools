package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.ClassFilterSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

import static org.biopax.paxtools.util.SetEquivalanceChecker.isEquivalentIntersection;

/**
 * This class helps with managing the bidirectional xref links.
 *
 * @author Emek Demir
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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
	 */

	public XReferrableImpl()
	{
		this.xref = new HashSet<Xref>();
	}

// -------------------------- OTHER METHODS --------------------------



	@ManyToMany(targetEntity = XrefImpl.class)
	@JoinTable(name="xref")
	public Set<Xref> getXref()
	{
		return xref;
	}

	public void removeXref(Xref xref)
	{
		if (xref != null) {
			this.xref.remove(xref);
			xref.getXrefOf().remove(this);
		}
	}

	protected void setXref(Set<Xref> xref)
	{
		this.xref = xref;
	}

	public void addXref(Xref xref)
	{
		if (xref != null) {
			this.xref.add(xref);
			xref.getXrefOf().add(this);
		}
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		boolean equivalence = false;
		if (element!=null && element instanceof XReferrable)
		{
			equivalence = hasCommonUnificationXref((XReferrable) element);
		}
		return equivalence;
	}

	@Override
	public int equivalenceCode()
	{
		return 1;
	}

	/**
	 * Answers whether two BioPAX objects that implement 
	 * @see XReferrable 
	 * interface have at least one UnificationXref in common
	 * or neither have any.
	 * 
	 * @param xReferrable
	 * @return true if this and that either share - or neither have a UnificationXref!
	 */
	protected boolean hasCommonUnificationXref(XReferrable xReferrable)
	{
		return isEquivalentIntersection(
				new ClassFilterSet<Xref,UnificationXref>(xref, UnificationXref.class),
				new ClassFilterSet<Xref,UnificationXref>(xReferrable.getXref(), UnificationXref.class)
		);
	}
}
