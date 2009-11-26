package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface SequenceLocation extends UtilityClass
{
	// Property LOCATION-TYPE

	 Set<SequenceRegionVocabulary> getRegionType();

	 void addRegionType(SequenceRegionVocabulary regionType);

	 void removeRegionType(SequenceRegionVocabulary regionType);

	 void setRegionType(Set<SequenceRegionVocabulary> regionType);

 
}
