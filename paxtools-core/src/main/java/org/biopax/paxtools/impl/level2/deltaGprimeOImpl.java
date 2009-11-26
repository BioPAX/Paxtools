package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.deltaGprimeO;

/**
 */
class deltaGprimeOImpl extends BioPAXLevel2ElementImpl implements deltaGprimeO
{
// ------------------------------ FIELDS ------------------------------

	private float DELTA_G_PRIME_O = BioPAXElement.UNKNOWN_FLOAT;
	private float TEMPERATURE = BioPAXElement.UNKNOWN_FLOAT;
	private float IONIC_STRENGTH = BioPAXElement.UNKNOWN_FLOAT;
	private float PH = BioPAXElement.UNKNOWN_FLOAT;
	private float PMG = BioPAXElement.UNKNOWN_FLOAT;

// ------------------------ CANONICAL METHODS ------------------------

	public int equivalenceCode()
	{
		int result = 29 + DELTA_G_PRIME_O != +0.0f ?
			Float.floatToIntBits(DELTA_G_PRIME_O) : 0;
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
		final deltaGprimeO that = (deltaGprimeO) element;

		return
			(Float.compare(that.getDELTA_G_PRIME_O(), DELTA_G_PRIME_O) == 0)
				&& (Float.compare(that.getIONIC_STRENGTH(), IONIC_STRENGTH) == 0)
				&& (Float.compare(that.getPH(), PH) == 0)
				&& (Float.compare(that.getPMG(), PMG) == 0)
				&& (Float.compare(that.getTEMPERATURE(), TEMPERATURE) == 0);
	}

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return deltaGprimeO.class;
	}

// --------------------- Interface deltaGprimeO ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public float getDELTA_G_PRIME_O()
	{
		return DELTA_G_PRIME_O;
	}

	public void setDELTA_G_PRIME_O(float DELTA_G_PRIME_O)
	{
		this.DELTA_G_PRIME_O = DELTA_G_PRIME_O;
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
