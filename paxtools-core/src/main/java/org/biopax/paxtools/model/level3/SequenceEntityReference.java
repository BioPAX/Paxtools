package org.biopax.paxtools.model.level3;

/**
 * Tagger interface for protein, dna and rna entities
 */
public interface SequenceEntityReference extends EntityReference
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

	
	String getSequence();

	void setSequence(String sequence);

	

}
