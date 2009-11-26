
package org.biopax.paxtools.proxy.level3;

import java.io.Serializable;
import org.hibernate.search.annotations.Indexed;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.PhysicalEntity;

/**
 *
 * @author IgorRodchenkov
 */
@Entity(name="l3dnaregion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DnaRegionProxy extends SimplePhysicalEntityProxy implements DnaRegion, Serializable{

    public DnaRegionProxy() {
    }

    @Transient
	public Class getModelInterface() {
		return DnaRegion.class;
	}

	@Transient
    public Class<? extends PhysicalEntity> getPhysicalEntityClass() {
        return DnaRegion.class;

    }

}
