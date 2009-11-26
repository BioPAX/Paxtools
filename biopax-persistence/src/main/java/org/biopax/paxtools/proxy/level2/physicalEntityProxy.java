/*
 * PhysicalEntityProxy.java
 *
 * 2007.03.16 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for physicalEntity
 */
@Entity(name = "l2physicalentity")
@Indexed(index = BioPAXElementProxy.SEARCH_INDEX_NAME)
public class physicalEntityProxy extends entityProxy
	implements physicalEntity, Serializable
{
	protected physicalEntityProxy()
	{
	}

	@Transient
	public Class getModelInterface()
	{
		return physicalEntity.class;
	}

	@Transient
	public Set<physicalEntityParticipant> isPHYSICAL_ENTITYof()
	{
		return ((physicalEntity) object).isPHYSICAL_ENTITYof();
	}

	@Transient
	public Set<interaction> getAllInteractions()
	{
		return ((physicalEntity) object).getAllInteractions();
	}

	@Transient
	public <T extends interaction> Set<T> getAllInteractions(Class<T> ofType)
	{
		return ((physicalEntity) object).getAllInteractions(ofType);
	}

	@Transient
	public void addPHYSICAL_ENTITYof(physicalEntityParticipant pep)
	{
		((physicalEntity) object).addPHYSICAL_ENTITYof(pep);
	}

	@Transient
	public void removePHYSICAL_ENTITYof(physicalEntityParticipant pep)
	{
		((physicalEntity) object).removePHYSICAL_ENTITYof(pep);
	}
}

