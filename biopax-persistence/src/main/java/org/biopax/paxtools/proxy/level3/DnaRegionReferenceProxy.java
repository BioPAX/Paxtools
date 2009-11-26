
package org.biopax.paxtools.proxy.level3;

import java.io.Serializable;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.hibernate.search.annotations.Indexed;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.CascadeType;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
/**
 *
 * @author IgorRodchenkov
 */
@Entity(name="l3dnaregionreference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DnaRegionReferenceProxy extends SequenceEntityReferenceProxy implements DnaRegionReference, Serializable {

    public DnaRegionReferenceProxy() {
    }

    @Transient
    @Override
	public Class getModelInterface() {
		return DnaRegionReference.class;
	}

   	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = DnaRegionReferenceProxy.class)
	@JoinColumn(name="sub_region_x")
    public DnaRegionReference getSubRegion() {
        return ((DnaRegionReference)object).getSubRegion();
    }

    public void setSubRegion(DnaRegionReference dnaRegionReference) {
        ((DnaRegionReference)object).setSubRegion(dnaRegionReference);
    }

   	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = SequenceLocationProxy.class)
	@JoinColumn(name="sequencelocation_x")
	public SequenceLocation getAbsoluteRegion() {
        return ((DnaRegionReferenceProxy)object).getAbsoluteRegion();
    }

    public void setAbsoluteRegion(SequenceLocation sequenceLocation) {
        ((DnaRegionReferenceProxy)object).setAbsoluteRegion(sequenceLocation);
    }

}
