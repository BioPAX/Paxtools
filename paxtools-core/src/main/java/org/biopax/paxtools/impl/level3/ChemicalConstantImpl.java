package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ChemicalConstant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.Transient;

import static java.lang.Float.compare;

@Entity
@Proxy(proxyClass=ChemicalConstant.class)
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class ChemicalConstantImpl extends L3ElementImpl implements ChemicalConstant {
    float ionicStrength;
    float ph;
    float pMg;
    float temperature;

    public ChemicalConstantImpl() {
        this.ionicStrength = KPrimeImpl.UNKNOWN_FLOAT;
        this.ph = KPrimeImpl.UNKNOWN_FLOAT;
        this.pMg = KPrimeImpl.UNKNOWN_FLOAT;
        this.temperature = KPrimeImpl.UNKNOWN_FLOAT;
    }

    public float getIonicStrength() {
        return ionicStrength;
    }

    public void setIonicStrength(float ionicStrength) {
        this.ionicStrength = ionicStrength;
    }

    public float getPh() {
        return ph;
    }

    public void setPh(float ph) {
        this.ph = ph;
    }

    public float getPMg() {
        return pMg;
    }

    public void setPMg(float pMg) {
        this.pMg = pMg;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    @Transient
        public Class<? extends ChemicalConstant> getModelInterface() {
        return ChemicalConstant.class;
    }

    @Override
    protected boolean semanticallyEquivalent(BioPAXElement element) {
        if(!(element instanceof ChemicalConstant))
            return false;

        final ChemicalConstant aKPrime = (ChemicalConstant) element;
        return
                compare(aKPrime.getIonicStrength(), getIonicStrength()) == 0 &&
                        compare(aKPrime.getPh(), getPh()) == 0 &&
                        compare(aKPrime.getPMg(), pMg) == 0 &&
                        compare(aKPrime.getTemperature(), getTemperature()) == 0;

    }

    @Override
    public int equivalenceCode() {
        return super.equivalenceCode();    //To change body of overridden methods use File | Settings | File Templates.
    }
}