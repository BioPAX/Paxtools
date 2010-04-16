/*
 * EvidenceProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for evidence
 */
@Entity(name="l3evidence")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class EvidenceProxy extends XReferrableProxy<Evidence> implements Evidence, Serializable {



// evidence

	// Property CONFIDENCE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = ScoreProxy.class)
	@JoinTable(name="l3evidence_confidence")
	public Set<Score> getConfidence() {
		return object.getConfidence();
	}

	public void addConfidence(Score CONFIDENCE) {
		object.addConfidence(CONFIDENCE);
	}

	public void removeConfidence(Score CONFIDENCE) {
		object.removeConfidence(CONFIDENCE);
	}

	public void setConfidence(Set<Score> CONFIDENCE) {
		object.setConfidence(CONFIDENCE);
	}

	// Property EVIDENCE-CODE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceCodeVocabularyProxy.class)
	@JoinTable(name="l3evidence_evidence_code")
	public Set<EvidenceCodeVocabulary> getEvidenceCode() {
		return object.getEvidenceCode();
	}

	public void addEvidenceCode(EvidenceCodeVocabulary EVIDENCE_CODE) {
		object.addEvidenceCode(EVIDENCE_CODE);
	}

	public void removeEvidenceCode(EvidenceCodeVocabulary EVIDENCE_CODE) {
		object.removeEvidenceCode(EVIDENCE_CODE);
	}

	public void setEvidenceCode(Set<EvidenceCodeVocabulary> EVIDENCE_CODE) {
		object.setEvidenceCode(EVIDENCE_CODE);
	}

	// Property EXPERIMENTAL-FORM

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = ExperimentalFormProxy.class)
	@JoinTable(name="l3evidence_experimental_form")
	public Set<ExperimentalForm> getExperimentalForm() {
		return object.getExperimentalForm();
	}

	public void addExperimentalForm(ExperimentalForm EXPERIMENTAL_FORM) {
		object.addExperimentalForm(EXPERIMENTAL_FORM);
	}

	public void removeExperimentalForm(ExperimentalForm EXPERIMENTAL_FORM) {
		object.removeExperimentalForm(EXPERIMENTAL_FORM);
	}

	public void setExperimentalForm(Set<ExperimentalForm> EXPERIMENTAL_FORM) {
		object.setExperimentalForm(EXPERIMENTAL_FORM);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Evidence.class;
	}
}

