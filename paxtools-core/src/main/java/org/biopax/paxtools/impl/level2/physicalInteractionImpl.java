package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.physicalInteraction;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.HashSet;
import java.util.Set;

/**
 */
class physicalInteractionImpl extends physicalInteractionAdapter
{
// ------------------------------ FIELDS ------------------------------

	private Set<InteractionParticipant> PARTICIPANTS;

// --------------------------- CONSTRUCTORS ---------------------------

	public physicalInteractionImpl()
	{
		this.PARTICIPANTS = new HashSet<InteractionParticipant>();
		this.updatePARTICIPANTS(null, PARTICIPANTS);
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return physicalInteraction.class;
	}

// --------------------- Interface interaction ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<InteractionParticipant> getPARTICIPANTS()
	{
		return PARTICIPANTS;
	}

	public void setPARTICIPANTS(Set<InteractionParticipant> PARTICIPANTS)
	{
		if (PARTICIPANTS == null)
		{
			PARTICIPANTS = new HashSet<InteractionParticipant>();
		}
		updatePARTICIPANTS(this.PARTICIPANTS, this.PARTICIPANTS = PARTICIPANTS);
	}

	public void addPARTICIPANTS(InteractionParticipant aParticipant)
	{
		if (aParticipant != null)
		{
			this.PARTICIPANTS.add(aParticipant);
			this.setParticipantInverse(aParticipant, false);
		}
		else
		{
			throw new IllegalBioPAXArgumentException(
				"No null participant allowed");
		}
	}

	public void removePARTICIPANTS(InteractionParticipant aParticipant)
	{
		this.PARTICIPANTS.remove(aParticipant);
		this.setParticipantInverse(aParticipant, true);
	}
}
