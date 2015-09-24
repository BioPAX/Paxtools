package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;

public class InteractionImpl extends ProcessImpl implements Interaction
{
// ------------------------------ FIELDS ------------------------------

	Set<Entity> participant;
	private Set<InteractionVocabulary> interactionType;
    private final Log log = LogFactory.getLog(InteractionImpl.class);

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

	public void addInteractionType(
		InteractionVocabulary newinteractionType)
	{
	   if(newinteractionType != null)
			this.interactionType.add(newinteractionType);
	}

	public void removeInteractionType(
		InteractionVocabulary oldinteractionType)
	{
		if(oldinteractionType != null)
			this.interactionType.remove(oldinteractionType);
	}

	public Set<Entity> getParticipant()
	{
		return participant;
	}

	protected void setParticipant(Set<Entity> participant)
	{
        this.participant = participant;
    }

	public void addParticipant(Entity aParticipant)
	{
		if (aParticipant != null) {
			if (aParticipant != null) {
				this.participant.add(aParticipant);
				aParticipant.getParticipantOf().add(this);
			} else {
				if (log.isWarnEnabled())
					log.warn("Null object passed to addParticipant @"
							+ this.getUri());
			}
		}
    }

	public void removeParticipant(Entity aParticipant)
	{
		if (aParticipant != null) {
			this.participant.remove(aParticipant);
			aParticipant.getParticipantOf().remove(this);
		}
	}
}
