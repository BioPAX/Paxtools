package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.XrefFieldBridge;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.Set;

/**
 * This class helps with managing the bidirectional xref links.
 *
 * @author Emek Demir
 */
@Entity
@Proxy(proxyClass= XReferrable.class)
@DynamicUpdate @DynamicInsert
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
		this.xref = BPCollections.I.createSafeSet();
	}

// -------------------------- OTHER METHODS --------------------------

	
	@Field(name=FIELD_XREFID, analyze=Analyze.NO, bridge = @FieldBridge(impl=XrefFieldBridge.class), boost=@Boost(1.5f))
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
			synchronized (xref) {
				xref.getXrefOf().remove(this);
			}
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
			synchronized (xref) {
				xref.getXrefOf().add(this);
			}
		}
	}


	@Override
	public int equivalenceCode()
	{
		return 1;
	}

}
