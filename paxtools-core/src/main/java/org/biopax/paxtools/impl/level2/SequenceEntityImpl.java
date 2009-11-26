package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.level2.bioSource;

abstract class SequenceEntityImpl extends physicalEntityImpl
{
// ------------------------------ FIELDS ------------------------------

	private String SEQUENCE;
	private bioSource ORGANISM;

// -------------------------- OTHER METHODS --------------------------

	public bioSource getORGANISM()
	{
		return ORGANISM;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	public String getSEQUENCE()
	{
		return SEQUENCE;
	}

	/**
	 * ORGANISM of origin for this sequence entity
	 *
	 * @param ORGANISM- set null for cannonical
	 */
	public void setORGANISM(bioSource ORGANISM)
	{
		this.ORGANISM = ORGANISM;
	}

	public void setSEQUENCE(String SEQUENCE)
	{
		this.SEQUENCE = SEQUENCE;
	}
}
