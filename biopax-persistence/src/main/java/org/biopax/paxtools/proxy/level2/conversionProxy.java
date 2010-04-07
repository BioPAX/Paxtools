/*
 * ConversionProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for conversion
 */
@Entity(name="l2conversion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class conversionProxy extends physicalInteractionProxy implements conversion {
	public conversionProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return conversion.class;
	}

	Set<physicalEntityParticipant> proxyLEFT = null;
	Set<physicalEntityParticipant> proxyRIGHT = null;


	@Transient
	public Set<physicalEntityParticipant> getLEFT() {
		return ((conversion)object).getLEFT();
	}

	public void setLEFT(Set<physicalEntityParticipant> LEFT) {
		((conversion)object).setLEFT(LEFT);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=physicalEntityParticipantProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l2conversion_left_x")
	public Set<physicalEntityParticipant> getLEFT_x() {
		if (proxyLEFT == null)
			proxyLEFT = getLEFT();
		return proxyLEFT;
	}

	public void setLEFT_x(Set<physicalEntityParticipant> LEFT) {
		try {

			// 2008.05.26 Takeshi Yoneki
			Set<physicalEntityParticipant> oldLEFT = getLEFT();
			for (physicalEntityParticipant opep: oldLEFT)
				removeLEFT(opep);
			for (physicalEntityParticipant pep: LEFT)
				addLEFT(pep);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		proxyLEFT = LEFT;
	}

	public void addLEFT(physicalEntityParticipant LEFT) {
		((conversion)object).addLEFT(LEFT);
		proxyLEFT = null;
	}

	public void removeLEFT(physicalEntityParticipant LEFT) {
		((conversion)object).removeLEFT(LEFT);
		proxyLEFT = null;
	}

	
	@Transient
	public Set<physicalEntityParticipant> getRIGHT() {
		return ((conversion)object).getRIGHT();
	}

	public void setRIGHT(Set<physicalEntityParticipant> RIGHT) {
		((conversion)object).setRIGHT(RIGHT);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=physicalEntityParticipantProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l2conversion_right_x")
	public Set<physicalEntityParticipant> getRIGHT_x() {
		if (proxyRIGHT == null)
			proxyRIGHT = getRIGHT();
		return proxyRIGHT;
	}

	public void setRIGHT_x(Set<physicalEntityParticipant> RIGHT) {
		try {
			
			// 2008.05.26 Takeshi Yoneki
			Set<physicalEntityParticipant> oldRIGHT = getRIGHT();
			for (physicalEntityParticipant opep: oldRIGHT)
				removeRIGHT(opep);
			for (physicalEntityParticipant pep: RIGHT)
				addRIGHT(pep);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		proxyRIGHT = RIGHT;
	}

	public void addRIGHT(physicalEntityParticipant RIGHT) {
		((conversion)object).addRIGHT(RIGHT);
		proxyRIGHT = null;
	}

	public void removeRIGHT(physicalEntityParticipant RIGHT) {
		((conversion)object).removeRIGHT(RIGHT);
		proxyRIGHT = null;
	}

	@Basic @Enumerated @Column(name="spontaneous_x")
	public SpontaneousType getSPONTANEOUS() {
		return ((conversion)object).getSPONTANEOUS();
	}

	public void setSPONTANEOUS(SpontaneousType SPONTANEOUS) {
		((conversion)object).setSPONTANEOUS(SPONTANEOUS);
	}
}
