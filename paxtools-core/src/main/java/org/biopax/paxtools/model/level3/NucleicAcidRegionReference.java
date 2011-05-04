package org.biopax.paxtools.model.level3;
import java.util.Set;

/**
 */
public interface NucleicAcidRegionReference
		extends NucleicAcidReference
{


	public Set<NucleicAcidReference> getSubRegionOf();

	SequenceLocation getAbsoluteRegion();

	void setAbsoluteRegion(SequenceLocation absoluteRegion);


	Set<SequenceRegionVocabulary> getRegionType();

	void addRegionType(SequenceRegionVocabulary regionType);

	void removeRegionType(SequenceRegionVocabulary regionType);



	NucleicAcidReference getContainerEntityReference();

	void setContainerEntityReference(NucleicAcidReference containerEntityReference);
}
