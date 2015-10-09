package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
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


	@Override
	public void setEntityReference(EntityReference entityReference) {
		if(entityReference instanceof SmallMoleculeReference || entityReference == null)
			super.setEntityReference(entityReference);
		else
			throw new IllegalBioPAXArgumentException("setEntityReference failed: "
					+ entityReference.getUri() + " is not a SmallMoleculeReference.");
	}
}
