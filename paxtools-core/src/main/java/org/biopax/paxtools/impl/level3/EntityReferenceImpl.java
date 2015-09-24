package org.biopax.paxtools.impl.level3;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


public abstract class EntityReferenceImpl extends NamedImpl
		implements EntityReference
{
	private final static Log log = LogFactory.getLog(EntityReferenceImpl.class);
	
	private Set<EntityFeature> entityFeature;
	private Set<SimplePhysicalEntity> entityReferenceOf;
	private Set<Evidence> evidence;
	private Set<EntityReferenceTypeVocabulary> entityReferenceType;
	private Set<EntityReference> memberEntityReference;
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

	public Class<? extends EntityReference> getModelInterface()
	{
		return EntityReference.class;
	}

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
				//we neither fix nor fail here (currently, biopax-validator detects and optionally fixes it).
				log.warn("addEntityFeature: violated the inverse-functional OWL constraint; to fix, "
					+ entityFeature.getModelInterface().getSimpleName() + " " + entityFeature.getUri()
					+ " should be REMOVED from " 
					+ eFof.getModelInterface().getSimpleName() + " " + eFof.getUri());
				//TODO use eFof.removeEntityFeature(entityFeature) or throw an exception...
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
				//Don't set entityFeatureOf to null here 
				//(looks, this EF was previously moved to another ER)
				log.warn("removeEntityFeature: removed " 
					+ entityFeature.getModelInterface().getSimpleName() + " " + entityFeature.getUri()
					+ " from " + getModelInterface().getSimpleName() + " " + getUri()
					+ "; though entityFeatureOf was another " 
					+ entityFeature.getEntityFeatureOf().getModelInterface().getSimpleName() 
					+ " " + entityFeature.getEntityFeatureOf().getUri());
			} else {
				log.warn("removeEntityFeature: removed " 
					+ entityFeature.getModelInterface().getSimpleName() + " " + entityFeature.getUri()
					+ " from " + getModelInterface().getSimpleName() + " " + getUri()
					+ ", but entityFeatureOf was already NULL (illegal state)");
			}
		} else {
			log.warn("removeEntityFeature: did nothing, because "
					+ getUri() + " does not contain feature " + entityFeature.getUri());
		}
	}

	public Set<SimplePhysicalEntity> getEntityReferenceOf()
	{
		return entityReferenceOf;
	}

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

	public Set<EntityReference> getMemberEntityReferenceOf()
	{
		return ownerEntityReference;
	}

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

}
