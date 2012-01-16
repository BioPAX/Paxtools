package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.XrefFieldBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;

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
@Proxy(proxyClass= XReferrable.class)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

	
	@Field(name="xref", index=Index.UN_TOKENIZED, bridge = @FieldBridge(impl=XrefFieldBridge.class), boost=@Boost(1.5f))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
		if (element instanceof XReferrable)
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
	 * This method returns true if two {@link XReferrable} objects have at least one UnificationXref in common
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
