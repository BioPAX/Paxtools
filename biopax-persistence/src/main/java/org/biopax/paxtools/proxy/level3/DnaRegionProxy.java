
package org.biopax.paxtools.proxy.level3;

import org.hibernate.search.annotations.Indexed;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.DnaRegion;

/**
 *
 * @author IgorRodchenkov
 */
@Entity(name="l3dnaregion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DnaRegionProxy extends SimplePhysicalEntityProxy implements DnaRegion{

    public DnaRegionProxy() {
    }

    @Transient
	public Class<? extends BioPAXElement> getModelInterface() {
    	return DnaRegion.class;
    }
}
