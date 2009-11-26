/*
 * physicalInteractionProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for physicalInteraction
 */
@Entity(name="l2physicalinteraction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class physicalInteractionProxy extends interactionProxy implements physicalInteraction, Serializable {
	public physicalInteractionProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return physicalInteraction.class;
	}

	public void addINTERACTION_TYPE(openControlledVocabulary INTERACTION_TYPE) {
		((physicalInteraction)object).addINTERACTION_TYPE(INTERACTION_TYPE);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=openControlledVocabularyProxy.class)
	@JoinTable(name="l2physicalint_interaction_type")
	public Set<openControlledVocabulary> getINTERACTION_TYPE() {
		return ((physicalInteraction)object).getINTERACTION_TYPE();
	}

	public void removeINTERACTION_TYPE(openControlledVocabulary INTERACTION_TYPE) {
		((physicalInteraction)object).removeINTERACTION_TYPE(INTERACTION_TYPE);
	}

	public void setINTERACTION_TYPE(Set<openControlledVocabulary> INTERACTION_TYPE) {
		((physicalInteraction)object).setINTERACTION_TYPE(INTERACTION_TYPE);
	}

	// �����ς��邽�߂Ƀ��\�b�h��Ē�`
	protected void call_setPARTICIPANTS_x(Set<InteractionParticipant> PARTICIPANTS) {
		proxyPARTICIPANTS = PARTICIPANTS;
	}
}
