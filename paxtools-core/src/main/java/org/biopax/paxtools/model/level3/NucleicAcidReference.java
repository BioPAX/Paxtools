package org.biopax.paxtools.model.level3;


import java.util.Set;

/**
 * Role interface for NucleicAcidReferences, namely DNA and RNA. They share the common behaviour of having "regions"
 * on them.
 * @see NucleicAcidRegionReference
 */
public interface NucleicAcidReference extends SequenceEntityReference
{
   Set<NucleicAcidRegionReference> getSubRegion();

	void addSubRegion(NucleicAcidRegionReference regionReference);

	void removeSubRegion(NucleicAcidRegionReference regionReference);
}
