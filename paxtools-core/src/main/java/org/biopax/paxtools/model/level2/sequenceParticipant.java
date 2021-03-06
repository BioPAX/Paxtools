package org.biopax.paxtools.model.level2;

import java.util.Set;

public interface sequenceParticipant extends physicalEntityParticipant
{
// -------------------------- OTHER METHODS --------------------------

	void addSEQUENCE_FEATURE_LIST(sequenceFeature SEQUENCE_FEATURE);
// --------------------- ACCESORS and MUTATORS---------------------

	Set<sequenceFeature> getSEQUENCE_FEATURE_LIST();

	void removeSEQUENCE_FEATURE_LIST(sequenceFeature SEQUENCE_FEATURE);

	void setSEQUENCE_FEATURE_LIST(Set<sequenceFeature> SEQUENCE_FEATURE_LIST);
}