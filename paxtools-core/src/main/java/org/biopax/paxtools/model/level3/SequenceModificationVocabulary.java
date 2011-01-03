package org.biopax.paxtools.model.level3;


/**
 * <b>Definition</b>: A term that describes the covalent modifications to an amino
 * acid or nucleic acid chain. For proteins this is reference to the PSI
 * Molecular Interaction ontology (MI) of covalent sequence modifications.
 * For nucleic acids, there is currently no recommended standard CV.
 *<p>
 * <b>Usage</b>: Whenever possible the most specific term in the ontology should be used.
 * For example PSI-MI term o-phospho-serine should be used instead of the
 * parent "phosphorylated residue" when annotating a phosphorylation at a
 * known serine amino acid.
 * @see <a href="http://www.psidev.info">PSI Dev</a>
 * @see <a href="http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MI&amp;termId=MI%3A0252&amp;termName=biological%20feature">PSI Biological Feature</a>
 * @see <a href="http://rna-mdb.cas.albany.edu/RNAmods/"> Candidate RNA modification CV</a>
 */
public interface SequenceModificationVocabulary extends ControlledVocabulary
{
}
