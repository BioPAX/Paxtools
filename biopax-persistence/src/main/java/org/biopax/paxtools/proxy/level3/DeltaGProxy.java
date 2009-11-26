/*
 * DeltaGProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for deltaG
 */
@Entity(name="l3deltag")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DeltaGProxy extends UtilityClassProxy implements DeltaG, Serializable {
	public DeltaGProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return DeltaG.class;
	}

	// Property DELTA-G-PRIME-O

	@Basic @Column(name="delta_g_prime_o_x", columnDefinition="text")
	protected String getDeltaGPrime0_x() {
		return floatToString(((DeltaG)object).getDeltaGPrime0());
	}

	protected void setDeltaGPrime0_x(String s) {
		((DeltaG)object).setDeltaGPrime0(stringToFloat(s));
	}

	@Transient
	public float getDeltaGPrime0() {
		return stringToFloat(getDeltaGPrime0_x());
	}

	public void setDeltaGPrime0(float DELTA_G_PRIME_O) {
		setDeltaGPrime0_x(floatToString(DELTA_G_PRIME_O));
	}

	// Property IONIC-STRENGTH

	@Basic @Column(name="ionic_strength_x", columnDefinition="text")
	protected String getIonicStrength_x() {
		return floatToString(((DeltaG)object).getIonicStrength());
	}

	protected void setIonicStrength_x(String IONIC_STRENGTH) {
		((DeltaG)object).setIonicStrength(stringToFloat(IONIC_STRENGTH));
	}

	@Transient
	public float getIonicStrength() {
		return stringToFloat(getIonicStrength_x());
	}

	public void setIonicStrength(float IONIC_STRENGTH) {
		setIonicStrength_x(floatToString(IONIC_STRENGTH));
	}

	// Property PH

	@Basic @Column(name="ph_x", columnDefinition="text")
	protected String getPh_x() {
		return floatToString(((DeltaG)object).getPh());
	}

	protected void setPh_x(String PH) {
		((DeltaG)object).setPh(stringToFloat(PH));
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
		return floatToString(((DeltaG)object).getPMg());
	}

	protected void setPMg_x(String s) {
		((DeltaG)object).setPMg(stringToFloat(s));
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
		return floatToString(((DeltaG)object).getTemperature());
	}

	protected void setTemperature_x(String s) {
		((DeltaG)object).setTemperature(stringToFloat(s));
	}

	@Transient
	public float getTemperature() {
		return stringToFloat(getTemperature_x());
	}

	public void setTemperature(float TEMPERATURE) {
		setTemperature_x(floatToString(TEMPERATURE));
	}
}

