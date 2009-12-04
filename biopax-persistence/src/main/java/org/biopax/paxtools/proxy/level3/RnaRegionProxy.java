
package org.biopax.paxtools.proxy.level3;

import org.hibernate.search.annotations.Indexed;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.biopax.paxtools.model.level3.RnaRegion;

/**
 *
 * @author IgorRodchenkov
 */
@Entity(name="l3rnaregion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaRegionProxy extends SimplePhysicalEntityProxy implements RnaRegion{

    public RnaRegionProxy() {
    }

    @Transient
	public Class getModelInterface() {
		return RnaRegion.class;
	}

}
