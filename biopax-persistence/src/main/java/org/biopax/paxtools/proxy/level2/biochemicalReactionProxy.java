/*
 * BiochemicalReactionProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for biochemicalReaction
 */
@Entity(name="l2biochemicalreaction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class biochemicalReactionProxy extends conversionProxy implements biochemicalReaction, Serializable {
	public biochemicalReactionProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return biochemicalReaction.class;
	}

	public void addDELTA_G(deltaGprimeO DELTA_G) {
		((biochemicalReaction)object).addDELTA_G(DELTA_G);
	}

	public void addDELTA_H(double DELTA_H) {
		((biochemicalReaction)object).addDELTA_H(DELTA_H);
	}

	public void addDELTA_S(double DELTA_S) {
		((biochemicalReaction)object).addDELTA_S(DELTA_S);
	}

	public void addEC_NUMBER(String EC_NUMBER) {
		((biochemicalReaction)object).addEC_NUMBER(EC_NUMBER);
	}

	public void addKEQ(kPrime KEQ) {
		((biochemicalReaction)object).addKEQ(KEQ);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=deltaGprimeOProxy.class)
	@JoinTable(name="l2biochemicalreaction_delta_g")
	public Set<deltaGprimeO> getDELTA_G() {
		return ((biochemicalReaction)object).getDELTA_G();
	}

	@Transient
	//@CollectionOfElements @Column(columnDefinition="text")
	public Set<Double> getDELTA_H() {
		return stringSetToDoubleSet(getDELTA_H_x());
		//return ((biochemicalReaction)object).getDeltaH();
	}

	public void setDELTA_H(Set<Double> DELTA_H) {
		setDELTA_H_x(doubleSetToStringSet(DELTA_H));
		//((biochemicalReaction)object).setDeltaH(DELTA_H);
	}

	@CollectionOfElements @Column(name="delta_h_x", columnDefinition="text")
	protected Set<String> getDELTA_H_x() {
		return doubleSetToStringSet(((biochemicalReaction)object).getDELTA_H());
	}

	protected void setDELTA_H_x(Set<String> ss) {
		((biochemicalReaction)object).setDELTA_H(stringSetToDoubleSet(ss));
	}

	@Transient
	//@CollectionOfElements @Column(columnDefinition="text")
	public Set<Double> getDELTA_S() {
		return stringSetToDoubleSet(getDELTA_S_x());
		//return ((biochemicalReaction)object).getDeltaS();
	}

	public void setDELTA_S(Set<Double> DELTA_S) {
		setDELTA_S_x(doubleSetToStringSet(DELTA_S));
		//((biochemicalReaction)object).setDeltaS(DELTA_S);
	}

	@CollectionOfElements @Column(name="delta_s_x", columnDefinition="text")
	public Set<String> getDELTA_S_x() {
		return doubleSetToStringSet(((biochemicalReaction)object).getDELTA_S());
	}

	public void setDELTA_S_x(Set<String> ss) {
		((biochemicalReaction)object).setDELTA_S(stringSetToDoubleSet(ss));
	}

	@CollectionOfElements @Column(name="ec_number_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_EC_NUMBER, index=Index.TOKENIZED)
	public Set<String> getEC_NUMBER() {
		return ((biochemicalReaction)object).getEC_NUMBER();
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=kPrimeProxy.class)
	@JoinTable(name="l2biochemicalreaction_keq")
	public Set<kPrime> getKEQ() {
		return ((biochemicalReaction)object).getKEQ();
	}

	public void removeDELTA_G(deltaGprimeO DELTA_G) {
		((biochemicalReaction)object).removeDELTA_G(DELTA_G);
	}

	public void removeDELTA_H(double DELTA_H) {
		((biochemicalReaction)object).removeDELTA_H(DELTA_H);
	}

	public void removeDELTA_S(double DELTA_S) {
		((biochemicalReaction)object).removeDELTA_S(DELTA_S);
	}

	public void removeEC_NUMBER(String EC_NUMBER) {
		((biochemicalReaction)object).removeEC_NUMBER(EC_NUMBER);
	}

	public void removeKEQ(kPrime KEQ) {
		((biochemicalReaction)object).removeKEQ(KEQ);
	}

	public void setDELTA_G(Set<deltaGprimeO> DELTA_G) {
		((biochemicalReaction)object).setDELTA_G(DELTA_G);
	}

	public void setEC_NUMBER(Set<String> EC_NUMBER) {
		((biochemicalReaction)object).setEC_NUMBER(EC_NUMBER);
	}

	public void setKEQ(Set<kPrime> KEQ) {
		((biochemicalReaction)object).setKEQ(KEQ);
	}
}
