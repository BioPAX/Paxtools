/*
 * EvidenceProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
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
public class EvidenceProxy extends Level3ElementProxy implements Evidence, Serializable {
	public EvidenceProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Evidence.class;
	}

// XReferrable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = XrefProxy.class)
	@JoinTable(name="l3evidence_xref")
	public Set<Xref> getXref() {
		return ((Evidence)object).getXref();
	}

	public void addXref(Xref XREF) {
		((Evidence)object).addXref(XREF);
	}

	public void removeXref(Xref XREF) {
		((Evidence)object).removeXref(XREF);
	}

	public void setXref(Set<Xref> XREF) {
		((Evidence)object).setXref(XREF);
	}

// evidence

	// Property CONFIDENCE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = ScoreProxy.class)
	@JoinTable(name="l3evidence_confidence")
	public Set<Score> getConfidence() {
		return ((Evidence)object).getConfidence();
	}

	public void addConfidence(Score CONFIDENCE) {
		((Evidence)object).addConfidence(CONFIDENCE);
	}

	public void removeConfidence(Score CONFIDENCE) {
		((Evidence)object).removeConfidence(CONFIDENCE);
	}

	public void setConfidence(Set<Score> CONFIDENCE) {
		((Evidence)object).setConfidence(CONFIDENCE);
	}

	// Property EVIDENCE-CODE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceCodeVocabularyProxy.class)
	@JoinTable(name="l3evidence_evidence_code")
	public Set<EvidenceCodeVocabulary> getEvidenceCode() {
		return ((Evidence)object).getEvidenceCode();
	}

	public void addEvidenceCode(EvidenceCodeVocabulary EVIDENCE_CODE) {
		((Evidence)object).addEvidenceCode(EVIDENCE_CODE);
	}

	public void removeEvidenceCode(EvidenceCodeVocabulary EVIDENCE_CODE) {
		((Evidence)object).removeEvidenceCode(EVIDENCE_CODE);
	}

	public void setEvidenceCode(Set<EvidenceCodeVocabulary> EVIDENCE_CODE) {
		((Evidence)object).setEvidenceCode(EVIDENCE_CODE);
	}

	// Property EXPERIMENTAL-FORM

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = ExperimentalFormProxy.class)
	@JoinTable(name="l3evidence_experimental_form")
	public Set<ExperimentalForm> getExperimentalForm() {
		return ((Evidence)object).getExperimentalForm();
	}

	public void addExperimentalForm(ExperimentalForm EXPERIMENTAL_FORM) {
		((Evidence)object).addExperimentalForm(EXPERIMENTAL_FORM);
	}

	public void removeExperimentalForm(ExperimentalForm EXPERIMENTAL_FORM) {
		((Evidence)object).removeExperimentalForm(EXPERIMENTAL_FORM);
	}

	public void setExperimentalForm(Set<ExperimentalForm> EXPERIMENTAL_FORM) {
		((Evidence)object).setExperimentalForm(EXPERIMENTAL_FORM);
	}
}

