package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.util.ChildDataStringBridge;
import org.biopax.paxtools.util.OrganismFieldBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@Entity
@Proxy(proxyClass= SimplePhysicalEntity.class)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class SimplePhysicalEntityImpl extends PhysicalEntityImpl
		implements SimplePhysicalEntity
{
	private EntityReference entityReference;
  	Log log = LogFactory.getLog(SimplePhysicalEntityImpl.class);
	public SimplePhysicalEntityImpl() {
	}
	
	@Fields({
		@Field(name="organism", index=Index.UN_TOKENIZED, bridge= @FieldBridge(impl = OrganismFieldBridge.class)),
		@Field(name="data", index=Index.TOKENIZED, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	})
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToOne(targetEntity = EntityReferenceImpl.class)
	public EntityReference getEntityReferenceX()
	{
		return entityReference;
	}
	public void setEntityReferenceX(EntityReference entityReference) {
		this.entityReference = entityReference;
	}

	@Transient
	public Set<EntityReference> getGenericEntityReferences()
	{
		Set<EntityReference> ger = new HashSet<EntityReference>();
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
		if(!(element instanceof SimplePhysicalEntity))
			return false;
		
		SimplePhysicalEntity that = (SimplePhysicalEntity) element;
		return ( (that.getEntityReference()!=null)
					? that.getEntityReference().isEquivalent(getEntityReference())
					: getEntityReference() == null
				) && super.semanticallyEquivalent(element);
	}
}
