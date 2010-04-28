package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.SetEquivalanceChecker;
import org.biopax.paxtools.util.BidirectionalLinkViolationException;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

class EntityFeatureImpl extends L3ElementImpl implements EntityFeature
{

	private Set<Evidence> evidence;

	private EntityReference ownerEntityReference;
	private Set<PhysicalEntity> featuredEntities;
	private Set<PhysicalEntity> notFeaturedEntities;
	private SequenceLocation featureLocation;
	private Set<EntityFeature> memberFeature;
	private SequenceRegionVocabulary featureLocationType;
	private Set<EntityFeature> memberFeatureOf;


	@javax.persistence.Entity
	public EntityFeatureImpl()
	{
		evidence = new HashSet<Evidence>();
		featuredEntities = new HashSet<PhysicalEntity>();
		notFeaturedEntities = new HashSet<PhysicalEntity>();
		this.memberFeatureOf = new HashSet<EntityFeature>();
		this.memberFeature = new HashSet<EntityFeature>();
	}


	@Transient
	public Class<? extends EntityFeature> getModelInterface()
	{
		return EntityFeature.class;
	}


	/**
	 * @return Reference entity that this feature belongs to.
	 */
	@ManyToOne(targetEntity = EntityReferenceImpl.class)
	public EntityReference getEntityFeatureOf()
	{
		return ownerEntityReference;
	}

	/**
	 * This method sets the EntityReference for this feature. This method should only be used by the
	 * new EntityReference for updating the bidirectional link.
	 *
	 * @param newEntityReference New owner of this feature.
	 * @throws BidirectionalLinkViolationException
	 *          : If already specified, this feature first should be removed from the old reference
	 *          entity's feature list.
	 */
	protected void setEntityFeatureOf(EntityReference newEntityReference)
	{

		if (this.ownerEntityReference == null)
		{
			this.ownerEntityReference = newEntityReference;
		}
		else
		{
			if (this.ownerEntityReference.getEntityFeature().contains(this))
			{
				throw new BidirectionalLinkViolationException(this, this.ownerEntityReference);
			}
			else
			{
				this.ownerEntityReference = newEntityReference;
			}
		}
	}



	@ManyToMany(targetEntity = PhysicalEntity.class, mappedBy = "feature")
	public Set<PhysicalEntity> getFeatureOf()
	{
		return featuredEntities;
	}

	@ManyToMany(targetEntity = PhysicalEntity.class, mappedBy = "notFeature")
	public Set<PhysicalEntity> getNotFeatureOf()
	{
		return notFeaturedEntities;
	}


	@ManyToMany(targetEntity = Evidence.class)
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


	@OneToOne(targetEntity = SequenceLocationImpl.class)
	public SequenceLocation getFeatureLocation()
	{
		return featureLocation;
	}
	public void setFeatureLocation(SequenceLocation featureLocation)
	{
		this.featureLocation = featureLocation;
	}

	@OneToMany(targetEntity = SequenceRegionVocabularyImpl.class)
	public SequenceRegionVocabulary getFeatureLocationType()
	{
		return featureLocationType;
	}

	public void setFeatureLocationType(SequenceRegionVocabulary regionVocabulary)
	{
		this.featureLocationType= regionVocabulary;
	}


	@ManyToMany(targetEntity = EntityFeatureImpl.class)
	public Set<EntityFeature> getMemberFeature()
	{
		return memberFeature;
	}

	public void addMemberFeature(EntityFeature feature)
	{
		memberFeature.add(feature);
	}

	public void removeMemberFeature(EntityFeature feature)
	{
		memberFeature.remove(feature);
	}

	protected void setMemberFeature(Set<EntityFeature> feature)
	{
		this.memberFeature = feature;
	}


	public Set<EntityFeature> getMemberFeatureOf()
	{
		return this.memberFeatureOf;

	}
	@Transient
	public boolean atEquivalentLocation(EntityFeature that)
	{
		return 
			(getEntityFeatureOf() != null ?
				getEntityFeatureOf().isEquivalent(that.getEntityFeatureOf())
				: that.getEntityFeatureOf() == null)
		       && getFeatureLocation().isEquivalent(that.getFeatureLocation());
	}

	@Transient
	protected int locationCode()
	{
		int code = this.getEntityFeatureOf().equivalenceCode();
		code=code+13*this.getFeatureLocation().equivalenceCode();

		return code;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		EntityFeature that = (EntityFeature) element;
		boolean value = atEquivalentLocation(that);
		if (value)
		{
			SequenceRegionVocabulary myType = this.featureLocationType;
			SequenceRegionVocabulary yourType = that.getFeatureLocationType();
			value = (yourType == null) ?
			        myType == null :
			        myType.isEquivalent(yourType);
		}
		return value;
	}

	@Override
	public int equivalenceCode()
	{
		SequenceRegionVocabulary siteType = this.getFeatureLocationType();
		int code = siteType == null ? 0 : siteType.hashCode();
		return code + 13 * this.locationCode();
	}
}
