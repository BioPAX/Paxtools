/*
 * ConversionProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for conversion
 */
@Entity(name="l3conversion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ConversionProxy extends InteractionProxy implements
	Conversion, Serializable {
	public ConversionProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Conversion.class;
	}

	Set<PhysicalEntity> proxyLEFT = null;
	Set<PhysicalEntity> proxyRIGHT = null;

	// Property LEFT

	// JoinTableなしだと、同じ型への接続だということなのか、一つの接続用テーブルにleftとrightがまとめられてしまう。
	// しかしそれではDB上の規約違反となり実質使えない。
	// leftを保存しようとするとrightがnullであるとか言われる。多分Hibernateのbug。
	// なので、JoinTableを記述して、オブジェクトの接続のテーブルを独立に用意する。
	// 2007.04.26 Takeshi Yoneki
	// IllegalBioPAXArgumentException("Illegal attempt to reuse a PEP!")で禁止されたsetterなので、
	// とりあえず対象外にする。
	// 2007.05.17 Takeshi Yoneki
	// 直接のsetterを使わない別メソッドで表現することにした。
	// 2007.09.05

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3conversion_left_x")
	public Set<PhysicalEntity> getLeft_x() {
		if (proxyLEFT == null)
			proxyLEFT = getLeft();
		return proxyLEFT;
	}

	public void setLeft_x(Set<PhysicalEntity> LEFT) {
		try {
			// 古いものを削除しなくてはいけないのではないか？
			// 2008.05.26 Takeshi Yoneki
			Set<PhysicalEntity> oldLEFT = getLeft();
			for (PhysicalEntity opep: oldLEFT)
				removeLeft(opep);
			for (PhysicalEntity pep: LEFT)
				addLeft(pep);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		proxyLEFT = LEFT;
	}

	@Transient
	public Set<PhysicalEntity> getLeft() {
		return ((Conversion)object).getLeft();
	}

	public void setLeft(Set<PhysicalEntity> LEFT) {
		((Conversion)object).setLeft(LEFT);
	}

	public void addLeft(PhysicalEntity LEFT) {
		((Conversion)object).addLeft(LEFT);
		proxyLEFT = null;
	}

	public void removeLeft(PhysicalEntity LEFT) {
		((Conversion)object).removeLeft(LEFT);
		proxyLEFT = null;
	}

	// Property RIGHT

	// とりあえず対象外にする。
	// 2007.05.17 Takeshi Yoneki
	// 直接のsetterを使わない別メソッドで表現することにした。
	// 2007.09.05

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3conversion_right_x")
	public Set<PhysicalEntity> getRight_x() {
		if (proxyRIGHT == null)
			proxyRIGHT = getRight();
		return proxyRIGHT;
	}

	public void setRight_x(Set<PhysicalEntity> RIGHT) {
		try {
			// 古いものを削除しなくてはいけないのではないか？
			// 2008.05.26 Takeshi Yoneki
			Set<PhysicalEntity> oldRIGHT = getRight();
			for (PhysicalEntity opep: oldRIGHT)
				removeRight(opep);
			for (PhysicalEntity pep: RIGHT)
				addRight(pep);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		proxyRIGHT = RIGHT;
	}

	@Transient
	public Set<PhysicalEntity> getRight() {
		return ((Conversion)object).getRight();
	}

	public void addRight(PhysicalEntity RIGHT) {
		((Conversion)object).addRight(RIGHT);
		proxyRIGHT = null;
	}

	public void removeRight(PhysicalEntity RIGHT) {
		((Conversion)object).removeRight(RIGHT);
		proxyRIGHT = null;
	}

	public void setRight(Set<PhysicalEntity> RIGHT) {
		((Conversion)object).setRight(RIGHT);
	}

	// Property PARTICIPANT-STOICHIOMETRY

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = StoichiometryProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3conversion_part_stoich_x")
	public Set<Stoichiometry> getParticipantStoichiometry() {
		return ((Conversion)object).getParticipantStoichiometry();
	}

	public void addParticipantStoichiometry(Stoichiometry new_STOICHIOMETRY) {
		((Conversion)object).addParticipantStoichiometry(
			new_STOICHIOMETRY);
	}

	public void removeParticipantStoichiometry(Stoichiometry old_STOICHIOMETRY) {
		((Conversion)object).removeParticipantStoichiometry(
			old_STOICHIOMETRY);
	}

	public void setParticipantStoichiometry(Set<Stoichiometry> new_STOICHIOMETRY) {
		((Conversion)object).setParticipantStoichiometry(
			new_STOICHIOMETRY);
	}

	// Property SPONTANEOUS

//	@Basic @Enumerated @Column(name="spontaneous_x")
	@Basic @Column(name="spontaneous_x")
	public Boolean getSpontaneous() {
		return ((Conversion)object).getSpontaneous();
	}

	public void setSpontaneous(Boolean SPONTANEOUS) {
		((Conversion)object).setSpontaneous(SPONTANEOUS);
	}

    //Property CONVERSION DIRECTION

	@Basic @Enumerated @Column(name="conversion_direction_x")
    public ConversionDirectionType getConversionDirection() {
		return ((Conversion)object).getConversionDirection();
	}

	public void setConversionDirection(ConversionDirectionType conversionDirection) {
		((Conversion)object).setConversionDirection(conversionDirection);
	}
}
