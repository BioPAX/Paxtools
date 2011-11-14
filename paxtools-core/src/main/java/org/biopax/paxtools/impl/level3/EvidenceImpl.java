package org.biopax.paxtools.impl.level3;


import static org.biopax.paxtools.util.SetEquivalanceChecker.isEquivalentIntersection;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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
	@OneToMany(targetEntity = ScoreImpl.class)//, cascade={CascadeType.ALL})
	@JoinTable(name="confidence")		
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
	@ManyToMany(targetEntity = EvidenceCodeVocabularyImpl.class)
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


	@OneToMany(targetEntity = ExperimentalFormImpl.class)//, cascade={CascadeType.ALL})
	@JoinTable(name="experimentalForm")	
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
	 * TODO: review; add comparing ExperimentalForm and Confidence values...
	 * 
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
		boolean hasCommonPublicationXref = isEquivalentIntersection(
				new ClassFilterSet<Xref,PublicationXref>(getXref(), PublicationXref.class),
				new ClassFilterSet<Xref,PublicationXref>(that.getXref(), PublicationXref.class));
		
		return super.semanticallyEquivalent(element) && hasAllEquivEvidenceCodes && hasAllEquivEvidenceCodes;
	}

}
