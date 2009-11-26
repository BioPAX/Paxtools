package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.DeltaG;

class DeltaGImpl extends L3ElementImpl implements
        org.biopax.paxtools.model.level3.DeltaG
{

	private float deltaGPrime0 = BioPAXElement.UNKNOWN_FLOAT;
	private float temperature = BioPAXElement.UNKNOWN_FLOAT;
	private float ionicStrength = BioPAXElement.UNKNOWN_FLOAT;
	private float ph = BioPAXElement.UNKNOWN_FLOAT;
	private float pMg = BioPAXElement.UNKNOWN_FLOAT;

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends DeltaG> getModelInterface()
	{
		return DeltaG.class;
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final DeltaG that = (DeltaG) element;

		return
			(Float.compare(that.getDeltaGPrime0(), deltaGPrime0) == 0)
				&& (Float.compare(that.getIonicStrength(), ionicStrength) == 0)
				&& (Float.compare(that.getPh(), ph) == 0)
				&& (Float.compare(that.getPMg(), pMg) == 0)
				&& (Float.compare(that.getTemperature(), temperature) == 0);
	}

	public int equivalenceCode()
	{
		int result = 29 + deltaGPrime0 != +0.0f ?
			Float.floatToIntBits(deltaGPrime0) : 0;
		result = 29 * result + temperature != +0.0f ?
			Float.floatToIntBits(temperature) : 0;
		result = 29 * result + ionicStrength != +0.0f ?
			Float.floatToIntBits(ionicStrength) : 0;
		result = 29 * result + ph != +0.0f ? Float.floatToIntBits(ph) : 0;
		result = 29 * result + pMg != +0.0f ? Float.floatToIntBits(pMg) : 0;
		return result;
	}

	//
	// deltaGprimeO interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property DELTA-G-PRIME-O

	public float getDeltaGPrime0()
	{
		return deltaGPrime0;
	}

	public void setDeltaGPrime0(float deltaGPrime0)
	{
		this.deltaGPrime0 = deltaGPrime0;
	}

	// Property IONIC-STRENGTH

	public float getIonicStrength()
	{
		return ionicStrength;
	}

	public void setIonicStrength(float ionicStrength)
	{
		this.ionicStrength = ionicStrength;
	}

	// Property ph

	public float getPh()
	{
		return ph;
	}

	public void setPh(float ph)
	{
		this.ph = ph;
	}

	// Property pMg

	public float getPMg()
	{
		return pMg;
	}

	public void setPMg(float pmg)
	{
		this.pMg = pmg;
	}

	// Property temperature

	public float getTemperature()
	{
		return temperature;
	}

	public void setTemperature(float temperature)
	{
		this.temperature = temperature;
	}
}
