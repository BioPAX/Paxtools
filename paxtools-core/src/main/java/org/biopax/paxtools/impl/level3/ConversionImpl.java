package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.SetEquivalenceChecker;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.Set;

@Entity
@Proxy(proxyClass= Conversion.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
		left = BPCollections.createSafeSet();
		right = BPCollections.createSafeSet();
		participantStoichiometry = BPCollections.createSafeSet();
	}


// --------------------- Interface BioPAXElement ---------------------

	@Override @Transient
	public Class<? extends Conversion> getModelInterface()
	{
		return Conversion.class;
	}

// --------------------- Interface Conversion ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PhysicalEntityImpl.class)
	@JoinTable(name="rightParticipant")
	public Set<PhysicalEntity> getRight()
	{
		return right;
	}

	protected void setRight(Set<PhysicalEntity> right)
	{
		this.right= right;
	}

	public void addRight(PhysicalEntity right)
	{
		if(right != null) {
			this.right.add(right);
			super.addParticipant(right);
		}
	}

	public void removeRight(PhysicalEntity right)
	{
		if(right != null) {
			super.removeParticipant(right);
			this.right.remove(right);
		}
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PhysicalEntityImpl.class)
	@JoinTable(name="leftParticipant")
	public Set<PhysicalEntity> getLeft()
	{
		return left;
	}

	protected void setLeft(Set<PhysicalEntity> left)
	{
		this.left = left;
	}

	public void addLeft(PhysicalEntity left)
	{
		if(left != null) {
			this.left.add(left);
			super.addParticipant(left);
		}
	}

	public void removeLeft(PhysicalEntity left)
	{
		if(left != null) {
			super.removeParticipant(left);
			this.left.remove(left);
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

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = StoichiometryImpl.class)
	@JoinTable(name="conversionstoichiometry")		
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

	protected void setParticipantStoichiometry(
			Set<Stoichiometry> participantStoichiometry)
	{
		this.participantStoichiometry = participantStoichiometry;
	}


	@Enumerated
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
				if(SetEquivalenceChecker.isEquivalent(this.getLeft(), that.getLeft()))
				{
					return SetEquivalenceChecker.isEquivalent(this.getRight(), that.getRight());
				}
				else if(SetEquivalenceChecker.isEquivalent(this.getLeft(), that.getRight()))
				{
					return(SetEquivalenceChecker.isEquivalent(this.getRight(), that.getLeft()));
				}
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
