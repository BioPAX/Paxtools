package org.biopax.paxtools.model.level3;

/**
 * A covalently modified feature on a sequence relevant to an interaction, such
 * as a post-translational modification. The difference between this class  and
 * bindingFeature is that this is covalent and bindingFeature is non-covalent.
 * Examples: A phosphorylation on a protein.
 */
public interface ModificationFeature extends EntityFeature
{
    SequenceModificationVocabulary getModificationType();

    void setModificationType(SequenceModificationVocabulary featureType);

}

