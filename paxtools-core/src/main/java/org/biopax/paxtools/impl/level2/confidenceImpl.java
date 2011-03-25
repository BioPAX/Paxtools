package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;

import java.util.Set;

/**
 */
class confidenceImpl extends BioPAXLevel2ElementImpl implements confidence
{
// ------------------------------ FIELDS ------------------------------

	private String CONFIDENCE_VALUE;
	private final ReferenceHelper referenceHelper;

// --------------------------- CONSTRUCTORS ---------------------------

	public confidenceImpl()
	{
		this.referenceHelper = new ReferenceHelper(this);
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return confidence.class;
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
	

// --------------------- Interface confidence ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public String getCONFIDENCE_VALUE()
	{
		return CONFIDENCE_VALUE;
	}

	public void setCONFIDENCE_VALUE(String CONFIDENCE_VALUE)
	{
		this.CONFIDENCE_VALUE = CONFIDENCE_VALUE;
	}

// --------------------- CANONOCAL METHODS ------------------------

	public String toString()
	{
		return CONFIDENCE_VALUE;
	}
}
