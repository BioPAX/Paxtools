package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.Set;

import static org.biopax.paxtools.util.SetEquivalenceChecker.hasEquivalentIntersection;

public class EvidenceImpl extends XReferrableImpl implements Evidence
{

	private Set<ExperimentalForm> experimentalForm;
	private Set<Score> confidence;
	private Set<EvidenceCodeVocabulary> evidenceCode;

	/**
	 * Constructor.
	 */
	public EvidenceImpl()
	{
		this.confidence = BPCollections.I.createSafeSet();
		this.evidenceCode = BPCollections.I.createSafeSet();
		this.experimentalForm = BPCollections.I.createSafeSet();
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

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
	public Set<Score> getConfidence()
	{
		return confidence;
	}

	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 *
	 * @param confidence a new confidence measure to add
	 */
	public void addConfidence(Score confidence)
	{
		if(confidence != null)
			this.confidence.add(confidence);
	}

	/**
	 * Confidence in the containing instance.  Usually a statistical measure.
	 *
	 * @param confidence a confidence measure to be removed.
	 */
	public void removeConfidence(Score confidence)
	{
		if(confidence != null)
			this.confidence.remove(confidence);
	}

	/**
	 * A pointer to a term in an external controlled vocabulary, such as the GO, PSI-MI or BioCyc
	 * evidence codes, that describes the nature of the support, such as 'traceable author statement'
	 * or 'yeast two-hybrid'.
	 *
	 * @return a set of evidence codes  for this evidence type.
	 */
	public Set<EvidenceCodeVocabulary> getEvidenceCode()
	{
		return evidenceCode;
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
		if(evidenceCode != null)
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
		if(evidenceCode != null)
			this.evidenceCode.remove(evidenceCode);
	}

	public Set<ExperimentalForm> getExperimentalForm()
	{
		return experimentalForm;
	}

	public void addExperimentalForm(ExperimentalForm experimentalForm)
	{
		if(experimentalForm != null)
			this.experimentalForm.add(experimentalForm);
	}

	public void removeExperimentalForm(ExperimentalForm experimentalForm)
	{
		if(experimentalForm != null)
			this.experimentalForm.remove(experimentalForm);
	}

// ------------------------ INTERFACE METHODS ------------------------

	/**
	 * Answers whether two Evidence objects are semantically equivalent.
	 * (Currently, it considers only member UnificationXrefs and EvidenceCodeVocabularies
	 * for comparison...)
	 * 
	 * TODO: review; compare ExperimentalForm and Confidence values; or - simply always return false!
	 */
	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element) {
		if(! (element instanceof Evidence) ) return false;
		Evidence that = (Evidence) element; // not null (guaranteed by here)
		boolean hasAllEquivEvidenceCodes = false;
		
		if(this.getEvidenceCode().isEmpty()) {
			if(that.getEvidenceCode().isEmpty()) {
				hasAllEquivEvidenceCodes = true;
			}
		} else {
			if(!that.getEvidenceCode().isEmpty()) {
				Set<EvidenceCodeVocabulary> shorter;
				Set<EvidenceCodeVocabulary> longer;
				if (this.getEvidenceCode().size() < that.getEvidenceCode().size())
				{
					shorter = this.getEvidenceCode();
					longer  = that.getEvidenceCode();
				} else {
					longer = this.getEvidenceCode();
					shorter  = that.getEvidenceCode();
				}
				
				/* each ECV in the 'shorter' set must find its equivalent
				 * in the 'longer' set; 
				 * otherwise two Evidence objects (this and that) are not equiv.
				 */
				hasAllEquivEvidenceCodes = true; // initial guess
				for(EvidenceCodeVocabulary secv : shorter) {
					boolean foundEquiv = false;
					for(EvidenceCodeVocabulary lecv : longer) {
						if(secv.isEquivalent(lecv)) {
							foundEquiv = true;
						}
					}
					if(!foundEquiv) {
						hasAllEquivEvidenceCodes = false;
						break;
					}
				}
			}
		}
		
		//consider publication xrefs!
		boolean hasCommonPublicationXref = hasEquivalentIntersection(
				new ClassFilterSet<>(getXref(), PublicationXref.class),
				new ClassFilterSet<>(that.getXref(), PublicationXref.class));
		
		return super.semanticallyEquivalent(element) && hasAllEquivEvidenceCodes && hasCommonPublicationXref;
	}

}
