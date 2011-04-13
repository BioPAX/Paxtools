package org.biopax.paxtools.model.level3;

import java.util.Set;


/**
 * Definition: The support for a particular assertion, such as the existence of an interaction or pathway.
 * <p/>
 * Usage: At least one of confidence, evidenceCode, or experimentalForm must be instantiated when creating an
 * evidence instance. XREF may reference a publication describing the experimental evidence using a publicationXref
 * or may store a description of the experiment in an experimental description database using a unificationXref (if
 * the referenced experiment is the same) or relationshipXref (if it is not identical,
 * but similar in some way e.g. similar in protocol). Evidence is meant to provide more information than just an xref
 * to the source paper.
 * <p/>
 * Examples: A description of a molecular binding assay that was used to detect a protein-protein interaction.
 */
public interface Evidence extends UtilityClass, XReferrable
{


	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 * @return a set of scores representing confidence
	 */
	Set<Score> getConfidence();

	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 * @param confidence a new confidence measure to add
	 */
	void addConfidence(Score confidence);

	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 * @param confidence a confidence measure to be removed.
	 */
	void removeConfidence(Score confidence);


	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 * Contents of this set should not be modified. Use add/remove methods instead.
	 * @return a set of evidence codes  for this evidence type.
	 */
	Set<EvidenceCodeVocabulary> getEvidenceCode();

	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 * @param evidenceCode a new evidence code  for this evidence.
	 */
	void addEvidenceCode(EvidenceCodeVocabulary evidenceCode);

	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 * @param evidenceCode to be removed
	 */
	void removeEvidenceCode(EvidenceCodeVocabulary evidenceCode);


	/**
	 * Contents of this set should not be modified. Use add/remove methods instead.
	 * @return The experimental forms associated with an evidence instance.
	 */
	Set<ExperimentalForm> getExperimentalForm();

	/**
	 * Adds an experimental form.
	 * @param experimentalForm associated with an evidence instance.
	 */
	void addExperimentalForm(ExperimentalForm experimentalForm);

	/**
	 * Removes an experimental form.
	 * @param experimentalForm associated with an evidence instance.
	 */
	void removeExperimentalForm(ExperimentalForm experimentalForm);

}
