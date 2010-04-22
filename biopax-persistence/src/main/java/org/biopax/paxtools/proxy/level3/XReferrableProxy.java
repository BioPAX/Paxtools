package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;

import javax.persistence.CascadeType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.Set;

@javax.persistence.Entity(name="l3xreferrableProxy")
public abstract class XReferrableProxy<T extends XReferrable> 
		extends Level3ElementProxy<T>
		implements XReferrable
{
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = XrefProxy.class)
	@JoinTable(name="l3entity_xref")
	public Set<Xref> getXref() {
		return object.getXref();
	}

	public void addXref(Xref XREF) {
		object.addXref(XREF);
	}

	public void removeXref(Xref XREF) {
		object.removeXref(XREF);
	}


}
