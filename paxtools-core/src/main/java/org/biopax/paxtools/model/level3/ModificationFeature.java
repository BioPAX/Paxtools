package org.biopax.paxtools.model.level3;

/**
 * Definition: A covalently modified feature on a sequence, relevant to an interaction,
 * such as a post-translational modification. The difference between this class and BindingFeature is that this is
 * covalent and BindingFeature is non-covalent.
 * <p/>
 * Rationale: In Biology, identity of DNA, RNA and Protein entities are defined around a wildtype sequence. Covalent
 * modifications to this basal sequence are represented using modificaton features. Since small molecules are
 * identified based on their chemical structure, not sequence, a covalent modification to a small molecule would
 * result in a different molecule.
 * <p/>
 * Examples: A phosphorylation feature on a protein that enables the binding of an SH2 domain.
 * <p/>
 * Usagee: The added groups should be simple and stateless, such as phosphate or methyl groups and are captured
 * by the modificationType controlled vocabulary. In other cases, such as covalently linked proteins,
 * use CovalentBindingFeature instead. SmallMolecules can only have covalentBindingFeatures.
 */
public interface ModificationFeature extends EntityFeature
{
	/**
	 * @return Description and classification of the feature.
	 */
	SequenceModificationVocabulary getModificationType();

	/**
	 * @param featureType Description and classification of the feature.
	 */
	void setModificationType(SequenceModificationVocabulary featureType);

}

