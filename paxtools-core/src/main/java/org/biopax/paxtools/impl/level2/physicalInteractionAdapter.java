package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.physicalInteraction;

import java.util.HashSet;
import java.util.Set;

/**
 */
abstract class physicalInteractionAdapter extends interactionAdapter
	implements physicalInteraction
{
// ------------------------------ FIELDS ------------------------------

	private Set<openControlledVocabulary> INTERACTION_TYPE;

// --------------------------- CONSTRUCTORS ---------------------------

	physicalInteractionAdapter()
	{
		this.INTERACTION_TYPE = new HashSet<openControlledVocabulary>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface physicalInteraction ---------------------

	public void addINTERACTION_TYPE(openControlledVocabulary INTERACTION_TYPE)
	{
		//TODO validate to external CV. Want to fetch and parse OBO. Map and share ocvs to OBO
		this.INTERACTION_TYPE.add(INTERACTION_TYPE);
	}

	public Set<openControlledVocabulary> getINTERACTION_TYPE()
	{
		return INTERACTION_TYPE;
	}

	public void removeINTERACTION_TYPE(
		openControlledVocabulary INTERACTION_TYPE)
	{
		this.INTERACTION_TYPE.remove(INTERACTION_TYPE);
	}

	public void setINTERACTION_TYPE(
		Set<openControlledVocabulary> INTERACTION_TYPE)
	{
		this.INTERACTION_TYPE = INTERACTION_TYPE;
	}

	public void addPARTICIPANTS(InteractionParticipant aParticipant)
	{
		throw new UnsupportedOperationException(
			"Directly setting participants are not allowed !");
	}

	public void removePARTICIPANTS(InteractionParticipant aParticipant)
	{
		throw new UnsupportedOperationException(
			"Directly setting participants are not allowed !");
	}

	public void setPARTICIPANTS(Set<InteractionParticipant> PARTICIPANTS)
	{
		throw new UnsupportedOperationException(
			"Directly setting participants are not allowed !");
	}
}
