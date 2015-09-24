package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;


public class ExperimentalFormImpl extends L3ElementImpl implements ExperimentalForm
{

	private Entity experimentalFormEntity;
	private Set<ExperimentalFormVocabulary> experimentalFormDescription;
	private Set<EntityFeature> experimentalFeature;

	/**
	 * Constructor.
	 */
	public ExperimentalFormImpl()
	{
		this.experimentalFormDescription = BPCollections.I.createSafeSet();
		this.experimentalFeature = BPCollections.I.createSafeSet();
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends ExperimentalForm> getModelInterface()
	{
		return ExperimentalForm.class;
	}

	public Set<ExperimentalFormVocabulary> getExperimentalFormDescription()
	{
		return experimentalFormDescription;
	}

	public void addExperimentalFormDescription(ExperimentalFormVocabulary experimentalFormType)
	{
		if(experimentalFormType != null)
			this.experimentalFormDescription.add(experimentalFormType);
	}

	public void removeExperimentalFormDescription(ExperimentalFormVocabulary experimentalFormType)
	{
		if(experimentalFormType != null)
			this.experimentalFormDescription.remove(experimentalFormType);
	}

	public Entity getExperimentalFormEntity()
	{
		return experimentalFormEntity;
	}

	public void setExperimentalFormEntity(Entity experimentalFormEntity)
	{
		if(experimentalFormEntity == null) {
			this.experimentalFormEntity = null;
		} else if(PhysicalEntity.class.isAssignableFrom(experimentalFormEntity.getClass())
                || Gene.class.isInstance(experimentalFormEntity)) {
                this.experimentalFormEntity = experimentalFormEntity;
        } else {
            throw new IllegalBioPAXArgumentException(
                    "Argument type is not yet supported: "
                    + experimentalFormEntity.getModelInterface());
        }
	}

    public Set<EntityFeature> getExperimentalFeature()
    {
        return experimentalFeature;
    }

    public void addExperimentalFeature(EntityFeature experimentalFeature)
    {
    	if(experimentalFeature != null)
    		this.experimentalFeature.add(experimentalFeature);
    }
    
    public void removeExperimentalFeature(EntityFeature experimentalFeature)
    {
    	if(experimentalFeature != null)
    		this.experimentalFeature.remove(experimentalFeature);
    }
}
