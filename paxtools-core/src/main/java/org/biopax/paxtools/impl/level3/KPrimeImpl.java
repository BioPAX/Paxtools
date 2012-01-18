package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.KPrime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

import static java.lang.Float.compare;

/**
 */
@Entity
 @Proxy(proxyClass=KPrime.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class KPrimeImpl extends ChemicalConstantImpl implements KPrime
{

    private float kPrime = UNKNOWN_FLOAT;
    

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
