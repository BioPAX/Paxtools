package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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
		left = new HashSet<PhysicalEntity>();
		right = new HashSet<PhysicalEntity>();
		participantStoichiometry = new HashSet<Stoichiometry>();
	}


// --------------------- Interface BioPAXElement ---------------------

	@Override @Transient
	public Class<? extends Conversion> getModelInterface()
	{
		return Conversion.class;
	}

// --------------------- Interface Conversion ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

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
				//return super.semanticallyEquivalent(element);
			}
		}
		return false;//todo
	}

	@Override
	public int equivalenceCode()
	{
		//todo
		return super.equivalenceCode();
	}

}
