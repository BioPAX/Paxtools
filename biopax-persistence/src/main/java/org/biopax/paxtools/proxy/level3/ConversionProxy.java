/*
 * ConversionProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;

/**
 * Proxy for conversion
 */
@Entity(name="l3conversion")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ConversionProxy<T extends Conversion> extends InteractionProxy<T>
	implements Conversion 
{
	

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3conversion_left_x")
	public Set<PhysicalEntity> getLeft() {
		return object.getLeft();
	}

	public void setLeft(Set<PhysicalEntity> LEFT) {
		 object.setLeft(LEFT);
	}

	public void addLeft(PhysicalEntity LEFT) {
		object.addLeft(LEFT);
	}

	public void removeLeft(PhysicalEntity LEFT) {
		object.removeLeft(LEFT);
	}

	
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3conversion_right_x")
	public Set<PhysicalEntity> getRight() {
		return object.getRight();
	}

	public void addRight(PhysicalEntity RIGHT) {
		object.addRight(RIGHT);
	}

	public void removeRight(PhysicalEntity RIGHT) {
		object.removeRight(RIGHT);
	}

	public void setRight(Set<PhysicalEntity> RIGHT) {
		 object.setRight(RIGHT);
	}

	// Property PARTICIPANT-STOICHIOMETRY

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = StoichiometryProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3conversion_part_stoich_x")
	public Set<Stoichiometry> getParticipantStoichiometry() {
		return object.getParticipantStoichiometry();
	}

	public void addParticipantStoichiometry(Stoichiometry new_STOICHIOMETRY) {
		object.addParticipantStoichiometry(
			new_STOICHIOMETRY);
	}

	public void removeParticipantStoichiometry(Stoichiometry old_STOICHIOMETRY) {
		object.removeParticipantStoichiometry(
			old_STOICHIOMETRY);
	}

	public void setParticipantStoichiometry(Set<Stoichiometry> new_STOICHIOMETRY) {
		object.setParticipantStoichiometry(
			new_STOICHIOMETRY);
	}

	// Property SPONTANEOUS

	@Basic @Column(name="spontaneous_x")
	public Boolean getSpontaneous() {
		return object.getSpontaneous();
	}

	public void setSpontaneous(Boolean SPONTANEOUS) {
		object.setSpontaneous(SPONTANEOUS);
	}

    //Property CONVERSION DIRECTION

	@Basic @Enumerated @Column(name="conversion_direction_x")
    public ConversionDirectionType getConversionDirection() {
		return object.getConversionDirection();
	}

	public void setConversionDirection(ConversionDirectionType conversionDirection) {
		object.setConversionDirection(conversionDirection);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Conversion.class;
	}
}
