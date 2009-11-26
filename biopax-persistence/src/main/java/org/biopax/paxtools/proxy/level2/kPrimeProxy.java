/*
 * KPrimeProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.kPrime;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Proxy for kPrime
 */
@Entity(name="l2kprime")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class kPrimeProxy extends utilityClassProxy implements kPrime, Serializable {
	public kPrimeProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return kPrime.class;
	}


	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getIONIC_STRENGTH() {
		return stringToFloat(getIONIC_STRENGTH_x());
		//return ((kPrime)object).getIONIC_STRENGTH();
	}

	public void setIONIC_STRENGTH(float IONIC_STRENGTH) {
		setIONIC_STRENGTH_x(floatToString(IONIC_STRENGTH));
		//((kPrime)object).setIONIC_STRENGTH(IONIC_STRENGTH);
	}

	@Basic @Column(name="ionic_strength_x", columnDefinition="text")
	protected String getIONIC_STRENGTH_x() {
		return floatToString(((kPrime)object).getIONIC_STRENGTH());
	}

	protected void setIONIC_STRENGTH_x(String s) {
		((kPrime)object).setIONIC_STRENGTH(stringToFloat(s));
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getK_PRIME() {
		return stringToFloat(getK_PRIME_x());
		//return ((kPrime)object).getK_PRIME();
	}

	public void setK_PRIME(float K_PRIME) {
		setK_PRIME_x(floatToString(K_PRIME));
		//((kPrime)object).setK_PRIME(K_PRIME);
	}

	@Basic @Column(name="k_prime_x", columnDefinition="text")
	protected String getK_PRIME_x() {
		return floatToString(((kPrime)object).getK_PRIME());
	}

	protected void setK_PRIME_x(String s) {
		((kPrime)object).setK_PRIME(stringToFloat(s));
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getPH() {
		return stringToFloat(getPH_x());
		//return ((kPrime)object).getPH();
	}

	public void setPH(float PH) {
		setPH_x(floatToString(PH));
		//((kPrime)object).setPH(PH);
	}

	@Basic @Column(name="ph_x", columnDefinition="text")
	protected String getPH_x() {
		return floatToString(((kPrime)object).getPH());
	}

	protected void setPH_x(String PH) {
		((kPrime)object).setPH(stringToFloat(PH));
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getPMG() {
		return stringToFloat(getPMG_x());
		//return ((kPrime)object).getPMG();
	}

	public void setPMG(float PMG) {
		setPMG_x(floatToString(PMG));
		//((kPrime)object).setPMG(PMG);
	}

	@Basic @Column(name="pmg_x", columnDefinition="text")
	protected String getPMG_x() {
		return floatToString(((kPrime)object).getPMG());
	}

	protected void setPMG_x(String s) {
		((kPrime)object).setPMG(stringToFloat(s));
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public float getTEMPERATURE() {
		return stringToFloat(getTEMPERATURE_x());
		//return ((kPrime)object).getTEMPERATURE();
	}

	public void setTEMPERATURE(float TEMPERATURE) {
		setTEMPERATURE_x(floatToString(TEMPERATURE));
		//((kPrime)object).setTEMPERATURE(TEMPERATURE);
	}

	@Basic @Column(name="temperature_x", columnDefinition="text")
	protected String getTEMPERATURE_x() {
		return floatToString(((kPrime)object).getTEMPERATURE());
	}

	protected void setTEMPERATURE_x(String s) {
		((kPrime)object).setTEMPERATURE(stringToFloat(s));
	}
}

