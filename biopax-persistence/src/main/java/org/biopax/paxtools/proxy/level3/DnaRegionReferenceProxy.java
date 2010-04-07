
package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

/**
 *
 * @author IgorRodchenkov
 */
@Entity(name="l3dnaregionreference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DnaRegionReferenceProxy extends SequenceEntityReferenceProxy implements DnaRegionReference {

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

    @Transient
	public Class<? extends BioPAXElement> getModelInterface() {
    	return DnaRegionReference.class;
    }
}
