package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= SmallMolecule.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SmallMoleculeImpl extends SimplePhysicalEntityImpl
		implements SmallMolecule
{
	public SmallMoleculeImpl() {
	}
	
	@Override @Transient
	public Class<? extends PhysicalEntity> getModelInterface() {
		return SmallMolecule.class;
	}
	
	@Override
	public void addFeature(EntityFeature feature) {
		if(feature==null || feature instanceof BindingFeature)
			super.addFeature(feature);
		else 
			throw new IllegalBioPAXArgumentException(
					"Range violation: SmallMolecule.feature can have only BindingFeature");	
	}
	
	@Override
	public void addNotFeature(EntityFeature feature) {
		if(feature==null || feature instanceof BindingFeature)
			super.addNotFeature(feature);
		else 
			throw new IllegalBioPAXArgumentException(
					"Range violation: SmallMolecule.notFeature can have only BindingFeature");	
	}
	
}
