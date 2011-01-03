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

	/**
	 * Polymer sequence in uppercase letters. For DNA, usually A,C,G,T letters representing the
	 * nucleosides of adenine, cytosine, guanine and thymine, respectively; for RNA, usually A, C, U,
	 * G; for protein, usually the letters corresponding to the 20 letter IUPAC amino acid code.
	 */
	String getSequence();

	/**
	 * Polymer sequence in uppercase letters. For DNA, usually A,C,G,T letters representing the
	 * nucleosides of adenine, cytosine, guanine and thymine, respectively; for RNA, usually A, C, U,
	 * G; for protein, usually the letters corresponding to the 20 letter IUPAC amino acid code.
	 * @param sequence Polymer sequence in uppercase letters.
	 */
	void setSequence(String sequence);


}
