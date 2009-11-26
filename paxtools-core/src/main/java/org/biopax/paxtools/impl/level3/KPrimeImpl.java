package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.KPrime;

/**
 */
class KPrimeImpl extends L3ElementImpl implements KPrime
{

	private float ionicStrength = BioPAXElement.UNKNOWN_FLOAT;
	private float kPrime = BioPAXElement.UNKNOWN_FLOAT;
	private float ph = BioPAXElement.UNKNOWN_FLOAT;
	private float pMg = BioPAXElement.UNKNOWN_FLOAT;
	private float temperature = BioPAXElement.UNKNOWN_FLOAT;

	//
	// bioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends KPrime> getModelInterface()
	{
		return KPrime.class;
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final KPrime aKPrime = (KPrime) element;
		return
			(Float.compare(aKPrime.getIonicStrength(), ionicStrength) == 0) &&
				(Float.compare(aKPrime.getKPrime(), kPrime) == 0) &&
				(Float.compare(aKPrime.getPh(), ph) == 0) &&
				(Float.compare(aKPrime.getPMg(), pMg) == 0) &&
				(Float.compare(aKPrime.getTemperature(), temperature) == 0);
	}

	public int equivalenceCode()
	{
		int result = 29 + kPrime != +0.0f ? Float.floatToIntBits(kPrime) : 0;
		result = 29 * result + temperature != +0.0f ?
			Float.floatToIntBits(temperature) : 0;
		result = 29 * result + ionicStrength != +0.0f ?
			Float.floatToIntBits(ionicStrength) : 0;
		result = 29 * result + ph != +0.0f ? Float.floatToIntBits(ph) : 0;
		result = 29 * result + pMg != +0.0f ? Float.floatToIntBits(pMg) : 0;
		return result;
	}

	//
	// KPrime interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property IONIC-STRENGTH

	public float getIonicStrength()
	{
		return ionicStrength;
	}

	public void setIonicStrength(float ionicStrength)
	{
		this.ionicStrength = ionicStrength;
	}

	// Property K-PRIME

	public float getKPrime()
	{
		return kPrime;
	}

	public void setKPrime(float prime)
	{
		this.kPrime = prime;
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

	public void setPMg(float pMg)
	{
		this.pMg = pMg;
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
