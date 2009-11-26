package org.biopax.paxtools.model.level3;

import java.util.Set;

public interface ExperimentalForm extends UtilityClass
{
    // Property EXPERIMENTAL-FORM-TYPE

    Set<ExperimentalFormVocabulary> getExperimentalFormDescription();

    void addExperimentalFormDescription(
            ExperimentalFormVocabulary experimentalFormType);

    void removeExperimentalFormDescription(
            ExperimentalFormVocabulary experimentalFormType);

    void setExperimentalFormDescription(
            Set<ExperimentalFormVocabulary> experimentalFormType);

	// Property PARTICIPANT
    //@Range({PhysicalEntity.class, Gene.class})
    Entity getExperimentalFormEntity();

    void setExperimentalFormEntity(Entity newPARTICIPANT);
    //void setExperimentalFormEntity(PhysicalEntity newPARTICIPANT);
    //void setExperimentalFormEntity(Gene newPARTICIPANT);

    Set<EntityFeature> getExperimentalFeature();

    void setExperimentalFeature(Set<EntityFeature> experimentalFeature);

    void addExperimentalFeature(EntityFeature experimentalFeature);

    void removeExperimentalFeature(EntityFeature experimentalFeature);
}
