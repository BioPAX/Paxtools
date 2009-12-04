/*
 * KPrimeProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Proxy for kPrime
 */
@Entity(name="l3kprime")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class KPrimeProxy extends Level3ElementProxy implements KPrime {
	public KPrimeProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return KPrime.class;
	}

	// Property IONIC-STRENGTH

	@Basic @Column(name="ionic_strength_x", columnDefinition="text")
	protected String getIonicStrength_x() {
		return floatToString(((KPrime)object).getIonicStrength());
	}

	protected void setIonicStrength_x(String s) {
		((KPrime)object).setIonicStrength(stringToFloat(s));
	}

	@Transient
	public float getIonicStrength() {
		return stringToFloat(getIonicStrength_x());
	}

	public void setIonicStrength(float IONIC_STRENGTH) {
		setIonicStrength_x(floatToString(IONIC_STRENGTH));
	}

	// Property K-PRIME

	@Basic @Column(name="k_prime_x", columnDefinition="text")
	protected String getKPrime_x() {
		return floatToString(((KPrime)object).getKPrime());
	}

	protected void setKPrime_x(String s) {
		((KPrime)object).setKPrime(stringToFloat(s));
	}

	@Transient
	public float getKPrime() {
		return stringToFloat(getKPrime_x());
	}

	public void setKPrime(float K_PRIME) {
		setKPrime_x(floatToString(K_PRIME));
	}

	// Property PH

	@Basic @Column(name="ph_x", columnDefinition="text")
	protected String getPh_x() {
		return floatToString(((KPrime)object).getPh());
	}

	protected void setPh_x(String PH) {
		((KPrime)object).setPh(stringToFloat(PH));
	}

	@Transient
	public float getPh() {
		return stringToFloat(getPh_x());
	}

	public void setPh(float PH) {
		setPh_x(floatToString(PH));
	}

	// Property PMG

	@Basic @Column(name="pmg_x", columnDefinition="text")
	protected String getPMg_x() {
		return floatToString(((KPrime)object).getPMg());
	}

	protected void setPMg_x(String s) {
		((KPrime)object).setPMg(stringToFloat(s));
	}

	@Transient
	public float getPMg() {
		return stringToFloat(getPMg_x());
	}

	public void setPMg(float PMG) {
		setPMg_x(floatToString(PMG));
	}

	// Property TEMPERATURE

	@Basic @Column(name="temperature_x", columnDefinition="text")
	protected String getTemperature_x() {
		return floatToString(((KPrime)object).getTemperature());
	}

	protected void setTemperature_x(String s) {
		((KPrime)object).setTemperature(stringToFloat(s));
	}

	@Transient
	public float getTemperature() {
		return stringToFloat(getTemperature_x());
	}

	public void setTemperature(float TEMPERATURE) {
		((KPrime)object).setTemperature(TEMPERATURE);
	}
}

