package org.biopax.paxtools.impl.level2;

import org.apache.commons.collections15.set.CompositeSet;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;

/**
 */
abstract class interactionAdapter extends processImpl implements interaction
{
// ------------------------------ FIELDS ------------------------------

	private transient CompositeSet<InteractionParticipant> PARTICIPANTS;

// --------------------------- CONSTRUCTORS ---------------------------

	interactionAdapter()
	{
		this.PARTICIPANTS = new CompositeSet<InteractionParticipant>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface interaction ---------------------

	public Set<InteractionParticipant> getPARTICIPANTS()
	{
		return PARTICIPANTS;
	}

// -------------------------- OTHER METHODS --------------------------

	//TODO
    boolean compareParticipantSets(
		Set<InteractionParticipant> set1,
		Set<InteractionParticipant> set2)
	{
		if (set1.size() == set2.size())
		{
			for (InteractionParticipant ip : set1)
			{
//TODO
			}
		}
		return false;
	}
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final interaction that = (interaction) element;
		return compareParticipantSets(this.PARTICIPANTS, that.getPARTICIPANTS());
	}

	public int equivalenceCode()
	{
		return (PARTICIPANTS != null ? PARTICIPANTS.hashCode() : 0);
	}

	
// --------------------- ACCESORS and MUTATORS---------------------

	void updatePARTICIPANTS(
		Set<? extends InteractionParticipant> oldSet,
		Set<? extends InteractionParticipant> newSet)
	{
		if (oldSet != null)
		{
			PARTICIPANTS.removeComposited(oldSet);
			for (InteractionParticipant ip : oldSet)
			{
				setParticipantInverse(ip, true);
			}
		}
		if (newSet != null)
		{
			PARTICIPANTS.addComposited(newSet);
			for (InteractionParticipant ip : newSet)
			{
				setParticipantInverse(ip, false);
			}
		}
	}

	void setParticipantInverse(InteractionParticipant ip,
	                                     boolean remove)
	{
		assert ip != null;
		Set<interaction> pof = ip.isPARTICIPANTSof();
		if (remove)
		{
			pof.remove(this);
		}
		else
		{
			if (ip instanceof physicalEntityParticipant)
			{
				physicalEntityParticipant pep =
					((physicalEntityParticipant) ip);
				if (pep.isCOMPONENTof() != null)
				{
					throw new IllegalBioPAXArgumentException(
						"Illegal attempt to reuse a PEP!\n " +
							"PEP is already used in a complex context");
				}
				else if (!pof.isEmpty() && !pof.contains(this))
				{
					throw new IllegalBioPAXArgumentException(
						"Illegal attempt to reuse a PEP!\n " +
							"PEP is already used in another interaction context");
				}
			}
			pof.add(this);
		}
	}

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        this.PARTICIPANTS = new  CompositeSet<InteractionParticipant>();
    }
}
