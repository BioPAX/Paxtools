package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.SetEquivalenceChecker;

import java.util.Set;


public class ConversionImpl extends InteractionImpl
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
		left = BPCollections.I.createSafeSet();
		right = BPCollections.I.createSafeSet();
		participantStoichiometry = BPCollections.I.createSafeSet();
	}


// --------------------- Interface BioPAXElement ---------------------

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

	public void addRight(PhysicalEntity right)
	{
		if(right != null) {
			if(this.right.add(right)) {
				super.addParticipant(right);
			}
		}
	}

	public void removeRight(PhysicalEntity right)
	{
		if(right != null) {
			this.right.remove(right);
			super.removeParticipant(right);
		}
	}

	public Set<PhysicalEntity> getLeft()
	{
		return left;
	}

	public void addLeft(PhysicalEntity left)
	{
		if(left != null) {
			if(this.left.add(left)) {
				super.addParticipant(left);
			}
		}
	}

	public void removeLeft(PhysicalEntity left)
	{
		if(left != null) {
			this.left.remove(left);
			super.removeParticipant(left);
		}
	}
	
	public Boolean getSpontaneous()
	{
		return spontaneous;
	}

	public void setSpontaneous(Boolean spontaneous)
	{
		this.spontaneous = spontaneous;
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
		if(element.getModelInterface() == this.getModelInterface())
		{
			Conversion that = (Conversion) element;
			if(that.getSpontaneous()==this.getSpontaneous() &&
		       that.getConversionDirection() == this.getConversionDirection())//TODO: what if one is REVERSIBLE or opposite?
			{
				return SetEquivalenceChecker.isEquivalent(this.getLeft(), that.getLeft())
						&& SetEquivalenceChecker.isEquivalent(this.getRight(), that.getRight())
					|| SetEquivalenceChecker.isEquivalent(this.getLeft(), that.getRight())
						&& SetEquivalenceChecker.isEquivalent(this.getRight(), that.getLeft());
				//TODO: Why don't we call super.semanticallyEquivalent here (to check xrefs, evidence)?
			}
		}
		return false;
	}

	@Override
	public int equivalenceCode()
	{
		return getEqCodeForSet(this.getLeft())*getEqCodeForSet(this.getRight());
	}

	private int getEqCodeForSet(Set<PhysicalEntity> peSet)
	{
		int eqCode=0;
		for (PhysicalEntity pe : peSet)
		{
			eqCode+=pe.equivalenceCode();
		}
		return eqCode;
	}

}
