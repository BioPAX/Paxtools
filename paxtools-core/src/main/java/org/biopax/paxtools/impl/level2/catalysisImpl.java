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
class catalysisImpl extends controlImpl implements catalysis
{
// ------------------------------ FIELDS ------------------------------

	private Direction DIRECTION;
	private Set<physicalEntityParticipant> COFACTOR;

// --------------------------- CONSTRUCTORS ---------------------------

	public catalysisImpl()
	{
		this.COFACTOR = new HashSet<physicalEntityParticipant>();
		updatePARTICIPANTS(null, COFACTOR);
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------



	public Class<? extends BioPAXElement> getModelInterface()
	{
		return catalysis.class;
	}

// --------------------- Interface catalysis ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Direction getDIRECTION()
	{
		return DIRECTION;
	}

	public void setDIRECTION(Direction DIRECTION)
	{
		this.DIRECTION = DIRECTION;
	}

	public Set<physicalEntityParticipant> getCOFACTOR()
	{
		return COFACTOR;
	}

	public void setCOFACTOR(Set<physicalEntityParticipant> COFACTOR)
	{
		if (COFACTOR == null)
		{
			COFACTOR = new HashSet<physicalEntityParticipant>();
		}
		updatePARTICIPANTS(this.COFACTOR, this.COFACTOR = COFACTOR);
	}

	public void addCOFACTOR(physicalEntityParticipant COFACTOR)
	{
		this.COFACTOR.add(COFACTOR);
		setParticipantInverse(COFACTOR, false);
	}

	public void removeCOFACTOR(physicalEntityParticipant COFACTOR)
	{
		this.COFACTOR.remove(COFACTOR);
		setParticipantInverse(COFACTOR, true);
	}


	protected boolean checkCONTROLLED(process controlled)
	{
		return controlled instanceof conversion;
	}

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        CompositeSet<InteractionParticipant> interactionParticipants =
                (CompositeSet<InteractionParticipant>) super.getPARTICIPANTS();
        interactionParticipants.addComposited(COFACTOR);
    }

}
