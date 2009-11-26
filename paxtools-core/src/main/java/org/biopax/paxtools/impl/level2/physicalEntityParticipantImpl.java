package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.HashSet;
import java.util.Set;

/**
 */
class physicalEntityParticipantImpl extends BioPAXLevel2ElementImpl
	implements physicalEntityParticipant
{
// ------------------------------ FIELDS ------------------------------

	private openControlledVocabulary CELLULAR_LOCATION;
	private double STOICHIOMETRIC_COEFFICIENT = BioPAXElement.UNKNOWN_DOUBLE;
	private physicalEntity PHYSICAL_ENTITY;
	private Set<interaction> PARTICIPANTSof = new HashSet<interaction>();
	private complex COMPONENTSof;


// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface BioPAXElement ---------------------


	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		final physicalEntityParticipant that =
			(physicalEntityParticipant) element;
		return isInEquivalentState(that) &&
			isInEquivalentContext(that);
	}

	private boolean isInEquivalentContext(physicalEntityParticipant that)
	{
		return that.getSTOICHIOMETRIC_COEFFICIENT() ==
			STOICHIOMETRIC_COEFFICIENT
			&& PARTICIPANTSof.iterator().next()
			.isEquivalent(that.isPARTICIPANTSof().iterator().next());    //TODO fix
	}

	public boolean isInEquivalentState(physicalEntityParticipant that)
	{
		return
			(PHYSICAL_ENTITY == null ?
				that.getPHYSICAL_ENTITY() == null:
				PHYSICAL_ENTITY.equals(that.getPHYSICAL_ENTITY()))
				&&
				(CELLULAR_LOCATION == null ?
					that.getCELLULAR_LOCATION() == null
					:
					CELLULAR_LOCATION.isEquivalent(that.getCELLULAR_LOCATION()))
				&&
				(COMPONENTSof == null ?
					that.isCOMPONENTof() == null :
					COMPONENTSof.equals(that.isCOMPONENTof()));
	}

	public int stateCode()
	{
		return 29 * PHYSICAL_ENTITY.equivalenceCode()
			+ (CELLULAR_LOCATION == null ? 0 : CELLULAR_LOCATION.equivalenceCode());
	}

	public int equivalenceCode()
	{
		int result = 29 * this.stateCode();
		long temp = STOICHIOMETRIC_COEFFICIENT != +0.0d ?
			Double.doubleToLongBits(STOICHIOMETRIC_COEFFICIENT) : 0L;
		return 29 * result + (int) (temp ^ (temp >>> 32))
			+ PARTICIPANTSof.iterator().next().equivalenceCode();
	}



	public Class<? extends BioPAXElement> getModelInterface()
	{
		return physicalEntityParticipant.class;
	}

// --------------------- Interface InteractionParticipant ---------------------


	public Set<interaction> isPARTICIPANTSof()
	{
		return PARTICIPANTSof;
	}

// --------------------- Interface physicalEntityParticipant ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public openControlledVocabulary getCELLULAR_LOCATION()
	{
		return CELLULAR_LOCATION;
	}

	public void setCELLULAR_LOCATION(openControlledVocabulary CELLULAR_LOCATION)
	{
		this.CELLULAR_LOCATION = CELLULAR_LOCATION;
	}

	public double getSTOICHIOMETRIC_COEFFICIENT()
	{
		return STOICHIOMETRIC_COEFFICIENT;
	}

	public void setSTOICHIOMETRIC_COEFFICIENT(double STOICHIOMETRIC_COEFFICIENT)
	{
		this.STOICHIOMETRIC_COEFFICIENT = STOICHIOMETRIC_COEFFICIENT;
	}

	public physicalEntity getPHYSICAL_ENTITY()
	{
		return PHYSICAL_ENTITY;
	}

	public void setPHYSICAL_ENTITY(physicalEntity PHYSICAL_ENTITY)
	{
		if(this.PHYSICAL_ENTITY!=null)
		{
			this.PHYSICAL_ENTITY.removePHYSICAL_ENTITYof(this);
		}
		this.PHYSICAL_ENTITY = PHYSICAL_ENTITY;
		if (PHYSICAL_ENTITY != null)
		{
			PHYSICAL_ENTITY.addPHYSICAL_ENTITYof(this);
		}
	}

	public complex isCOMPONENTof()
	{
		return COMPONENTSof;
	}

	public void setCOMPONENTSof(complex aComplex)
	{
		if (aComplex != null)
		{
			if (!((this.COMPONENTSof == null ||
				this.COMPONENTSof == aComplex) &&
				this.PARTICIPANTSof.isEmpty() &&
				aComplex.getCOMPONENTS().contains(this)))
			{
				throw new IllegalBioPAXArgumentException(
					"Illegal attempt to set the inverse link!");
			}
		}
		this.COMPONENTSof = aComplex;
	}

// -------------------------- OTHER METHODS --------------------------


}
