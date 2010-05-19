package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.BidirectionalLinkViolationException;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;

@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_UTILILTY_CLASS)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class EntityFeatureImpl extends L3ElementImpl implements EntityFeature
{

	private Set<Evidence> evidence;

	private EntityReference ownerEntityReference;
	private Set<PhysicalEntity> featureOf;
	private Set<PhysicalEntity> notFeatureOf;
	private SequenceLocation featureLocation;
	private Set<EntityFeature> memberFeature;
	private SequenceRegionVocabulary featureLocationType;
	private Set<EntityFeature> memberFeatureOf;


	public EntityFeatureImpl()
	{
		evidence = new HashSet<Evidence>();
		featureOf = new HashSet<PhysicalEntity>();
		notFeatureOf = new HashSet<PhysicalEntity>();
		memberFeatureOf = new HashSet<EntityFeature>();
		memberFeature = new HashSet<EntityFeature>();
	}


	@Transient
	public Class<? extends EntityFeature> getModelInterface()
	{
		return EntityFeature.class;
	}


	/**
	 * @return Reference entity that this feature belongs to.
	 */
	@ManyToOne(targetEntity = EntityReferenceImpl.class, cascade = {CascadeType.ALL})
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


	

	@ManyToMany(targetEntity = PhysicalEntityImpl.class, mappedBy = "feature")
	public Set<PhysicalEntity> getFeatureOf()
	{
		return featureOf;
	}

	@ManyToMany(targetEntity = PhysicalEntityImpl.class, mappedBy = "notFeature")
	public Set<PhysicalEntity> getNotFeatureOf()
	{
		return notFeatureOf;
	}


	@ManyToMany(targetEntity = EvidenceImpl.class, cascade={CascadeType.ALL})
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


	@OneToOne(targetEntity = SequenceLocationImpl.class, cascade={CascadeType.ALL})
	public SequenceLocation getFeatureLocation()
	{
		return featureLocation;
	}
	public void setFeatureLocation(SequenceLocation featureLocation)
	{
		this.featureLocation = featureLocation;
	}

	@ManyToOne(targetEntity = SequenceRegionVocabularyImpl.class, cascade = {CascadeType.ALL})
	public SequenceRegionVocabulary getFeatureLocationType()
	{
		return featureLocationType;
	}

	public void setFeatureLocationType(SequenceRegionVocabulary regionVocabulary)
	{
		this.featureLocationType= regionVocabulary;
	}


	@ManyToMany(targetEntity = EntityFeatureImpl.class, cascade={CascadeType.ALL})
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

	@ManyToMany(targetEntity = EntityFeatureImpl.class, mappedBy = "memberFeature")
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

	protected void setOwnerEntityReference(EntityReference ownerEntityReference)
	{
		this.ownerEntityReference = ownerEntityReference;
	}

	protected void setFeatureOf(Set<PhysicalEntity> featureOf)
	{
		this.featureOf = featureOf;
	}

	protected void setNotFeatureOf(Set<PhysicalEntity> notFeatureOf)
	{
		this.notFeatureOf = notFeatureOf;
	}

	protected void setMemberFeatureOf(Set<EntityFeature> memberFeatureOf)
	{
		this.memberFeatureOf = memberFeatureOf;
	}
}
