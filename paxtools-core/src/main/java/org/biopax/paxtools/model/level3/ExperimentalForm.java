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



    Entity getExperimentalFormEntity();

    void setExperimentalFormEntity(Entity newPARTICIPANT);


    Set<EntityFeature> getExperimentalFeature();

    void addExperimentalFeature(EntityFeature experimentalFeature);

    void removeExperimentalFeature(EntityFeature experimentalFeature);
}
