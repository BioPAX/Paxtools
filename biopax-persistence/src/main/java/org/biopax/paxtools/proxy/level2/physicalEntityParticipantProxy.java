/*
 * physicalEntityParticipantProxy.java
 *
 * 2007.04.02 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for physicalEntityParticipant
 */
// 2007.09.10
@Entity(name = "l2physicalentityparticipant")
@Indexed(index = BioPAXElementProxy.SEARCH_INDEX_NAME)
public class physicalEntityParticipantProxy
	extends InteractionParticipantUtilityClassProxy
	implements physicalEntityParticipant, Serializable
{
	protected physicalEntityParticipantProxy()
	{
	}

	@Transient
	public Class getModelInterface()
	{
		return physicalEntityParticipant.class;
	}

	@ManyToOne(cascade = {CascadeType.ALL},
		targetEntity = openControlledVocabularyProxy.class)
	@JoinColumn(name = "cellular_location_x")
	public openControlledVocabulary getCELLULAR_LOCATION()
	{
		return ((physicalEntityParticipant) object).getCELLULAR_LOCATION();
	}

	@ManyToOne(cascade = {CascadeType.ALL},
		targetEntity = physicalEntityProxy.class)
	@JoinColumn(name = "physical_entity_x")
	public physicalEntity getPHYSICAL_ENTITY()
	{
		return ((physicalEntityParticipant) object).getPHYSICAL_ENTITY();
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public double getSTOICHIOMETRIC_COEFFICIENT()
	{
		return stringToDouble(getSTOICHIOMETRIC_COEFFICIENT_x());
		//return ((physicalEntityParticipant)object).getSTOICHIOMETRIC_COEFFICIENT();
	}

	public void setSTOICHIOMETRIC_COEFFICIENT(double STOICHIOMETRIC_COEFFICIENT)
	{
		setSTOICHIOMETRIC_COEFFICIENT_x(doubleToString(
			STOICHIOMETRIC_COEFFICIENT));
		//((physicalEntityParticipant)object).setSTOICHIOMETRIC_COEFFICIENT(STOICHIOMETRIC_COEFFICIENT);
	}

	@Basic
	@Column(name = "stoichiometric_coefficient_x", columnDefinition = "text")
	protected String getSTOICHIOMETRIC_COEFFICIENT_x()
	{
		return doubleToString(((physicalEntityParticipant) object).getSTOICHIOMETRIC_COEFFICIENT());
	}

	protected void setSTOICHIOMETRIC_COEFFICIENT_x(String s)
	{
		((physicalEntityParticipant) object)
			.setSTOICHIOMETRIC_COEFFICIENT(stringToDouble(s));
	}

	@Transient
	public complex isCOMPONENTof()
	{
		return ((physicalEntityParticipant) object).isCOMPONENTof();
	}

	public void setCELLULAR_LOCATION(openControlledVocabulary CELLULAR_LOCATION)
	{
		((physicalEntityParticipant) object)
			.setCELLULAR_LOCATION(CELLULAR_LOCATION);
	}

	public void setCOMPONENTSof(complex aComplex)
	{
		((physicalEntityParticipant) object).setCOMPONENTSof(aComplex);
	}

	public void setPHYSICAL_ENTITY(physicalEntity PHYSICAL_ENTITY)
	{
		((physicalEntityParticipant) object)
			.setPHYSICAL_ENTITY(PHYSICAL_ENTITY);
	}

	@Transient
	public Set<interaction> isPARTICIPANTSof()
	{
		return ((physicalEntityParticipant) object).isPARTICIPANTSof();
	}

	@Transient
	public boolean isInEquivalentState(physicalEntityParticipant that)
	{
		return ((physicalEntityParticipant) object).isInEquivalentState(that);
	}

	public int stateCode() {
		return ((physicalEntityParticipant)object).stateCode();
	}
}

