package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BidirectionalLinkViolationException;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@Entity
abstract class EntityReferenceImpl extends NamedImpl
		implements EntityReference
{

	private Set<EntityFeature> entityFeature;
	private HashSet<SimplePhysicalEntity> simplePhysicalEntity;
	private Set<Evidence> evidence;
	Set<EntityReferenceTypeVocabulary> entityReferenceType;
	Set<EntityReference> memberEntity;
	private Set<EntityReference> ownerEntityReference;

	/**
	 * Constructor.
	 */
	EntityReferenceImpl()
	{
		this.entityFeature = new HashSet<EntityFeature>();
		this.simplePhysicalEntity = new HashSet<SimplePhysicalEntity>();
		this.evidence = new HashSet<Evidence>();
		this.entityReferenceType = new HashSet<EntityReferenceTypeVocabulary>();
		this.memberEntity = new HashSet<EntityReference>();
		this.ownerEntityReference= new HashSet<EntityReference>();
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
			if (eFof != null)
			{
				if (!eFof.equals(this))
				{
					throw new BidirectionalLinkViolationException(this,
							entityFeature);
				}
			}
			else
			{
				((EntityFeatureImpl) entityFeature).setEntityFeatureOf(this); //todo
			}
			this.entityFeature.add(entityFeature);
		}
	}


	public void removeEntityFeature(EntityFeature entityFeature)
	{
		if (entityFeature != null)
		{
			assert entityFeature.getEntityFeatureOf() == this;
			this.entityFeature.remove(entityFeature);
			((EntityFeatureImpl) entityFeature).setEntityFeatureOf(null); //todo
		}
	}

	protected void setEntityFeature(Set<EntityFeature> entityFeature)
	{
		this.entityFeature = entityFeature;
	}

	@OneToMany(targetEntity= SimplePhysicalEntityImpl.class, mappedBy = "entityReference")
	public Set<SimplePhysicalEntity> getEntityReferenceOf()
	{
		return simplePhysicalEntity;
	}

	public Set<EntityReferenceTypeVocabulary> getEntityReferenceType()
	{
		return entityReferenceType;
	}

	public void addEntityReferenceType(
			EntityReferenceTypeVocabulary entityReferenceType)
	{
		this.entityReferenceType.add(entityReferenceType);
	}

	public void removeEntityReferenceType(
			EntityReferenceTypeVocabulary entityReferenceType)
	{
		this.entityReferenceType.remove(entityReferenceType);
	}

	protected void setEntityReferenceType(
			Set<EntityReferenceTypeVocabulary> entityReferenceType)
	{
		this.entityReferenceType=entityReferenceType;
	}

	@ManyToMany(targetEntity = EntityReferenceImpl.class) //todo generify?
	public Set<EntityReference> getMemberEntityReference()
	{
		return memberEntity;
	}

	public void addMemberEntityReference(EntityReference memberEntity)
	{
		this.memberEntity.add(memberEntity);
		memberEntity.getMemberEntityReferenceOf().add(this);
	}

	public void removeMemberEntityReference(EntityReference memberEntity)
	{
		this.memberEntity.remove(memberEntity);
		memberEntity.getMemberEntityReferenceOf().remove(this);
	}

	public void setMemberEntityReference(Set<EntityReference> memberEntity)
	{
		this.memberEntity = memberEntity;

	}

	@ManyToMany(targetEntity = EntityReferenceImpl.class, mappedBy = "memberEntityReference")
	public Set<EntityReference> getMemberEntityReferenceOf()
	{
		return ownerEntityReference;
	}

	protected  void setMemberEntityReferenceOf(Set<EntityReference> newOwnerEntityReferenceSet)
	{
		this.ownerEntityReference = newOwnerEntityReferenceSet;
	}

	//
	// observable interface implementation
	//
	/////////////////////////////////////////////////////////////////////////////

	@ManyToMany(targetEntity = EvidenceImpl.class)
	public Set<Evidence> getEvidence()
	{
		return evidence;
	}

	public void addEvidence(Evidence evidence)
	{
		this.evidence.add(evidence);
	}

	public void removeEvidence(Evidence evidence)
	{
		this.evidence.remove(evidence);
	}

	public void setEvidence(Set<Evidence> evidence)
	{
		this.evidence = evidence;
	}

}
