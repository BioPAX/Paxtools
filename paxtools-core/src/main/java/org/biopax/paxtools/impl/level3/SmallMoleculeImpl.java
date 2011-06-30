package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class SmallMoleculeImpl extends SimplePhysicalEntityImpl
		implements SmallMolecule
{
	public SmallMoleculeImpl() {
	}
	
	@Override @Transient
	public Class<? extends PhysicalEntity> getModelInterface() {
		return SmallMolecule.class;
	}
	
}
