package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.DeltaG;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

import static java.lang.Float.compare;

@Entity
@Proxy(proxyClass = DeltaG.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DeltaGImpl extends ChemicalConstantImpl implements DeltaG {

    private float deltaGPrime0 = UNKNOWN_FLOAT;


    public DeltaGImpl() {
    }

    //
    // BioPAXElement interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Transient
    public Class<? extends DeltaG> getModelInterface() {
        return DeltaG.class;
    }


    protected boolean semanticallyEquivalent(BioPAXElement element) {
        return super.semanticallyEquivalent(element) && (compare(((DeltaG) element).getDeltaGPrime0(), deltaGPrime0) == 0);
    }


    public int equivalenceCode() {
        return super.equivalenceCode()+
                29 + deltaGPrime0 != +0.0f ?
                Float.floatToIntBits(deltaGPrime0) : 0;
    }

    public float getDeltaGPrime0() {
        return deltaGPrime0;
    }

    public void setDeltaGPrime0(float deltaGPrime0) {
        this.deltaGPrime0 = deltaGPrime0;
    }

}
