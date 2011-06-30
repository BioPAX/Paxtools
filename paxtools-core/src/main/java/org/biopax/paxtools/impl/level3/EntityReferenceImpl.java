package org.biopax.paxtools.impl.level3;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.SetEquivalanceChecker;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class EntityReferenceImpl extends NamedImpl
		implements EntityReference
{
	private final Log log = LogFactory.getLog(EntityReferenceImpl.class);
	
	private Set<EntityFeature> entityFeature;
	private Set<SimplePhysicalEntity> entityReferenceOf;
	private Set<Evidence> evidence;
	Set<EntityReferenceTypeVocabulary> entityReferenceType;
	Set<EntityReference> memberEntity;
	private Set<EntityReference> ownerEntityReference;

	/**
	 * Constructor.
	 */
	public EntityReferenceImpl()
	{
		this.entityFeature = new HashSet<EntityFeature>();
		this.entityReferenceOf = new HashSet<SimplePhysicalEntity>();
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
	@OneToMany(targetEntity = EntityFeatureImpl.class, 
			mappedBy = "entityFeatureXOf")//, cascade={CascadeType.ALL})
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
				//throw new BidirectionalLinkViolationException(this, entityFeature);
				log.warn("addEntityFeature: adding (to this "
					+ getModelInterface().getSimpleName() +
					" " + getRDFId() + ") a "
					+ entityFeature.getModelInterface().getSimpleName()
					+ " " + entityFeature.getRDFId()
					+ " that is already owned by another "
					+ eFof.getModelInterface().getSimpleName()
					+ " " + eFof.getRDFId());
			}

			((EntityFeatureImpl) entityFeature).setEntityFeatureOf(this); //todo (what?)
			
			this.entityFeature.add(entityFeature);
		}
	}


	public void removeEntityFeature(EntityFeature entityFeature)
	{
		if (entityFeature != null)
		{
			assert entityFeature.getEntityFeatureOf() == this
				: "attempt to remove not own EntityFeature!"; //- but the assertion alone is not enough...
			if(entityFeature.getEntityFeatureOf() == this) {
				this.entityFeature.remove(entityFeature);
				((EntityFeatureImpl) entityFeature).setEntityFeatureOf(null); //todo
			} 
		}
	}

	protected void setEntityFeature(Set<EntityFeature> entityFeature)
	{
		this.entityFeature = entityFeature;
	}

	@OneToMany(targetEntity= SimplePhysicalEntityImpl.class, mappedBy = "entityReferenceX")
	public Set<SimplePhysicalEntity> getEntityReferenceOf()
	{
		return entityReferenceOf;
	}

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

	@ManyToMany(targetEntity = EntityReferenceImpl.class) //TODO generify?
	@JoinTable(name="memberEntityReference")
	public Set<EntityReference> getMemberEntityReference()
	{
		return memberEntity;
	}

	public void addMemberEntityReference(EntityReference memberEntity)
	{
		if (memberEntity != null) {
			this.memberEntity.add(memberEntity);
			memberEntity.getMemberEntityReferenceOf().add(this);
		}
	}

	public void removeMemberEntityReference(EntityReference memberEntity)
	{
		if (memberEntity != null) {
			this.memberEntity.remove(memberEntity);
			memberEntity.getMemberEntityReferenceOf().remove(this);
		}
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
		this.memberEntity = memberEntity;
	}

	
    @Override
    protected boolean semanticallyEquivalent(BioPAXElement element) {
    	if(!(element instanceof EntityReference)) return false;
    	EntityReference that = (EntityReference) element;
    	return  SetEquivalanceChecker.isEquivalent(this.getMemberEntityReference(), that.getMemberEntityReference())
			&& super.semanticallyEquivalent(element);
    }
}
