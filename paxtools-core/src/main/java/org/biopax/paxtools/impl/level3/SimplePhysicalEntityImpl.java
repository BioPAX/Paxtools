package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class SimplePhysicalEntityImpl extends PhysicalEntityImpl
		implements SimplePhysicalEntity
{
	private EntityReference entityReference;

	public SimplePhysicalEntityImpl() {
	}
	
	@ManyToOne(targetEntity = EntityReferenceImpl.class, cascade = {CascadeType.ALL})
	@IndexedEmbedded //TODO test whether if works for our model...
	protected EntityReference getEntityReferenceX()
	{
		return entityReference;
	}
	protected void setEntityReferenceX(EntityReference entityReference) {
		this.entityReference = entityReference;
	}
	
	@Transient
	public EntityReference getEntityReference()
	{
		return entityReference;
	}

	public void setEntityReference(EntityReference entityReference)
	{
		if (this.entityReference != null)
		{
			this.entityReference.getEntityReferenceOf().remove(this);
		}
		this.entityReference = entityReference;
		if (this.entityReference != null)
		{
			this.entityReference.getEntityReferenceOf().add(this);
		}
	}

	@Override
	public int equivalenceCode()
	{
		return 31 * super.locationAndFeatureCode() +
		       this.entityReference.equivalenceCode();
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		SimplePhysicalEntity that = (SimplePhysicalEntity) element;
		return ( (that.getEntityReference()!=null)
					? that.getEntityReference().isEquivalent(getEntityReference())
					: getEntityReference() == null
				) && super.semanticallyEquivalent(element);
	}
}
