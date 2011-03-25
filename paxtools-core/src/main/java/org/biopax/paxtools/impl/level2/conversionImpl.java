package org.biopax.paxtools.impl.level2;

import org.apache.commons.collections15.set.CompositeSet;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 */
class conversionImpl extends physicalInteractionAdapter
	implements conversion
{
// ------------------------------ FIELDS ------------------------------

	private Set<physicalEntityParticipant> RIGHT;
	private Set<physicalEntityParticipant> LEFT;

	private SpontaneousType SPONTANEOUS;

// --------------------------- CONSTRUCTORS ---------------------------

	public conversionImpl()
	{
		this.LEFT = new HashSet<physicalEntityParticipant>();
		this.RIGHT = new HashSet<physicalEntityParticipant>();
		updatePARTICIPANTS(null, LEFT);
		updatePARTICIPANTS(null, RIGHT);
	}
	//todo
    protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final interaction that = (conversion) element;
		return LEFT.equals(that.getPARTICIPANTS());
	}

	//todo
	public int equivalenceCode()
	{
		return 0;
	}
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return conversion.class;
	}

// --------------------- Interface conversion ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<physicalEntityParticipant> getRIGHT()
	{
		return RIGHT;
	}

	public void setRIGHT(Set<physicalEntityParticipant> RIGHT)
	{
		if (RIGHT == null)
		{
			RIGHT = new HashSet<physicalEntityParticipant>();
		}
		updatePARTICIPANTS(this.RIGHT, this.RIGHT = RIGHT);
	}

	public void addRIGHT(physicalEntityParticipant RIGHT)
	{
		this.RIGHT.add(RIGHT);
		setParticipantInverse(RIGHT, false);
	}

	public void removeRIGHT(physicalEntityParticipant RIGHT)
	{
		this.RIGHT.remove(RIGHT);
		setParticipantInverse(RIGHT, true);
	}

	public Set<physicalEntityParticipant> getLEFT()

	{
		return LEFT;
	}

	public void setLEFT(Set<physicalEntityParticipant> LEFT)
	{
		if (LEFT == null)
		{
			LEFT = new HashSet<physicalEntityParticipant>();
		}

		updatePARTICIPANTS(this.LEFT, this.LEFT = LEFT);
	}

	public void addLEFT(physicalEntityParticipant LEFT)
	{
		this.LEFT.add(LEFT);
		setParticipantInverse(LEFT,false);
	}

	public void removeLEFT(physicalEntityParticipant LEFT)
	{
		this.LEFT.remove(LEFT);
		setParticipantInverse(LEFT, true);
	}

	public SpontaneousType getSPONTANEOUS()
	{
		return SPONTANEOUS;
	}

	public void setSPONTANEOUS(SpontaneousType SpontanousType)
	{
		this.SPONTANEOUS = SpontanousType;
	}

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
                CompositeSet<InteractionParticipant> interactionParticipants =
                (CompositeSet<InteractionParticipant>) super.getPARTICIPANTS();
        interactionParticipants.addComposited(LEFT);
        interactionParticipants.addComposited(RIGHT);


    }

}
