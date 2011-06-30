package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.DeltaG;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

import static java.lang.Float.compare;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class DeltaGImpl extends L3ElementImpl implements DeltaG
{

	private float deltaGPrime0 = UNKNOWN_FLOAT;
	private float temperature = UNKNOWN_FLOAT;
	private float ionicStrength = UNKNOWN_FLOAT;
	private float ph = UNKNOWN_FLOAT;
	private float pMg = UNKNOWN_FLOAT;

	
	public DeltaGImpl() {
	}
	
	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
	@Transient
	public Class<? extends DeltaG> getModelInterface()
	{
		return DeltaG.class;
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final DeltaG that = (DeltaG) element;

		return
			compare(that.getDeltaGPrime0(), deltaGPrime0) == 0
				&& (compare(that.getIonicStrength(), ionicStrength) == 0)
				&& (compare(that.getPh(), ph) == 0)
				&& (compare(that.getPMg(), pMg) == 0)
				&& (compare(that.getTemperature(), temperature) == 0);
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

	public float getDeltaGPrime0()
	{
		return deltaGPrime0;
	}

	public void setDeltaGPrime0(float deltaGPrime0)
	{
		this.deltaGPrime0 = deltaGPrime0;
	}

	public float getIonicStrength()
	{
		return ionicStrength;
	}

	public void setIonicStrength(float ionicStrength)
	{
		this.ionicStrength = ionicStrength;
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

	public void setPMg(float pmg)
	{
		this.pMg = pmg;
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
