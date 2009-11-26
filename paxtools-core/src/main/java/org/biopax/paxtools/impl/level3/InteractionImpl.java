package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
class InteractionImpl extends ProcessImpl implements Interaction
{
// ------------------------------ FIELDS ------------------------------

	Set<Entity> participant;
	private Set<ControlledVocabulary> interactionType;
    private final Log log = LogFactory.getLog(InteractionImpl.class);

// --------------------------- CONSTRUCTORS ---------------------------

	public InteractionImpl()
	{
		this.interactionType = new HashSet<ControlledVocabulary>();
		this.participant = new HashSet<Entity>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------




	public Class<? extends Interaction> getModelInterface()
	{
		return Interaction.class;
	}

// --------------------- Interface Interaction ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<ControlledVocabulary> getInteractionType()
	{
	   return interactionType;
	}

	public void addInteractionType(
		ControlledVocabulary interactionType)
	{
	   this.interactionType.add(interactionType);
	}

	public void removeInteractionType(
		ControlledVocabulary interactionType)
	{
	   this.interactionType.remove(interactionType);
	}

	public void setInteractionType(
		Set<ControlledVocabulary> interactionType)
	{
	   this.interactionType = interactionType;
	}

	public Set<Entity> getParticipant()
	{
		return participant;
	}

	public void setParticipant(Set<Entity> participant)
	{
		if (participant == null)
		{
			participant = new HashSet<Entity>();
		}
        this.participant = participant;
    }

	public void addParticipant(Entity aParticipant)
	{
		if(aParticipant!= null)
        {
            this.participant.add(aParticipant);
            aParticipant.getParticipantsOf().add(this);
        }
        else
        {
            log.warn("Null object passed to addParticipant @"+this.getRDFId());
        }
    }

	public void removeParticipant(Entity aParticipant)
	{
		this.participant.remove(aParticipant);
		aParticipant.getParticipantsOf().remove(this);
	}
    
}
