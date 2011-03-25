package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.util.SetEquivalanceChecker;

import java.util.HashSet;
import java.util.Set;

/**
 */
class sequenceFeatureImpl extends BioPAXLevel2ElementImpl
	implements sequenceFeature
{
// ------------------------------ FIELDS ------------------------------

	private openControlledVocabulary FEATURE_TYPE;
	private Set<sequenceLocation> FEATURE_LOCATION;
	private String NAME;
	private Set<String> SYNONYMS;
	private String SHORT_NAME;
	private final ReferenceHelper referenceHelper;

// --------------------------- CONSTRUCTORS ---------------------------

	public sequenceFeatureImpl()
	{
		this.FEATURE_LOCATION = new HashSet<sequenceLocation>();
		this.SYNONYMS = new HashSet<String>();
		this.referenceHelper = new ReferenceHelper(this);
	}

// ------------------------ CANONICAL METHODS ------------------------

	public int equivalenceCode()
	{
		int result = 29 + (FEATURE_TYPE != null ? FEATURE_TYPE.hashCode() : 0);
		result = 29 * result +
			(FEATURE_LOCATION != null ? FEATURE_LOCATION.hashCode() : 0);
		return result;
	}

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface BioPAXElement ---------------------


	protected boolean semanticallyEquivalent(BioPAXElement o)
	{
		final sequenceFeature that = (sequenceFeature) o;
		return
			SetEquivalanceChecker.isEquivalent(this.FEATURE_LOCATION, that.getFEATURE_LOCATION())
				&&
				(FEATURE_TYPE != null ? FEATURE_TYPE.isEquivalent(that.getFEATURE_TYPE()) :
				that.getFEATURE_TYPE() == null);
	}

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return sequenceFeature.class;
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

// --------------------- Interface sequenceFeature ---------------------


	public openControlledVocabulary getFEATURE_TYPE()
	{
		return FEATURE_TYPE;
	}

	public void setFEATURE_TYPE(openControlledVocabulary FEATURE_TYPE)
	{
		this.FEATURE_TYPE = FEATURE_TYPE;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<sequenceLocation> getFEATURE_LOCATION()
	{
		return FEATURE_LOCATION;
	}

	public void setFEATURE_LOCATION(Set<sequenceLocation> FEATURE_LOCATION)
	{
		this.FEATURE_LOCATION = FEATURE_LOCATION;
	}

	public void addFEATURE_LOCATION(sequenceLocation FEATURE_LOCATION)
	{
		this.FEATURE_LOCATION.add(FEATURE_LOCATION);
	}

	public void removeFEATURE_LOCATION(sequenceLocation FEATURE_LOCATION)
	{
		this.FEATURE_LOCATION.remove(FEATURE_LOCATION);
	}

	public String getNAME()
	{
		return NAME;
	}

	public void setNAME(String NAME)
	{
		this.NAME = NAME;
	}

	public Set<String> getSYNONYMS()
	{
		return SYNONYMS;
	}

	public void setSYNONYMS(Set<String> SYNONYMS)
	{
		this.SYNONYMS = SYNONYMS;
	}

	public void addSYNONYMS(String SYNONYMS)
	{
		this.SYNONYMS.add(SYNONYMS);
	}

	public void removeSYNONYMS(String SYNONYMS)
	{
		this.SYNONYMS.remove(SYNONYMS);
	}

	public String getSHORT_NAME()
	{
		return SHORT_NAME;
	}

	public void setSHORT_NAME(String SHORT_NAME)
	{
		this.SHORT_NAME = SHORT_NAME;
	}
}
