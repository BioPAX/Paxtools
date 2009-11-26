package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.kPrime;

/**
 */
class kPrimeImpl extends BioPAXLevel2ElementImpl implements kPrime
{
// ------------------------------ FIELDS ------------------------------

	private float K_PRIME = BioPAXElement.UNKNOWN_FLOAT;
	private float TEMPERATURE = BioPAXElement.UNKNOWN_FLOAT;
	private float IONIC_STRENGTH = BioPAXElement.UNKNOWN_FLOAT;
	private float PH = BioPAXElement.UNKNOWN_FLOAT;
	private float PMG = BioPAXElement.UNKNOWN_FLOAT;

// ------------------------ CANONICAL METHODS ------------------------

	public int equivalenceCode()
	{
		int result = 29 + K_PRIME != +0.0f ? Float.floatToIntBits(K_PRIME) : 0;
		result = 29 * result + TEMPERATURE != +0.0f ?
			Float.floatToIntBits(TEMPERATURE) : 0;
		result = 29 * result + IONIC_STRENGTH != +0.0f ?
			Float.floatToIntBits(IONIC_STRENGTH) : 0;
		result = 29 * result + PH != +0.0f ? Float.floatToIntBits(PH) : 0;
		result = 29 * result + PMG != +0.0f ? Float.floatToIntBits(PMG) : 0;
		return result;
	}


// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final kPrime aKPrime = (kPrime) element;
		return
			(Float.compare(aKPrime.getIONIC_STRENGTH(), IONIC_STRENGTH) == 0) &&
				(Float.compare(aKPrime.getK_PRIME(), K_PRIME) == 0) &&
				(Float.compare(aKPrime.getPH(), PH) == 0) &&
				(Float.compare(aKPrime.getPMG(), PMG) == 0) &&
				(Float.compare(aKPrime.getTEMPERATURE(), TEMPERATURE) == 0);
	}

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return kPrime.class;
	}

// --------------------- Interface kPrime ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public float getK_PRIME()
	{
		return K_PRIME;
	}

	public void setK_PRIME(float K_PRIME)
	{
		this.K_PRIME = K_PRIME;
	}

	public float getTEMPERATURE()
	{
		return TEMPERATURE;
	}

	public void setTEMPERATURE(float TEMPERATURE)
	{
		this.TEMPERATURE = TEMPERATURE;
	}

	public float getIONIC_STRENGTH()
	{
		return IONIC_STRENGTH;
	}

	public void setIONIC_STRENGTH(float IONIC_STRENGTH)
	{
		this.IONIC_STRENGTH = IONIC_STRENGTH;
	}

	public float getPH()
	{
		return PH;
	}

	public void setPH(float PH)
	{
		this.PH = PH;
	}

	public float getPMG()
	{
		return PMG;
	}

	public void setPMG(float PMG)
	{
		this.PMG = PMG;
	}
}
