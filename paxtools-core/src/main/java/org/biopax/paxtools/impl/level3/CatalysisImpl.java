package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;

import java.util.HashSet;
import java.util.Set;

/**
 */
class CatalysisImpl extends ControlImpl implements Catalysis
{
// ------------------------------ FIELDS ------------------------------

	private CatalysisDirectionType catalysisDirection;
	private Set<PhysicalEntity> cofactor;

// --------------------------- CONSTRUCTORS ---------------------------

	public CatalysisImpl()
	{
		this.cofactor = new HashSet<PhysicalEntity>();
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

	public void setCofactor(Set<PhysicalEntity> cofactor)
	{
		if (cofactor == null)
		{
			cofactor = new HashSet<PhysicalEntity>();
		}
		this.cofactor = cofactor;
	}

	public void addCofactor(PhysicalEntity cofactor)
	{
		this.cofactor.add(cofactor);
		addSubParticipant(cofactor);
	}

	public void removeCofactor(PhysicalEntity cofactor)
	{
		removeSubParticipant(cofactor);
        this.cofactor.remove(cofactor);
	}


	protected boolean checkControlled(Process controlled)
	{
		return controlled instanceof Conversion;
	}
}
