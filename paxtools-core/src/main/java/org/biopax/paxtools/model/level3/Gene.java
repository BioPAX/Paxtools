package org.biopax.paxtools.model.level3;


/**
 * <b>Definition</b>: A continuant that encodes information that can be inherited through replication.
 * <p/>
 * <b>Rationale</b>: Gene is an abstract continuant that can be best described as a "schema", a common conception commonly used by biologists to demark a component within genome. In BioPAX, Gene is considered a generalization over eukaryotic and prokaryotic genes and is used only in genetic interactions.  Gene is often confused with DNA and RNA fragments, however, these are considered the physical encoding of a gene.  N.B. Gene expression regulation makes use of DNA and RNA physical entities and not this class.
 * <p/>
 * <b>Usage</b>: Gene should only be used for describing GeneticInteractions.
 */
public interface Gene extends Entity {

    /**
     * An organism, e.g. 'Homo sapiens'. This is the organism that the entity is found in. Pathways may
     * not have an organism associated with them, for instance, reference pathways from KEGG.
     * Sequence-based entities (DNA, protein, RNA) may contain an xref to a sequence database that
     * contains organism information, in which case the information should be consistent with the value
     * for ORGANISM.
     *
     * @return the organism for this gene.
     */
    BioSource getOrganism();

    /**
     * An organism, e.g. 'Homo sapiens'. This is the organism that the entity is found in. Pathways may
     * not have an organism associated with them, for instance, reference pathways from KEGG.
     * Sequence-based entities (DNA, protein, RNA) may contain an xref to a sequence database that
     * contains organism information, in which case the information should be consistent with the value
     * for ORGANISM.
     *
     * @param source new organism for this gene
     */
    void setOrganism(BioSource source);
}
