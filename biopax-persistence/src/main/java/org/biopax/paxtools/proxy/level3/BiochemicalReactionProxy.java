/*
 * BiochemicalReactionProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for biochemicalReaction
 */
@Entity(name="l3biochemicalreaction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class BiochemicalReactionProxy extends ConversionProxy implements
	BiochemicalReaction, Serializable {
	public BiochemicalReactionProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return BiochemicalReaction.class;
	}

    // Property DELTA-G

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= DeltaGProxy.class)
	@JoinTable(name="l3biochemreact_delta_g")
	public Set<DeltaG> getDeltaG() {
		return ((BiochemicalReaction)object).getDeltaG();
	}

	public void addDeltaG(DeltaG delta_G) {
		((BiochemicalReaction)object).addDeltaG(delta_G);
	}

	public void removeDeltaG(DeltaG delta_G) {
		((BiochemicalReaction)object).removeDeltaG(delta_G);
	}

	public void setDeltaG(Set<DeltaG> delta_G) {
		((BiochemicalReaction)object).setDeltaG(delta_G);
	}

    // Property DELTA-H

	@CollectionOfElements @Column(name="delta_h_x", columnDefinition="text")
	protected Set<String> getDeltaH_x() {
		return floatSetToStringSet(((BiochemicalReaction)object).getDeltaH());
	}

	protected void setDeltaH_x(Set<String> ss) {
		((BiochemicalReaction)object).setDeltaH(stringSetToFloatSet(ss));
	}

	@Transient
	public Set<Float> getDeltaH() {
		return stringSetToFloatSet(getDeltaH_x());
	}

	public void addDeltaH(float DELTA_H) {
		((BiochemicalReaction)object).addDeltaH(DELTA_H);
	}

	public void removeDeltaH(float DELTA_H) {
		((BiochemicalReaction)object).removeDeltaH(DELTA_H);
	}

	public void setDeltaH(Set<Float> DELTA_H) {
		setDeltaH_x(floatSetToStringSet(DELTA_H));
	}

    // Property DELTA-S

	@CollectionOfElements @Column(name="delta_s_x", columnDefinition="text")
	public Set<String> getDeltaS_x() {
		return floatSetToStringSet(((BiochemicalReaction)object).getDeltaS());
	}

	public void setDeltaS_x(Set<String> ss) {
		((BiochemicalReaction)object).setDeltaS(stringSetToFloatSet(ss));
	}

	@Transient
	public Set<Float> getDeltaS() {
		return stringSetToFloatSet(getDeltaS_x());
	}

	public void addDeltaS(float DELTA_S) {
		((BiochemicalReaction)object).addDeltaS(DELTA_S);
	}

	public void removeDeltaS(float DELTA_S) {
		((BiochemicalReaction)object).removeDeltaS(DELTA_S);
	}

	public void setDeltaS(Set<Float> DELTA_S) {
		setDeltaS_x(floatSetToStringSet(DELTA_S));
	}

    // Property EC-NUMBER

	@CollectionOfElements @Column(name="ec_number_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_EC_NUMBER, index=Index.TOKENIZED)
	public Set<String> getECNumber() {
		return ((BiochemicalReaction)object).getECNumber();
	}

	public void addECNumber(String EC_NUMBER) {
		((BiochemicalReaction)object).addECNumber(EC_NUMBER);
	}

	public void removeECNumber(String EC_NUMBER) {
		((BiochemicalReaction)object).removeECNumber(EC_NUMBER);
	}

	public void setECNumber(Set<String> EC_NUMBER) {
		((BiochemicalReaction)object).setECNumber(EC_NUMBER);
	}

    // Property KEQ

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= KPrimeProxy.class)
	@JoinTable(name="l3biochemreact_keq")
	public Set<KPrime> getKEQ() {
		return ((BiochemicalReaction)object).getKEQ();
	}

	public void addKEQ(KPrime KEQ) {
		((BiochemicalReaction)object).addKEQ(KEQ);
	}

	public void removeKEQ(KPrime KEQ) {
		((BiochemicalReaction)object).removeKEQ(KEQ);
	}

	public void setKEQ(Set<KPrime> KEQ) {
		((BiochemicalReaction)object).setKEQ(KEQ);
	}
}
