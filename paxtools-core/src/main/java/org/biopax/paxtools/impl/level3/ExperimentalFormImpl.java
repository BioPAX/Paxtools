package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.ExperimentalForm;
import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import java.util.HashSet;
import java.util.Set;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

class ExperimentalFormImpl extends L3ElementImpl implements ExperimentalForm
{

	private Entity experimentalFormEntity;
	private Set<ExperimentalFormVocabulary> experimentalFormDescription;
	private Set<EntityFeature> experimentalFeature;

	/**
	 * Constructor.
	 */
	public ExperimentalFormImpl()
	{
		this.experimentalFormDescription = new HashSet<ExperimentalFormVocabulary>();
		this.experimentalFeature = new HashSet<EntityFeature>();
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends ExperimentalForm> getModelInterface()
	{
		return ExperimentalForm.class;
	}


	//
	// experimentalForm interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property EXPERIMENTAL-FORM-DESCRIPTION

	public Set<ExperimentalFormVocabulary> getExperimentalFormDescription()
	{
		return experimentalFormDescription;
	}

	public void addExperimentalFormDescription(
		ExperimentalFormVocabulary experimentalFormType)
	{
		this.experimentalFormDescription.add(experimentalFormType);
	}

	public void removeExperimentalFormDescription(
		ExperimentalFormVocabulary experimentalFormType)
	{
		this.experimentalFormDescription.remove(experimentalFormType);
	}

	public void setExperimentalFormDescription(
		Set<ExperimentalFormVocabulary> experimentalFormDescription)
	{
		this.experimentalFormDescription = experimentalFormDescription;
	}

	// Property experimentalFormEntity

	public Entity getExperimentalFormEntity()
	{
		return experimentalFormEntity;
	}

    /*
	public void setExperimentalFormEntity(PhysicalEntity experimentalFormEntity)
	{
		this.experimentalFormEntity = experimentalFormEntity;
	}

    public void setExperimentalFormEntity(Gene gene) {
        this.experimentalFormEntity = gene;
    }
    */

	public void setExperimentalFormEntity(Entity experimentalFormEntity)
	{
		if(PhysicalEntity.class.isAssignableFrom(experimentalFormEntity.getClass())
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

    public void setExperimentalFeature(Set<EntityFeature> experimentalFeature)
    {
        this.experimentalFeature = experimentalFeature;
    }

    public void addExperimentalFeature(EntityFeature experimentalFeature)
    {
        this.experimentalFeature.add(experimentalFeature);
    }
    public void removeExperimentalFeature(EntityFeature experimentalFeature)
    {
        this.experimentalFeature.remove(experimentalFeature);
    }
}
