package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.KPrime;

import static java.lang.Float.compare;


public class KPrimeImpl extends ChemicalConstantImpl implements KPrime
{

    private float kPrime = UNKNOWN_FLOAT;
    

    public KPrimeImpl() {
	}
	
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
        return super.semanticallyEquivalent(element) && (compare(((KPrime) element).getKPrime(), kPrime) == 0);
	}

	public int equivalenceCode()
	{
        return super.equivalenceCode()+
                29 + kPrime != +0.0f ?
                Float.floatToIntBits(kPrime) : 0;
	}

	//
	// KPrime interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	
	public float getKPrime()
	{
		return kPrime;
	}

	public void setKPrime(float prime)
	{
		this.kPrime = prime;
	}

	
}
