
package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.hibernate.search.annotations.Indexed;
import javax.persistence.Entity;
import javax.persistence.*;

/**
 *
 * @author IgorRodchenkov
 */
@Entity(name="l3rnaregionreference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaRegionReferenceProxy extends SequenceEntityReferenceProxy 
	implements RnaRegionReference 
{

    public RnaRegionReferenceProxy() {
    }

    @Transient
    @Override
	public Class getModelInterface() {
		return RnaRegionReference.class;
	}

   	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = RnaRegionReferenceProxy.class)
	@JoinColumn(name="sub_region_x")
    public RnaRegionReference getSubRegion() {
        return ((RnaRegionReference)object).getSubRegion();
    }

    public void setSubRegion(RnaRegionReference rnaRegionReference) {
        ((RnaRegionReference)object).setSubRegion(rnaRegionReference);
    }

   	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = SequenceLocationProxy.class)
	@JoinColumn(name="sequencelocation_x")
	public SequenceLocation getAbsoluteRegion() {
		return ((RnaRegionReference)object).getAbsoluteRegion();
	}

	public void setAbsoluteRegion(SequenceLocation sequenceLocation) {
		((RnaRegionReference)object).setAbsoluteRegion(sequenceLocation);
	}

}
