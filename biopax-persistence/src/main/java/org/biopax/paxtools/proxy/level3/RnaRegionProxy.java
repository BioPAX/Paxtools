
package org.biopax.paxtools.proxy.level3;

import java.io.Serializable;
import org.hibernate.search.annotations.Indexed;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.PhysicalEntity;

/**
 *
 * @author IgorRodchenkov
 */
@Entity(name="l3rnaregion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaRegionProxy extends SimplePhysicalEntityProxy implements RnaRegion, Serializable{

    public RnaRegionProxy() {
    }

    @Transient
	public Class getModelInterface() {
		return RnaRegion.class;
	}

	@Transient
    public Class<? extends PhysicalEntity> getPhysicalEntityClass() {
        return RnaRegion.class;

    }

}
