package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface Evidence extends UtilityClass, XReferrable
{


	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 *
	 * @return a set of scores representing confidence
	 */
	Set<Score> getConfidence();

	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 *
	 * @param confidence a new confidence measure to add
	 */
	void addConfidence(Score confidence);

	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 *
	 * @param confidence a confidence measure to be removed.
	 */
	void removeConfidence(Score confidence);



	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 *
	 * @return a set of evidence codes  for this evidence type.
	 */
	Set<EvidenceCodeVocabulary> getEvidenceCode();

	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 *
	 * @param evidenceCode a new evidence code  for this evidence.
	 */
	void addEvidenceCode(EvidenceCodeVocabulary evidenceCode);

	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 *
	 * @param evidenceCode to be removed
	 */
	void removeEvidenceCode(EvidenceCodeVocabulary evidenceCode);



	// Property EXPERIMENTAL-FORM

	Set<ExperimentalForm> getExperimentalForm();

	void addExperimentalForm(ExperimentalForm experimentalForm);

	void removeExperimentalForm(ExperimentalForm experimentalForm);

}
