
package org.biopax.paxtools.proxy.level3;

import javax.persistence.Transient;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.hibernate.search.annotations.Indexed;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.proxy.BioPAXElementProxy;

/**
 *
 * @author IgorRodchenkov
 */
@javax.persistence.Entity(name="l3rnaregion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaRegionProxy extends SimplePhysicalEntityProxy<RnaRegion> implements RnaRegion{
    
	@Transient
	public Class<? extends PhysicalEntity> getModelInterface() {
		return RnaRegion.class;
	}
}
