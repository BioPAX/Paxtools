package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.level2.*;

import java.util.HashSet;
import java.util.Set;


abstract class entityImpl extends BioPAXLevel2ElementImpl implements entity
{
// ------------------------------ FIELDS ------------------------------

	private HashSet<interaction> PARTICIPANTof;
	/**
	 * This Set keeps statements describing the availability of this data (e.g. a
	 * copyright statement).
	 */
	private Set<String> AVAILABILITY;

	/**
	 * This Set keeps statements describing the data sources for this data.
	 */
	private Set<dataSource> DATA_SOURCE;

	/**
	 * Name of this entity, optional
	 */
	private String NAME;

	/**
	 * Short name of this entity, optional
	 */
	private String SHORT_NAME;

	/**
	 * This Set keeps optional synonyms
	 */
	private Set<String> SYNONYMS;

	/**
	 * THis Set keeps optional XREFs
	 */
	private final ReferenceHelper referenceHelper;

// --------------------------- CONSTRUCTORS ---------------------------

	public entityImpl()
	{
		this.AVAILABILITY = new HashSet<String>();
		this.DATA_SOURCE = new HashSet<dataSource>();
		this.SYNONYMS = new HashSet<String>();
		this.referenceHelper = new ReferenceHelper(this);
		this.PARTICIPANTof = new HashSet<interaction>();
	}

// ------------------------ INTERFACE METHODS ------------------------


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

// --------------------- Interface entity ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<String> getAVAILABILITY()
	{
		return AVAILABILITY;
	}

	public void setAVAILABILITY(Set<String> AVAILABILITY)
	{
		this.AVAILABILITY = AVAILABILITY;
	}

	public void addAVAILABILITY(String AVAILABILITY_TEXT)
	{
		AVAILABILITY.add(AVAILABILITY_TEXT);
	}

	public void removeAVAILABILITY(String AVAILABILITY_TEXT)
	{
		AVAILABILITY.remove(AVAILABILITY_TEXT);
	}

	public Set<dataSource> getDATA_SOURCE()
	{
		return DATA_SOURCE;
	}

	public void setDATA_SOURCE(Set<dataSource> DATA_SOURCE)
	{
		this.DATA_SOURCE = DATA_SOURCE;
	}

	public void addDATA_SOURCE(dataSource DATA_SOURCE_INST)
	{
		DATA_SOURCE.add(DATA_SOURCE_INST);
	}

	public void removeDATA_SOURCE(dataSource DATA_SOURCE_INST)
	{
		DATA_SOURCE.remove(DATA_SOURCE_INST);
	}

	public String getNAME()
	{
		return NAME;
	}

	public void setNAME(String NAME)
	{
		this.NAME = NAME;
	}

	public String getSHORT_NAME()
	{
		return SHORT_NAME;
	}

	public void setSHORT_NAME(String SHORT_NAME)
	{
		this.SHORT_NAME = SHORT_NAME;
	}

	public Set<String> getSYNONYMS()
	{
		return SYNONYMS;
	}

	public void setSYNONYMS(Set<String> SYNONYMS)
	{
		this.SYNONYMS = SYNONYMS;
	}

	public void addSYNONYMS(String SYNONYMS_TEXT)
	{
		SYNONYMS.add(SYNONYMS_TEXT);
	}

	public void removeSYNONYMS(String SYNONYMS_TEXT)
	{
		SYNONYMS.remove(SYNONYMS_TEXT);
	}

// --------------------- Interface InteractionParticipant ---------------------


	public Set<interaction> isPARTICIPANTSof()
	{
		return PARTICIPANTof;
	}

}
