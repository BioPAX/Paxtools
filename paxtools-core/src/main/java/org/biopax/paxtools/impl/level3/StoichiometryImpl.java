package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Indexed//(index = BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class StoichiometryImpl extends L3ElementImpl implements Stoichiometry
{
	private float stoichiometricCoefficient = UNKNOWN_FLOAT;
	private PhysicalEntity physicalEntity;


	public StoichiometryImpl()
	{
	}

	@Transient
	public Class<? extends Stoichiometry> getModelInterface()
	{
		return Stoichiometry.class;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{

		boolean value = false;
		if (!value && element instanceof Stoichiometry)
		{
			Stoichiometry that = (Stoichiometry) element;
			if (that.getPhysicalEntity() != null && this.getPhysicalEntity() != null)
			{
				value = (that.getStoichiometricCoefficient() ==
				         this.getStoichiometricCoefficient()) &&
				        that.getPhysicalEntity().equals(this.getPhysicalEntity());

			}
		}
		return value;
	}

	@Override
	public int equivalenceCode()
	{
		return ((int) this.getStoichiometricCoefficient()) +
	       ((this.getPhysicalEntity() != null)
	        ? 31 * this.getPhysicalEntity().hashCode()
	        : 0);
	}

	@ManyToOne(targetEntity = PhysicalEntityImpl.class)//, cascade = {CascadeType.ALL})
	public PhysicalEntity getPhysicalEntity()
	{
		return physicalEntity;
	}

	public void setPhysicalEntity(PhysicalEntity PhysicalEntity)
	{
		this.physicalEntity = PhysicalEntity;
	}

	
	public float getStoichiometricCoefficient()
	{
		return stoichiometricCoefficient;
	}

	public void setStoichiometricCoefficient(float newStoichiometricCoefficient)
	{
		stoichiometricCoefficient = newStoichiometricCoefficient;
	}
}
