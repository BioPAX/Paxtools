package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;

public class InteractionImpl extends ProcessImpl implements Interaction
{
// ------------------------------ FIELDS ------------------------------

	private Set<Entity> participant;
	private Set<InteractionVocabulary> interactionType;

// --------------------------- CONSTRUCTORS ---------------------------

	public InteractionImpl()
	{
		this.interactionType = BPCollections.I.createSafeSet();
		this.participant = BPCollections.I.createSafeSet();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends Interaction> getModelInterface()
	{
		return Interaction.class;
	}

// --------------------- Interface Interaction ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<InteractionVocabulary> getInteractionType()
	{
		return interactionType;
	}

	public void addInteractionType(InteractionVocabulary newinteractionType)
	{
		if(newinteractionType != null) {
			this.interactionType.add(newinteractionType);
		}
	}

	public void removeInteractionType(InteractionVocabulary oldinteractionType)
	{
		if(oldinteractionType != null) {
			this.interactionType.remove(oldinteractionType);
		}
	}

	public Set<Entity> getParticipant()
	{
		return participant;
	}

	//this was originally added mainly for JPA
	protected void setParticipant(Set<Entity> participant)
	{
		this.participant = participant;
	}

	public void addParticipant(Entity aParticipant)
	{
		if (aParticipant != null) {
			if(this.participant.add(aParticipant)) {
				aParticipant.getParticipantOf().add(this);
			}
		}
	}

	public void removeParticipant(Entity aParticipant)
	{
		if (aParticipant != null) {
			if(this.participant.remove(aParticipant)) {
				aParticipant.getParticipantOf().remove(this);
			}
		}
	}
}
