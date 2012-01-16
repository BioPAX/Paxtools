package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ChildDataStringBridge;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@javax.persistence.Entity
@Proxy(proxyClass= ExperimentalForm.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

	@Field(name="data", index=Index.TOKENIZED, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = ExperimentalFormVocabularyImpl.class)
	@JoinTable(name="experimentalFormDescription")
	public Set<ExperimentalFormVocabulary> getExperimentalFormDescription()
	{
		return experimentalFormDescription;
	}

	public void addExperimentalFormDescription(
		ExperimentalFormVocabulary experimentalFormType)
	{
		if(experimentalFormType != null)
			this.experimentalFormDescription.add(experimentalFormType);
	}

	public void removeExperimentalFormDescription(
		ExperimentalFormVocabulary experimentalFormType)
	{
		if(experimentalFormType != null)
			this.experimentalFormDescription.remove(experimentalFormType);
	}

	public void setExperimentalFormDescription(
		Set<ExperimentalFormVocabulary> experimentalFormDescription)
	{
		this.experimentalFormDescription = experimentalFormDescription;
	}

	@ManyToOne(targetEntity = EntityImpl.class)//, cascade={CascadeType.ALL})
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

	@Field(name="data", index=Index.TOKENIZED, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ManyToMany(targetEntity = EntityFeatureImpl.class)
    @JoinTable(name="experimentalFeature")
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
    	if(experimentalFeature != null)
    		this.experimentalFeature.add(experimentalFeature);
    }
    
    public void removeExperimentalFeature(EntityFeature experimentalFeature)
    {
    	if(experimentalFeature != null)
    		this.experimentalFeature.remove(experimentalFeature);
    }
}
