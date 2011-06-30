package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class UnificationXrefImpl extends XrefImpl implements UnificationXref {

	public UnificationXrefImpl() {
	}
	
    @Transient
    public Class<? extends UnificationXref> getModelInterface() {
        return UnificationXref.class;
    }
}
