package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.paxtools.util.ChildDataStringBridge;
import org.biopax.paxtools.util.OrganismFieldBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */

@javax.persistence.Entity
@Proxy(proxyClass= Interaction.class)
@Indexed
@Boost(1.5f)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class InteractionImpl extends ProcessImpl implements Interaction
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

	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = InteractionVocabularyImpl.class)
	@JoinTable(name="interactionType")
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

	public void setInteractionType(
		Set<InteractionVocabulary> interactionType)
	{
	   this.interactionType = interactionType;
	}

	/* In addition to pathwayComponentOf, stepProcessOf props, we could infer parent pathways from participants (e.g. for a Controller...) 
	 * Wrong! - because having ubiquitous small molecule participants there results in most pathways will be parent for this interaction :)
	 */
	@Fields({
		@Field(name=FIELD_ORGANISM, store=Store.YES, analyze=Analyze.NO, bridge= @FieldBridge(impl = OrganismFieldBridge.class)),
		@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	})
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EntityImpl.class)
	@JoinTable(name="participant")
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
							+ this.getRDFId());
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
