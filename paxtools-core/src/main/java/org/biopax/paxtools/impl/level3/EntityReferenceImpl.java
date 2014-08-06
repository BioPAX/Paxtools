package org.biopax.paxtools.impl.level3;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BPCollections;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.Set;

@Entity
@Proxy(proxyClass= EntityReference.class)
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class EntityReferenceImpl extends NamedImpl
		implements EntityReference
{
	private final Log log = LogFactory.getLog(EntityReferenceImpl.class);
	
	private Set<EntityFeature> entityFeature;
	private Set<SimplePhysicalEntity> entityReferenceOf;
	private Set<Evidence> evidence;
	Set<EntityReferenceTypeVocabulary> entityReferenceType;
	Set<EntityReference> memberEntityReference;
	private Set<EntityReference> ownerEntityReference;

	/**
	 * Constructor.
	 */
	public EntityReferenceImpl()
	{
		this.entityFeature = BPCollections.I.createSafeSet();
		this.entityReferenceOf = BPCollections.I.createSafeSet();
		this.evidence = BPCollections.I.createSafeSet();
		this.entityReferenceType = BPCollections.I.createSafeSet();
		this.memberEntityReference = BPCollections.I.createSafeSet();
		this.ownerEntityReference= BPCollections.I.createSafeSet();
	}

	@Transient
	public Class<? extends EntityReference> getModelInterface()
	{
		return EntityReference.class;
	}

	/**
	 * The contents of this set should NOT be modified. For manipulating the contents use addNew and
	 * remove instead.
	 *
	 * @return A set of entity features for the reference entity.
	 */
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(targetEntity = EntityFeatureImpl.class, mappedBy = "entityFeatureOf")
	public Set<EntityFeature> getEntityFeature()
	{
		return entityFeature;
	}

	public void addEntityFeature(EntityFeature entityFeature)
	{
		if (entityFeature != null)
		{
			EntityReference eFof = entityFeature.getEntityFeatureOf();
			
			if (eFof != null && !eFof.equals(this))
			{
				//log, then do anyway				
				log.error("addEntityFeature: moved " 
						+ entityFeature.getModelInterface().getSimpleName() 
						+ " " + entityFeature.getRDFId() + " to "
						+ getModelInterface().getSimpleName() + " " + getRDFId()
						+ " from " + eFof.getModelInterface().getSimpleName() + " " + eFof.getRDFId() 
						+ ", but it's still owned by the latter (one should remove or copy the feature first).");		
			} 

			((EntityFeatureImpl) entityFeature).setEntityFeatureOf(this);	
			this.entityFeature.add(entityFeature);
		}
	}


	public void removeEntityFeature(EntityFeature entityFeature)
	{
		if (this.entityFeature.contains(entityFeature))
		{
			this.entityFeature.remove(entityFeature);
			
			if(entityFeature.getEntityFeatureOf() == this) {
				((EntityFeatureImpl) entityFeature).setEntityFeatureOf(null);
			} else if(entityFeature.getEntityFeatureOf() != null) {
				//won't call entityFeature.setEntityFeatureOf(null) 
				log.warn("Removed entityFeature value: " + entityFeature.getRDFId() 
					+ ", which is still entityFeatureOf " + entityFeature.getEntityFeatureOf().getRDFId());
			} else {
				log.warn("Removed entityFeature value: " + entityFeature.getRDFId() 
					+ ", which already had entityFeatureOf == null (was illegal state)");
			}
		} else {
			log.warn("removeEntityFeature did nothing: entityFeature property of "
					+ this.getRDFId() + " does not contain the (EF): " 
					+ ((entityFeature!=null)?entityFeature.getRDFId():null));
		}
	}

	
	protected void setEntityFeature(Set<EntityFeature> entityFeature)
	{
		this.entityFeature = entityFeature;
	}


	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(targetEntity= SimplePhysicalEntityImpl.class, mappedBy = "entityReferenceX")
	public Set<SimplePhysicalEntity> getEntityReferenceOf()
	{
		return entityReferenceOf;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EntityReferenceTypeVocabularyImpl.class)
	@JoinTable(name="entityReferenceType")
	public Set<EntityReferenceTypeVocabulary> getEntityReferenceType()
	{
		return entityReferenceType;
	}

	public void addEntityReferenceType(
			EntityReferenceTypeVocabulary entityReferenceType)
	{
		if(entityReferenceType != null)
			this.entityReferenceType.add(entityReferenceType);
	}

	public void removeEntityReferenceType(
			EntityReferenceTypeVocabulary entityReferenceType)
	{
		if(entityReferenceType != null)
			this.entityReferenceType.remove(entityReferenceType);
	}

	protected void setEntityReferenceType(
			Set<EntityReferenceTypeVocabulary> entityReferenceType)
	{
		this.entityReferenceType=entityReferenceType;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EntityReferenceImpl.class) //TODO generify?
	@JoinTable(name="memberEntityReference")
	public Set<EntityReference> getMemberEntityReference()
	{
		return memberEntityReference;
	}

	public void addMemberEntityReference(EntityReference memberEntity)
	{
		if (memberEntity != null) {
			this.memberEntityReference.add(memberEntity);
			memberEntity.getMemberEntityReferenceOf().add(this);
		}
	}

	public void removeMemberEntityReference(EntityReference memberEntity)
	{
		if (memberEntity != null) {
			this.memberEntityReference.remove(memberEntity);
			memberEntity.getMemberEntityReferenceOf().remove(this);
		}
	}

	public void setMemberEntityReference(Set<EntityReference> memberEntity)
	{
		this.memberEntityReference = memberEntity;

	}


    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EntityReferenceImpl.class, mappedBy = "memberEntityReference")
	public Set<EntityReference> getMemberEntityReferenceOf()
	{
		return ownerEntityReference;
	}

	protected  void setMemberEntityReferenceOf(Set<EntityReference> newOwnerEntityReferenceSet)
	{
		this.ownerEntityReference = newOwnerEntityReferenceSet;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EvidenceImpl.class)
	@JoinTable(name="evidence")	
	public Set<Evidence> getEvidence()
	{
		return evidence;
	}

	public void addEvidence(Evidence evidence)
	{
		if(evidence != null)
			this.evidence.add(evidence);
	}

	public void removeEvidence(Evidence evidence)
	{
		if(evidence != null)
			this.evidence.remove(evidence);
	}

	public void setEvidence(Set<Evidence> evidence)
	{
		this.evidence = evidence;
	}

	protected void setEntityReferenceOf(Set<SimplePhysicalEntity> entityReferenceOf)
	{
		this.entityReferenceOf = entityReferenceOf;
	}

	protected void setMemberEntity(Set<EntityReference> memberEntity)
	{
		this.memberEntityReference = memberEntity;
	}

}
