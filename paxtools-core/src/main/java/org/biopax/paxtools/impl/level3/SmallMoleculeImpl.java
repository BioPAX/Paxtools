package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;


public class SmallMoleculeImpl extends SimplePhysicalEntityImpl
		implements SmallMolecule
{
	public SmallMoleculeImpl() {
	}
	
	@Override
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
