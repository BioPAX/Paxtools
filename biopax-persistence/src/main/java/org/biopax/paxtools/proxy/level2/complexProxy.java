/*
 * ComplexProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for complex
 */
@Entity(name="l2complex")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class complexProxy extends physicalEntityProxy implements complex, Serializable {
	public complexProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return complex.class;
	}

	// 単純にCOMPONENTSを永続化対象外にして良いかどうかは不明。
	// 下記のように実装に対象インスタンスの問題があるので当面対象外とする。
	// 2007.04.26 Takeshi Yoneki
	// equals()とhashCode()を実装することでcontainsでProxyも扱えるようにする。
	// 2007.05.16
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=physicalEntityParticipantProxy.class)
	@JoinTable(name="l2complex_components")
	public Set<physicalEntityParticipant> getCOMPONENTS() {
		return ((complex)object).getCOMPONENTS();
	}

	public void setCOMPONENTS(Set<physicalEntityParticipant> COMPONENTS) {
		((complex)object).setCOMPONENTS(COMPONENTS);
	}

	public void addCOMPONENTS(physicalEntityParticipant COMPONENTS) {
		// ImplのaddCOMPONENTSにおいて、相互参照のためにthisを登録する記述があり、それはImplであり、proxyでないため、
		// ここでImplをパラメタとして渡すことにする。
		// 2007.04.26 Takeshi Yoneki
		// equals()とhashCode()を実装することでcontainsでProxyも扱えるようにする。
		// 2007.05.16
		((complex)object).addCOMPONENTS(COMPONENTS);
	}

	public void removeCOMPONENTS(physicalEntityParticipant COMPONENTS) {
		((complex)object).removeCOMPONENTS(COMPONENTS);
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=bioSourceProxy.class)
	@JoinColumn(name="organism_x")
	public bioSource getORGANISM() {
		return ((complex)object).getORGANISM();
	}

	public void setORGANISM(bioSource ORGANISM) {
		((complex)object).setORGANISM(ORGANISM);
	}
}
