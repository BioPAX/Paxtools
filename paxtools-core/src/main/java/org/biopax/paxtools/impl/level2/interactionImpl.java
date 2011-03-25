package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.interaction;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
class interactionImpl extends interactionAdapter
{
// ------------------------------ FIELDS ------------------------------

	private Set<InteractionParticipant> PARTICIPANTS;

// --------------------------- CONSTRUCTORS ---------------------------

	public interactionImpl()
	{
		this.PARTICIPANTS = new HashSet<InteractionParticipant>();
		updatePARTICIPANTS(null, PARTICIPANTS);
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------




	public Class<? extends BioPAXElement> getModelInterface()
	{
		return interaction.class;
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
		this.PARTICIPANTS.add(aParticipant);
		this.setParticipantInverse(aParticipant, false);
	}

	public void removePARTICIPANTS(InteractionParticipant aParticipant)
	{
		this.PARTICIPANTS.remove(aParticipant);
		this.setParticipantInverse(aParticipant, true);
	}
}
