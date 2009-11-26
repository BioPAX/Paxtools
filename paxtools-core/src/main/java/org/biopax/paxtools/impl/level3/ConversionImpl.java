package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;

import java.util.HashSet;
import java.util.Set;

/**
 */
class ConversionImpl extends RestrictedInteractionAdapter
		implements Conversion
{
// ------------------------------ FIELDS ------------------------------

	private Set<PhysicalEntity> right;
	private Set<PhysicalEntity> left;

	private ConversionDirectionType conversionDirection;
	private Set<Stoichiometry> participantStoichiometry;
	private Boolean spontaneous;

// --------------------------- CONSTRUCTORS ---------------------------

	public ConversionImpl()
	{
		this.left = new HashSet<PhysicalEntity>();
		this.right = new HashSet<PhysicalEntity>();
		participantStoichiometry = new HashSet<Stoichiometry>();
	}


// --------------------- Interface BioPAXElement ---------------------

	@Override
	public Class<? extends Conversion> getModelInterface()
	{
		return Conversion.class;
	}

// --------------------- Interface Conversion ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<PhysicalEntity> getRight()
	{
		return right;
	}

	public void setRight(Set<PhysicalEntity> right)
	{
		if (right == null)
		{
			right = new HashSet<PhysicalEntity>();
		}
		this.right = right;
	}

	public Boolean getSpontaneous()
	{
		return spontaneous;
	}

	public void setSpontaneous(Boolean spontaneous)
	{
		this.spontaneous = spontaneous;
	}

	public void addRight(PhysicalEntity right)
	{
		this.right.add(right);
		addSubParticipant(right);
	}

	public void removeRight(PhysicalEntity right)
	{
		removeSubParticipant(right);
		this.right.remove(right);
	}

	public Set<PhysicalEntity> getLeft()
	{
		return left;
	}

	public void setLeft(Set<PhysicalEntity> left)
	{
		if (left == null)
		{
			left = new HashSet<PhysicalEntity>();
		}

		this.left = left;
	}

	public void addLeft(PhysicalEntity left)
	{
		this.left.add(left);
		addSubParticipant(left);
	}

	public void removeLeft(PhysicalEntity left)
	{
		removeSubParticipant(left);
		this.left.remove(left);
	}

	public Set<Stoichiometry> getParticipantStoichiometry()
	{
		return participantStoichiometry;
	}

	public void addParticipantStoichiometry(
			Stoichiometry participantStoichiometry)
	{
		this.participantStoichiometry.add(participantStoichiometry);
	}

	public void removeParticipantStoichiometry(
			Stoichiometry participantStoichiometry)
	{
		this.participantStoichiometry.remove(participantStoichiometry);
	}

	public void setParticipantStoichiometry(
			Set<Stoichiometry> participantStoichiometry)
	{
		this.participantStoichiometry = participantStoichiometry;
	}


	public ConversionDirectionType getConversionDirection()
	{
		return conversionDirection;
	}

	public void setConversionDirection(ConversionDirectionType spontanousType)
	{
		this.conversionDirection = spontanousType;
	}


	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if(element.getModelInterface()== this.getModelInterface())
		{
			Conversion that = (Conversion) element;
			if(that.getSpontaneous()==this.getSpontaneous() &&
		       that.getConversionDirection() == this.getConversionDirection())
			{
				//return super.semanticallyEquivalent(element);
			}
		}
		return false;//todo
	}

	@Override
	public int equivalenceCode()
	{
		//todo
		return super
				.equivalenceCode();    //To change body of overridden methods use File | Settings | File Templates.
	}

}
