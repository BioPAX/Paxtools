/*
 * DeltaGprimeOProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.deltaGprimeO;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Proxy for deltaGprimeO
 */
@Entity(name="l2deltagprimeo")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class deltaGprimeOProxy extends utilityClassProxy implements deltaGprimeO, Serializable {
	public deltaGprimeOProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return deltaGprimeO.class;
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getDELTA_G_PRIME_O() {
		return stringToFloat(getDELTA_G_PRIME_O_x());
		//return ((deltaGprimeO)object).getDELTA_G_PRIME_O();
	}

	public void setDELTA_G_PRIME_O(float DELTA_G_PRIME_O) {
		setDELTA_G_PRIME_O_x(floatToString(DELTA_G_PRIME_O));
		//((deltaGprimeO)object).setDELTA_G_PRIME_O(DELTA_G_PRIME_O);
	}

	@Basic @Column(name="delta_g_prime_o_x", columnDefinition="text")
	protected String getDELTA_G_PRIME_O_x() {
		return floatToString(((deltaGprimeO)object).getDELTA_G_PRIME_O());
	}

	protected void setDELTA_G_PRIME_O_x(String s) {
		((deltaGprimeO)object).setDELTA_G_PRIME_O(stringToFloat(s));
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getIONIC_STRENGTH() {
		return stringToFloat(getIONIC_STRENGTH_x());
		//return ((deltaGprimeO)object).getIONIC_STRENGTH();
	}

	public void setIONIC_STRENGTH(float IONIC_STRENGTH) {
		setIONIC_STRENGTH_x(floatToString(IONIC_STRENGTH));
		//((deltaGprimeO)object).setIONIC_STRENGTH(IONIC_STRENGTH);
	}

	@Basic @Column(name="ionic_strength_x", columnDefinition="text")
	protected String getIONIC_STRENGTH_x() {
		return floatToString(((deltaGprimeO)object).getIONIC_STRENGTH());
	}

	protected void setIONIC_STRENGTH_x(String IONIC_STRENGTH) {
		((deltaGprimeO)object).setIONIC_STRENGTH(stringToFloat(IONIC_STRENGTH));
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getPH() {
		return stringToFloat(getPH_x());
		//return ((deltaGprimeO)object).getPH();
	}

	public void setPH(float PH) {
		setPH_x(floatToString(PH));
		//((deltaGprimeO)object).setPH(PH);
	}

	@Basic @Column(name="ph_x", columnDefinition="text")
	protected String getPH_x() {
		return floatToString(((deltaGprimeO)object).getPH());
	}

	protected void setPH_x(String PH) {
		((deltaGprimeO)object).setPH(stringToFloat(PH));
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getPMG() {
		return stringToFloat(getPMG_x());
		//return ((deltaGprimeO)object).getPMG();
	}

	public void setPMG(float PMG) {
		setPMG_x(floatToString(PMG));
		//((deltaGprimeO)object).setPMG(PMG);
	}

	@Basic @Column(name="pmg_x", columnDefinition="text")
	protected String getPMG_x() {
		return floatToString(((deltaGprimeO)object).getPMG());
	}

	protected void setPMG_x(String s) {
		((deltaGprimeO)object).setPMG(stringToFloat(s));
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getTEMPERATURE() {
		return stringToFloat(getTEMPERATURE_x());
		//return ((deltaGprimeO)object).getTEMPERATURE();
	}

	public void setTEMPERATURE(float TEMPERATURE) {
		setTEMPERATURE_x(floatToString(TEMPERATURE));
		//((deltaGprimeO)object).setTEMPERATURE(TEMPERATURE);
	}

	@Basic @Column(name="temperature_x", columnDefinition="text")
	protected String getTEMPERATURE_x() {
		return floatToString(((deltaGprimeO)object).getTEMPERATURE());
	}

	protected void setTEMPERATURE_x(String s) {
		((deltaGprimeO)object).setTEMPERATURE(stringToFloat(s));
	}
}

