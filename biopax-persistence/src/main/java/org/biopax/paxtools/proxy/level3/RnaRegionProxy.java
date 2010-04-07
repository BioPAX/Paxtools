
package org.biopax.paxtools.proxy.level3;

import javax.persistence.Transient;

import org.hibernate.search.annotations.Indexed;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.proxy.BioPAXElementProxy;

/**
 *
 * @author IgorRodchenkov
 */
@javax.persistence.Entity(name="l3rnaregion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaRegionProxy extends SimplePhysicalEntityProxy implements RnaRegion{
    
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return RnaRegion.class;
	}
}
