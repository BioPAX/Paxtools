package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.util.BPCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public abstract class SimplePhysicalEntityImpl extends PhysicalEntityImpl
		implements SimplePhysicalEntity
{
	private EntityReference entityReference;

  	private static final Logger log = LoggerFactory.getLogger(SimplePhysicalEntityImpl.class);

	public SimplePhysicalEntityImpl() {
	}

	public Set<EntityReference> getGenericEntityReferences()
	{
		Set<EntityReference> ger = BPCollections.I.createSet();
		EntityReference er = this.getEntityReference();
		if(er!=null)
		{
			ger.add(er);
			ger.addAll(er.getMemberEntityReference());
		}
		for (PhysicalEntity pe : this.getMemberPhysicalEntity())
		{
			if(pe instanceof SimplePhysicalEntity)
			ger.addAll(((SimplePhysicalEntity) pe).getGenericEntityReferences());
			else
				log.error("Member PE is of different class! Skipping..");
		}
		return ger;
	}

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
        return this.entityReference==null? hashCode():31 * super.locationAndFeatureCode() +
		       entityReference.equivalenceCode();
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if(!(element instanceof SimplePhysicalEntity))
			return false;
		
		SimplePhysicalEntity that = (SimplePhysicalEntity) element;
		return ( (that.getEntityReference()!=null)
					? that.getEntityReference().isEquivalent(getEntityReference())
					: getEntityReference() == null
				) && super.semanticallyEquivalent(element);
	}
}
