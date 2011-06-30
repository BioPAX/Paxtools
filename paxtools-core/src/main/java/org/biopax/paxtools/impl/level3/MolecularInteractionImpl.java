package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Transient;

/**
 */
@javax.persistence.Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class MolecularInteractionImpl extends InteractionImpl
        implements MolecularInteraction
{
	public MolecularInteractionImpl() {
	}
	
	@Transient
    public Class<? extends MolecularInteraction> getModelInterface()
    {
        return MolecularInteraction.class;
    }

    public void addParticipant(Entity participant)
    {
		if (participant != null) {
			if (participant instanceof PhysicalEntity) {
				super.addParticipant(participant);
			} else {
				throw new IllegalBioPAXArgumentException(
						"Participants of a molecular interaction should be"
								+ "of type PhysicalEntity. Parameter "
								+ participant + "is of type "
								+ participant.getClass());
			}
		}
    }
}
