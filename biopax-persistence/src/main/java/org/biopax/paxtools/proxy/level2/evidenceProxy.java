/*
 * EvidenceProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for evidence
 */
@Entity(name="l2evidence")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class evidenceProxy extends utilityClassProxy implements evidence {
	public evidenceProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return evidence.class;
	}

	public void addCONFIDENCE(confidence CONFIDENCE) {
		((evidence)object).addCONFIDENCE(CONFIDENCE);
	}

	public void addEVIDENCE_CODE(openControlledVocabulary EVIDENCE_CODE) {
		((evidence)object).addEVIDENCE_CODE(EVIDENCE_CODE);
	}

	public void addEXPERIMENTAL_FORM(experimentalForm EXPERIMENTAL_FORM) {
		((evidence)object).addEXPERIMENTAL_FORM(EXPERIMENTAL_FORM);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=confidenceProxy.class)
	@JoinTable(name="l2evidence_confidence")
	public Set<confidence> getCONFIDENCE() {
		return ((evidence)object).getCONFIDENCE();
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=openControlledVocabularyProxy.class)
	@JoinTable(name="l2evidence_evidence_code")
	public Set<openControlledVocabulary> getEVIDENCE_CODE() {
		return ((evidence)object).getEVIDENCE_CODE();
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=experimentalFormProxy.class)
	@JoinTable(name="l2evidence_experimental_form")
	public Set<experimentalForm> getEXPERIMENTAL_FORM() {
		return ((evidence)object).getEXPERIMENTAL_FORM();
	}

	public void removeCONFIDENCE(confidence CONFIDENCE) {
		((evidence)object).removeCONFIDENCE(CONFIDENCE);
	}

	public void removeEVIDENCE_CODE(openControlledVocabulary EVIDENCE_CODE) {
		((evidence)object).removeEVIDENCE_CODE(EVIDENCE_CODE);
	}

	public void removeEXPERIMENTAL_FORM(experimentalForm EXPERIMENTAL_FORM) {
		((evidence)object).removeEXPERIMENTAL_FORM(EXPERIMENTAL_FORM);
	}

	public void setCONFIDENCE(Set<confidence> CONFIDENCE) {
		((evidence)object).setCONFIDENCE(CONFIDENCE);
	}

	public void setEVIDENCE_CODE(Set<openControlledVocabulary> EVIDENCE_CODE) {
		((evidence)object).setEVIDENCE_CODE(EVIDENCE_CODE);
	}

	public void setEXPERIMENTAL_FORM(Set<experimentalForm> EXPERIMENTAL_FORM) {
		((evidence)object).setEXPERIMENTAL_FORM(EXPERIMENTAL_FORM);
	}

// --------------------- XReferrable ---------------------

	public void addXREF(xref XREF) {
		((evidence)object).addXREF(XREF);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=xrefProxy.class)
	@JoinTable(name="l2evidence_xref")
	public Set<xref> getXREF() {
		return ((evidence)object).getXREF();
	}

	public void removeXREF(xref XREF) {
		((evidence)object).removeXREF(XREF);
	}

	public void setXREF(Set<xref> XREF) {
		((evidence)object).setXREF(XREF);
	}

//	@Transient
	public Set<unificationXref> findCommonUnifications(XReferrable that)
	{
		return ((XReferrable) object).findCommonUnifications(that);
	}

//	@Transient
	public Set<relationshipXref> findCommonRelationships(XReferrable that)
	{
		return ((XReferrable) object).findCommonRelationships(that);
	}

//	@Transient
	public Set<publicationXref> findCommonPublications(XReferrable that)
	{
		return ((XReferrable) object).findCommonPublications(that);
	}


}

