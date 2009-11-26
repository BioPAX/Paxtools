package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.SetEquivalanceChecker;
import org.biopax.paxtools.util.BidirectionalLinkViolationException;

import java.util.HashSet;
import java.util.Set;

class EntityFeatureImpl extends L3ElementImpl implements EntityFeature
{

	private Set<Evidence> evidence;

	private EntityReference ownerEntityReference;
	private Set<PhysicalEntity> featuredEntities;
	private Set<PhysicalEntity> notFeaturedEntities;
	private Set<SequenceLocation> featureLocation;
	private Set<EntityFeature> memberFeature;
	private SequenceRegionVocabulary featureLocationType;


	/**
	 * Constructor.
	 */
	public EntityFeatureImpl()
	{
		evidence = new HashSet<Evidence>();
		featuredEntities = new HashSet<PhysicalEntity>();
		notFeaturedEntities = new HashSet<PhysicalEntity>();
		this.featureLocation = new HashSet<SequenceLocation>();
		this.memberFeature = new HashSet<EntityFeature>();
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends EntityFeature> getModelInterface()
	{
		return EntityFeature.class;
	}


	// Inverse of Property ENTITY-FEATURE

	/**
	 * @return Reference entity that this feature belongs to.
	 */

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
	public void setEntityFeatureOf(EntityReference newEntityReference)
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


	// Inverse of Property MODIFIED_AT

	public Set<PhysicalEntity> getFeatureOf()
	{
		return featuredEntities;
	}

	// Inverse of Property NOT_MODIFIED_AT

	public Set<PhysicalEntity> getNoFeatureOf()
	{
		return notFeaturedEntities;
	}


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

	// Property FEATURE-LOCATION

	public Set<SequenceLocation> getFeatureLocation()
	{
		return featureLocation;
	}

	public void addFeatureLocation(SequenceLocation featureLocation)
	{
		this.featureLocation.add(featureLocation);
	}

	public void removeFeatureLocation(SequenceLocation featureLocation)
	{
		this.featureLocation.remove(featureLocation);
	}

	public void setFeatureLocation(Set<SequenceLocation> featureLocation)
	{
		this.featureLocation = featureLocation;
	}

	public SequenceRegionVocabulary getFeatureLocationType()
	{
		return featureLocationType;
	}

	public void setFeatureLocationType(SequenceRegionVocabulary regionVocabulary)
	{
		this.featureLocationType= regionVocabulary;
	}

	// Property memberFeature

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

	public void setMemberFeature(Set<EntityFeature> feature)
	{
		this.memberFeature = feature;
	}

	
	public boolean atEquivalentLocation(EntityFeature that)
	{
		return 
			(getEntityFeatureOf() != null ?
				getEntityFeatureOf().isEquivalent(that.getEntityFeatureOf())
				: that.getEntityFeatureOf() == null)
		       && SetEquivalanceChecker.isEquivalent(
				getFeatureLocation(), that.getFeatureLocation());
	}

	protected int locationCode()
	{
		int code = this.getEntityFeatureOf().equivalenceCode();
		for (SequenceLocation sequenceLocation : this.featureLocation)
		{
		   code=code+13*sequenceLocation.equivalenceCode();
		}
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
