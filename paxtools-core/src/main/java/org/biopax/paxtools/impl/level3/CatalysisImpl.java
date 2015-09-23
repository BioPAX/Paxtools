package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;


public class CatalysisImpl extends ControlImpl implements Catalysis
{
// ------------------------------ FIELDS ------------------------------

	private CatalysisDirectionType catalysisDirection;

	private Set<PhysicalEntity> cofactor;

// --------------------------- CONSTRUCTORS ---------------------------

	public CatalysisImpl()
	{
		this.cofactor = BPCollections.I.createSafeSet();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends Catalysis> getModelInterface()
	{
		return Catalysis.class;
	}

// --------------------- Interface catalysis ---------------------

// --------------------- ACCESORS and MUTATORS---------------------
	public CatalysisDirectionType getCatalysisDirection()
	{
		return catalysisDirection;
	}

	public void setCatalysisDirection(CatalysisDirectionType catalysisDirection)
	{
		this.catalysisDirection = catalysisDirection;
	}

	public Set<PhysicalEntity> getCofactor()
	{
		return cofactor;
	}

	public void addCofactor(PhysicalEntity cofactor)
	{
		if (cofactor != null)
		{
			this.cofactor.add(cofactor);
			super.addParticipant(cofactor);
		}
	}

	public void removeCofactor(PhysicalEntity cofactor)
	{
		if (cofactor != null)
		{
			super.removeParticipant(cofactor);
			this.cofactor.remove(cofactor);
		}
	}


	@Override public void addController(Controller controller)
	{
		if (controller instanceof PhysicalEntity) super.addController(controller);
		else throw new IllegalBioPAXArgumentException("Catalysis can only be controlled with a Physical Entity");

	}

	protected boolean checkControlled(Process controlled)
	{
		return controlled instanceof Conversion;
	}
}
