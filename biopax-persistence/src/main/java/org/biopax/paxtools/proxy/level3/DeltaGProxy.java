/*
 * DeltaGProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Proxy for deltaG
 */
@Entity(name="l3deltag")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DeltaGProxy extends Level3ElementProxy<DeltaG> implements DeltaG {

	// Property DELTA-G-PRIME-O

	@Basic @Column(name="delta_g_prime_o_x", columnDefinition="text")
	protected String getDeltaGPrime0_x() {
		return floatToString(object.getDeltaGPrime0());
	}

	protected void setDeltaGPrime0_x(String s) {
		object.setDeltaGPrime0(stringToFloat(s));
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
		return floatToString(object.getIonicStrength());
	}

	protected void setIonicStrength_x(String IONIC_STRENGTH) {
		object.setIonicStrength(stringToFloat(IONIC_STRENGTH));
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
		return floatToString(object.getPh());
	}

	protected void setPh_x(String PH) {
		object.setPh(stringToFloat(PH));
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
		return floatToString(object.getPMg());
	}

	protected void setPMg_x(String s) {
		object.setPMg(stringToFloat(s));
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
		return floatToString(object.getTemperature());
	}

	protected void setTemperature_x(String s) {
		object.setTemperature(stringToFloat(s));
	}

	@Transient
	public float getTemperature() {
		return stringToFloat(getTemperature_x());
	}

	public void setTemperature(float TEMPERATURE) {
		setTemperature_x(floatToString(TEMPERATURE));
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return DeltaG.class;
	}
}

