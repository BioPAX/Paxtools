package org.biopax.paxtools.model.level3;
import java.util.Set;

/**
 */
public interface NucleicAcidRegionReference<T extends NucleicAcidRegionReference>
		extends SequenceEntityReference
{
	Set<T> getSubRegion();

	void addSubRegion(T regionReference);

	void removeSubRegion(T regionReference);


	SequenceLocation getAbsoluteRegion();

	void setAbsoluteRegion(SequenceLocation absoluteRegion);


	Set<SequenceRegionVocabulary> getRegionType();

	void addRegionType(SequenceRegionVocabulary regionType);

	void removeRegionType(SequenceRegionVocabulary regionType);



	NucleicAcidReference getContainerEntityReference();

	void setContainerEntityReference(NucleicAcidReference containerEntityReference);
}
