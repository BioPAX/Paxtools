package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.level3.InteractionVocabulary;

import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@javax.persistence.Entity
class InteractionImpl extends ProcessImpl implements Interaction
{
// ------------------------------ FIELDS ------------------------------

	Set<Entity> participant;
	private Set<InteractionVocabulary> interactionType;
    private final Log log = LogFactory.getLog(InteractionImpl.class);

// --------------------------- CONSTRUCTORS ---------------------------

	public InteractionImpl()
	{
		this.interactionType = new HashSet<InteractionVocabulary>();
		this.participant = new HashSet<Entity>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------



	@Transient
	public Class<? extends Interaction> getModelInterface()
	{
		return Interaction.class;
	}

// --------------------- Interface Interaction ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	@ManyToMany(targetEntity = InteractionVocabularyImpl.class)
	public Set<InteractionVocabulary> getInteractionType()
	{
	   return interactionType;
	}

	public void addInteractionType(
		InteractionVocabulary interactionType)
	{
	   this.interactionType.add(interactionType);
	}

	public void removeInteractionType(
		InteractionVocabulary interactionType)
	{
	   this.interactionType.remove(interactionType);
	}

	public void setInteractionType(
		Set<InteractionVocabulary> interactionType)
	{
	   this.interactionType = interactionType;
	}

	@ManyToMany(targetEntity = EntityImpl.class)
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
