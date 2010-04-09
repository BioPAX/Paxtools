
package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.hibernate.search.annotations.Indexed;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.proxy.BioPAXElementProxy;

/**
 *
 * @author IgorRodchenkov
 */
@Entity(name="l3dnaregion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DnaRegionProxy extends SimplePhysicalEntityProxy<DnaRegion> implements DnaRegion{

    @Transient
	public Class<? extends PhysicalEntity> getModelInterface() {
    	return DnaRegion.class;
    }
}
