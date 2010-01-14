package org.biopax.paxtools.impl.level2;

import org.apache.commons.collections15.set.CompositeSet;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 */
class controlImpl extends physicalInteractionAdapter
	implements control
{
// ------------------------------ FIELDS ------------------------------

	private ControlType CONTROL_TYPE;
	private Set<physicalEntityParticipant> CONTROLLER;
	private Set<process> CONTROLLED;

// --------------------------- CONSTRUCTORS ---------------------------

	public controlImpl()
	{
		this.CONTROLLER = new HashSet<physicalEntityParticipant>();
		this.CONTROLLED = new HashSet<process>();
		updatePARTICIPANTS(null, CONTROLLER);
		updatePARTICIPANTS(null, CONTROLLED);
	}

// ------------------------ INTERFACE METHODS ------------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return control.class;
	}

// --------------------- Interface control ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public ControlType getCONTROL_TYPE()
	{
		return CONTROL_TYPE;
	}

	public void setCONTROL_TYPE(ControlType CONTROL_TYPE)
	{
		this.CONTROL_TYPE = CONTROL_TYPE;
	}

	public Set<process> getCONTROLLED()
	{
		return this.CONTROLLED;
	}

	public void setCONTROLLED(Set<process> CONTROLLED)
	{

		if (CONTROLLED == null)
		{
			CONTROLLED = new HashSet<process>();
		}
		for (process aProcess : CONTROLLED)
		{
			if (!checkCONTROLLED(aProcess))
			{
				throw new IllegalBioPAXArgumentException("Illegal argument "
					+ "Attempting to set " + aProcess + " (id:" + aProcess.getRDFId() + ") to " +
					this + " (id:" + this.getRDFId() + ")");
			}
		}
		updatePARTICIPANTS(this.CONTROLLED, this.CONTROLLED = CONTROLLED);
	}

	public void addCONTROLLED(process CONTROLLED)
	{
		if (!checkCONTROLLED(CONTROLLED))
		{
			throw new IllegalBioPAXArgumentException("Illegal argument "
				+ "Attempting to set " + CONTROLLED.getRDFId() + " to " +
				this.getRDFId());
		}

		this.CONTROLLED.add(CONTROLLED);

		CONTROLLED.isCONTROLLEDOf().add(this);
		this.setParticipantInverse(CONTROLLED, false);
	}

	public void removeCONTROLLED(process CONTROLLED)
	{
		CONTROLLED.isCONTROLLEDOf().remove(this);
		this.setParticipantInverse(CONTROLLED, true);
		this.CONTROLLED.remove(CONTROLLED);
	}

	public Set<physicalEntityParticipant> getCONTROLLER()
	{
		return CONTROLLER;
	}

	public void setCONTROLLER(Set<physicalEntityParticipant> CONTROLLER)
	{
		if (CONTROLLER == null)
		{
			CONTROLLER = new HashSet<physicalEntityParticipant>();
		}
		updatePARTICIPANTS(this.CONTROLLER, this.CONTROLLER = CONTROLLER);
	}

	public void addCONTROLLER(physicalEntityParticipant CONTROLLER)
	{
		this.CONTROLLER.add(CONTROLLER);
		setParticipantInverse(CONTROLLER, false);
	}

	public void removeCONTROLLER(physicalEntityParticipant CONTROLLER)
	{
		this.setParticipantInverse(CONTROLLER, true);
		this.CONTROLLER.remove(CONTROLLER);
	}


// -------------------------- OTHER METHODS --------------------------
	protected boolean checkCONTROLLED(process controlled)
	{
		return true;
	}

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        CompositeSet<InteractionParticipant> interactionParticipants =
                (CompositeSet<InteractionParticipant>) super.getPARTICIPANTS();
        interactionParticipants.addComposited(CONTROLLED);
        interactionParticipants.addComposited(CONTROLLER);


    }

}
