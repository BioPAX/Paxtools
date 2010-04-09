/*
 * ExperimentalFormProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;

/**
 * Proxy for experimentalForm
 */
@Entity(name="l3experimentalform")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ExperimentalFormProxy extends Level3ElementProxy<ExperimentalForm> implements
	ExperimentalForm {

	// Property EXPERIMENTAL-FORM-TYPE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = ExperimentalFormVocabularyProxy.class)
	@JoinTable(name="l3expform_exp_form_desc")
	public Set<ExperimentalFormVocabulary> getExperimentalFormDescription() {
		return object.getExperimentalFormDescription();
	}

	public void addExperimentalFormDescription(ExperimentalFormVocabulary EXPERIMENTAL_FORM_TYPE) {
		object.addExperimentalFormDescription(EXPERIMENTAL_FORM_TYPE);
	}

	public void removeExperimentalFormDescription(ExperimentalFormVocabulary EXPERIMENTAL_FORM_TYPE) {
		object.removeExperimentalFormDescription(EXPERIMENTAL_FORM_TYPE);
	}

	public void setExperimentalFormDescription(Set<ExperimentalFormVocabulary> EXPERIMENTAL_FORM_TYPE) {
		object.setExperimentalFormDescription(EXPERIMENTAL_FORM_TYPE);
	}

	// Property PARTICIPANT

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class)
	@JoinColumn(name="experimental_form_entity_x")
	public org.biopax.paxtools.model.level3.Entity getExperimentalFormEntity() {
		return object.getExperimentalFormEntity();
	}

    /*
	public void setExperimentalFormEntity(PhysicalEntity newPARTICIPANT) {
		object.setExperimentalFormEntity(newPARTICIPANT);
	}

    public void setExperimentalFormEntity(Gene newPARTICIPANT) {
		object.setExperimentalFormEntity(newPARTICIPANT);
	}
    */

    public void setExperimentalFormEntity(org.biopax.paxtools.model.level3.Entity newPARTICIPANT) {
        object.setExperimentalFormEntity(newPARTICIPANT);
    }
    
	// Property ExperimentalFeature
	
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityFeatureProxy.class)
	@JoinTable(name="l3expform_exp_feture")
	public Set<EntityFeature> getExperimentalFeature() {
		return object.getExperimentalFeature();
	}

	public void setExperimentalFeature(Set<EntityFeature> experimentalFeature) {
		object.setExperimentalFeature(experimentalFeature);
	}

	public void addExperimentalFeature(EntityFeature experimentalFeature) {
		object.addExperimentalFeature(experimentalFeature);
	}

	public void removeExperimentalFeature(EntityFeature experimentalFeature) {
		object.removeExperimentalFeature(experimentalFeature);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return ExperimentalForm.class;
	}

}

