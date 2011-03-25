package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;

import java.util.HashSet;
import java.util.Set;

/**
 */
class evidenceImpl extends BioPAXLevel2ElementImpl implements evidence
{
// ------------------------------ FIELDS ------------------------------

	private Set<experimentalForm> EXPERIMENTAL_FORM;
	private Set<confidence> CONFIDENCE;
	private Set<openControlledVocabulary> EVIDENCE_CODE;
	private final ReferenceHelper referenceHelper;

// --------------------------- CONSTRUCTORS ---------------------------

	public evidenceImpl()
	{
		this.CONFIDENCE = new HashSet<confidence>();
		this.EVIDENCE_CODE = new HashSet<openControlledVocabulary>();
		this.EXPERIMENTAL_FORM = new HashSet<experimentalForm>();
		this.referenceHelper = new ReferenceHelper(this);
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return evidence.class;
	}

// --------------------- Interface XReferrable ---------------------


	public Set<xref> getXREF()
	{
		return referenceHelper.getXREF();
	}

	public void setXREF(Set<xref> XREF)
	{
		referenceHelper.setXREF(XREF);
	}

	public void addXREF(xref XREF)
	{
		referenceHelper.addXREF(XREF);
	}

	public void removeXREF(xref XREF)
	{
		referenceHelper.removeXREF(XREF);
	}
	public Set<unificationXref> findCommonUnifications(XReferrable that)
	{
		return referenceHelper.findCommonUnifications(that);
	}

	public Set<relationshipXref> findCommonRelationships(XReferrable that)
	{
		return referenceHelper.findCommonRelationships(that);
	}

	public Set<publicationXref> findCommonPublications(XReferrable that)
	{
		return referenceHelper.findCommonPublications(that);
	}

// --------------------- Interface evidence ---------------------


	public Set<experimentalForm> getEXPERIMENTAL_FORM()
	{
		return EXPERIMENTAL_FORM;
	}

	public void setEXPERIMENTAL_FORM(Set<experimentalForm> EXPERIMENTAL_FORM)
	{
		this.EXPERIMENTAL_FORM = EXPERIMENTAL_FORM;
	}

	public void addEXPERIMENTAL_FORM(experimentalForm EXPERIMENTAL_FORM)
	{
		this.EXPERIMENTAL_FORM.add(EXPERIMENTAL_FORM);
	}

	public void removeEXPERIMENTAL_FORM(experimentalForm EXPERIMENTAL_FORM)
	{
		this.EXPERIMENTAL_FORM.remove(EXPERIMENTAL_FORM);
	}

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<confidence> getCONFIDENCE()
	{
		return CONFIDENCE;
	}

	public void setCONFIDENCE(Set<confidence> CONFIDENCE)
	{
		this.CONFIDENCE = CONFIDENCE;
	}

	public void addCONFIDENCE(confidence CONFIDENCE)
	{
		this.CONFIDENCE.add(CONFIDENCE);
	}

	public void removeCONFIDENCE(confidence CONFIDENCE)
	{
		this.CONFIDENCE.remove(CONFIDENCE);
	}

	public Set<openControlledVocabulary> getEVIDENCE_CODE()
	{
		return EVIDENCE_CODE;
	}

	public void setEVIDENCE_CODE(Set<openControlledVocabulary> EVIDENCE_CODE)
	{
		this.EVIDENCE_CODE = EVIDENCE_CODE;
	}

	public void addEVIDENCE_CODE(openControlledVocabulary EVIDENCE_CODE)
	{
		this.EVIDENCE_CODE.add(EVIDENCE_CODE);
	}

	public void removeEVIDENCE_CODE(openControlledVocabulary EVIDENCE_CODE)
	{
		this.EVIDENCE_CODE.remove(EVIDENCE_CODE);
	}

// --------------------- CANONICAL METHODS ------------------------

	public String toString()
	{
		String s = "";

		for (experimentalForm form : EXPERIMENTAL_FORM)
		{
			if (s.length() > 0) s += ", ";
			s += form;
		}

		if (s.length() > 0) s += " : ";

		for (openControlledVocabulary code : EVIDENCE_CODE)
		{
			if (s.length() > 0 && s.lastIndexOf(" ") < s.length() - 1) s += ", ";
			s += code;
		}

		if (s.length() > 0) s += " : ";

		for (confidence conf : CONFIDENCE)
		{
			if (s.length() > 0 && s.lastIndexOf(" ") < s.length() - 1) s += ", ";
			s += conf;
		}

		return s;
	}
}
