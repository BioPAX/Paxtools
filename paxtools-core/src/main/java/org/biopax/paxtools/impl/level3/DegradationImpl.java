package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Degradation;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class DegradationImpl extends ConversionImpl implements Degradation
{
	public DegradationImpl() {
	}
	
    @Transient
	public Class<? extends Degradation> getModelInterface()
    {
        return Degradation.class;
    }

}
