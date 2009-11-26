package org.biopax.paxtools.model.level3;


/**
 * A continuant that encodes information that can be inherited through replication. This is a
 * generalization of the prokaryotic and eukaryotic notion of a gene. This is used only for genetic
 * interactions. Gene expression regulation makes use of DNA and RNA physical entities.
 */
public interface Gene extends Entity
{

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
