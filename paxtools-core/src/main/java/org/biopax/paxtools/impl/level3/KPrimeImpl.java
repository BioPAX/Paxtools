package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.KPrime;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

import static java.lang.Float.compare;
import static java.lang.Float.floatToIntBits;

/**
 */
@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class KPrimeImpl extends L3ElementImpl implements KPrime
{

	private float ionicStrength = UNKNOWN_FLOAT;
	private float kPrime = UNKNOWN_FLOAT;
	private float ph = UNKNOWN_FLOAT;
	private float pMg = UNKNOWN_FLOAT;
	private float temperature = UNKNOWN_FLOAT;

	public KPrimeImpl() {
	}
	
	//
	// bioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	@Transient
	public Class<? extends KPrime> getModelInterface()
	{
		return KPrime.class;
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final KPrime aKPrime = (KPrime) element;
		return
			compare(aKPrime.getIonicStrength(), ionicStrength) == 0 &&
			compare(aKPrime.getKPrime(), kPrime) == 0 &&
			compare(aKPrime.getPh(), ph) == 0 &&
			compare(aKPrime.getPMg(), pMg) == 0 &&
			compare(aKPrime.getTemperature(), temperature) == 0;
	}

	public int equivalenceCode()
	{
		int result = 29 + kPrime != +0.0f ? floatToIntBits(kPrime) : 0;
		result = 29 * result + temperature != +0.0f ?
			floatToIntBits(temperature) : 0;
		result = 29 * result + ionicStrength != +0.0f ?
			floatToIntBits(ionicStrength) : 0;
		result = 29 * result + ph != +0.0f ? floatToIntBits(ph) : 0;
		result = 29 * result + pMg != +0.0f ? floatToIntBits(pMg) : 0;
		return result;
	}

	//
	// KPrime interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	
	public float getIonicStrength()
	{
		return ionicStrength;
	}

	public void setIonicStrength(float ionicStrength)
	{
		this.ionicStrength = ionicStrength;
	}

	public float getKPrime()
	{
		return kPrime;
	}

	public void setKPrime(float prime)
	{
		this.kPrime = prime;
	}

	public float getPh()
	{
		return ph;
	}

	public void setPh(float ph)
	{
		this.ph = ph;
	}

	public float getPMg()
	{
		return pMg;
	}

	public void setPMg(float pMg)
	{
		this.pMg = pMg;
	}

	public float getTemperature()
	{
		return temperature;
	}

	public void setTemperature(float temperature)
	{
		this.temperature = temperature;
	}
}
