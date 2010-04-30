package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.biopax.paxtools.model.level3.ExperimentalForm;
import org.biopax.paxtools.model.level3.Score;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@Entity
class EvidenceImpl extends XReferrableImpl implements Evidence
{

	private Set<ExperimentalForm> experimentalForm;
	private Set<Score> confidence;
	private Set<EvidenceCodeVocabulary> evidenceCode;

	/**
	 * Constructor.
	 */
	public EvidenceImpl()
	{
		this.confidence = new HashSet<Score>();
		this.evidenceCode = new HashSet<EvidenceCodeVocabulary>();
		this.experimentalForm = new HashSet<ExperimentalForm>();
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	@Transient
	public Class<? extends Evidence> getModelInterface()
	{
		return Evidence.class;
	}


	//
	// Evidence interface implementation
	//
	////////////////////////////////////////////////////////////////////////////


	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 *
	 * @return a set of scores representing confidence
	 */
	@OneToMany(targetEntity = ScoreImpl.class)
	public Set<Score> getConfidence()
	{
		return confidence;
	}

	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 * <p/>
	 * WARNING: This method should only be used for batch operations and with care. For regular
	 * manipulation use add/remove instead.
	 *
	 * @param confidence a set of scores representing confidence
	 */

	public void setConfidence(Set<Score> confidence)
	{
		this.confidence = confidence;
	}

	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 *
	 * @param confidence a new confidence measure to add
	 */
	public void addConfidence(Score confidence)
	{
		this.confidence.add(confidence);
	}

	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 *
	 * @param confidence a confidence measure to be removed.
	 */
	public void removeConfidence(Score confidence)
	{
		this.confidence.remove(confidence);
	}


	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 *
	 * @return a set of evidence codes  for this evidence type.
	 */
	@ManyToOne(targetEntity = EvidenceCodeVocabularyImpl.class)
	public Set<EvidenceCodeVocabulary> getEvidenceCode()
	{
		return evidenceCode;
	}

	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 * <p/>
	 * WARNING: This method should only be used for batch operations and with care. For regular
	 * manipulation use add/remove instead.
	 *
	 * @param evidenceCode a new set of evidence codes  for this evidence type.
	 */
	public void setEvidenceCode(Set<EvidenceCodeVocabulary> evidenceCode)
	{
		this.evidenceCode = evidenceCode;
	}

	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 *
	 * @param evidenceCode a new evidence code  for this evidence.
	 */
	public void addEvidenceCode(EvidenceCodeVocabulary evidenceCode)
	{
		this.evidenceCode.add(evidenceCode);
	}

	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 *
	 * @param evidenceCode to be removed
	 */
	public void removeEvidenceCode(EvidenceCodeVocabulary evidenceCode)
	{
		this.evidenceCode.remove(evidenceCode);
	}


	@OneToMany(targetEntity = ExperimentalFormImpl.class)
	public Set<ExperimentalForm> getExperimentalForm()
	{
		return experimentalForm;
	}

	public void setExperimentalForm(Set<ExperimentalForm> experimentalForm)
	{
		this.experimentalForm = experimentalForm;
	}

	public void addExperimentalForm(ExperimentalForm experimentalForm)
	{
		this.experimentalForm.add(experimentalForm);
	}

	public void removeExperimentalForm(ExperimentalForm experimentalForm)
	{
		this.experimentalForm.remove(experimentalForm);
	}

// ------------------------ INTERFACE METHODS ------------------------



}
