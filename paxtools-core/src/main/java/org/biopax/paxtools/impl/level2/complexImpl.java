package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.bioSource;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;

import java.util.HashSet;
import java.util.Set;

/**
 */
class complexImpl extends physicalEntityImpl implements complex
{
// ------------------------------ FIELDS ------------------------------

	private bioSource ORGANISM;

	private Set<physicalEntityParticipant> COMPONENTS;

// --------------------------- CONSTRUCTORS ---------------------------

	public complexImpl()
	{
		this.COMPONENTS = new HashSet<physicalEntityParticipant>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return complex.class;
	}

// --------------------- Interface complex ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<physicalEntityParticipant> getCOMPONENTS()
	{
		return COMPONENTS;
	}

	public void addCOMPONENTS(physicalEntityParticipant COMPONENT)
	{
		this.COMPONENTS.add(COMPONENT);
		COMPONENT.setCOMPONENTSof(this);
	}

	public void removeCOMPONENTS(physicalEntityParticipant COMPONENT)
	{
		this.COMPONENTS.remove(COMPONENT);
		COMPONENT.setCOMPONENTSof(null);
	}

	public void setCOMPONENTS(Set<physicalEntityParticipant> COMPONENTS)
	{
		this.COMPONENTS = COMPONENTS;
		for (physicalEntityParticipant physicalEntityParticipant : COMPONENTS)
		{
			physicalEntityParticipant.setCOMPONENTSof(this);
		}
	}

	public bioSource getORGANISM()
	{
		return ORGANISM;
	}

	/**
	 * ORGANISM of origin for this sequence entity
	 *
	 * @param ORGANISM- set null for cannonical
	 */
	public void setORGANISM(bioSource ORGANISM)
	{
		this.ORGANISM = ORGANISM;
	}
}
