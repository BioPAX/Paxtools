package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.paxtools.model.BioPAXElement;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_UTILILTY_CLASS)
class StoichiometryImpl extends L3ElementImpl implements Stoichiometry
{
	private float stoichiometricCoefficient = UNKNOWN_FLOAT;
	private PhysicalEntity physicalEntity;


    @Transient
	public Class<? extends Stoichiometry> getModelInterface()
	{
		return Stoichiometry.class;
	}


	@Override
	public boolean equals(Object o)
	{
		boolean value = super.equals(o);
		if (!value && o instanceof Stoichiometry)
		{
			Stoichiometry that = (Stoichiometry) o;
			value = that.getStoichiometricCoefficient() ==
			        this.getStoichiometricCoefficient() &&
			        ((that.getPhysicalEntity() == null) 
			        	? this.getPhysicalEntity() == null
			        		: that.getPhysicalEntity().equals(this.getPhysicalEntity()));

		}
		return value;
	}

	
	@Override
	public int hashCode()
	{
		return ((int) this.getStoichiometricCoefficient()) +
		       ((this.getPhysicalEntity() != null) 
		         ? 31 * this.getPhysicalEntity().hashCode()
		        	: 0);
	}
	
	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{

		boolean value = false;
		if (element instanceof Stoichiometry)
		{
			Stoichiometry that = (Stoichiometry) element;
			value = that.getStoichiometricCoefficient() == this.getStoichiometricCoefficient()
				&& ((that.getPhysicalEntity() == null) 
			        ? this.getPhysicalEntity() == null
				    : that.getPhysicalEntity().isEquivalent(this.getPhysicalEntity()));

		}
		return value;
	}

	@Override
	public int equivalenceCode()
	{
		return (int) (physicalEntity.equivalenceCode() + 23 * stoichiometricCoefficient);
	}

	@ManyToOne(targetEntity = PhysicalEntityImpl.class)
    public PhysicalEntity getPhysicalEntity()
	{
		return physicalEntity;
	}

	public void setPhysicalEntity(PhysicalEntity PhysicalEntity)
	{
		this.physicalEntity = PhysicalEntity;
	}

    @Basic
	public float getStoichiometricCoefficient()
	{
		return stoichiometricCoefficient;
	}

	public void setStoichiometricCoefficient(float newStoichiometricCoefficient)
	{
		stoichiometricCoefficient = newStoichiometricCoefficient;
	}
}
