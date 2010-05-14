package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.ExperimentalForm;
import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import java.util.HashSet;
import java.util.Set;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@javax.persistence.Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_UTILILTY_CLASS)
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
		this.experimentalFormDescription = new HashSet<ExperimentalFormVocabulary>();
		this.experimentalFeature = new HashSet<EntityFeature>();
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	@Transient
	public Class<? extends ExperimentalForm> getModelInterface()
	{
		return ExperimentalForm.class;
	}

	@ManyToMany(targetEntity = ExperimentalFormVocabularyImpl.class, cascade={CascadeType.ALL})
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

	@ManyToOne(targetEntity = EntityImpl.class)
	public Entity getExperimentalFormEntity()
	{
		return experimentalFormEntity;
	}

	
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

    @ManyToMany(targetEntity = EntityFeatureImpl.class, cascade={CascadeType.ALL})
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
