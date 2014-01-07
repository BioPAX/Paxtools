package org.biopax.paxtools.impl.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BPCollections;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.Set;

@Entity
@Proxy(proxyClass= EntityFeature.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class EntityFeatureImpl extends L3ElementImpl implements EntityFeature
{
	private final Log LOG = LogFactory.getLog(EntityFeatureImpl.class);
	
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
		evidence = BPCollections.I.createSafeSet();
		featureOf = BPCollections.I.createSafeSet();
		notFeatureOf = BPCollections.I.createSafeSet();
		memberFeatureOf = BPCollections.I.createSafeSet();
		memberFeature = BPCollections.I.createSafeSet();
	}


	@Transient
	public Class<? extends EntityFeature> getModelInterface()
	{
		return EntityFeature.class;
	}

	// protected 'entityFeatureXOf' property for use by Hibernate (simple setter)
	@ManyToOne(targetEntity = EntityReferenceImpl.class)
	public EntityReference getEntityFeatureOf(){
		return ownerEntityReference;
	}
	public void setEntityFeatureOf(EntityReference entityReference){
		ownerEntityReference = entityReference;
	}
	
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PhysicalEntityImpl.class, mappedBy = "feature")
	public Set<PhysicalEntity> getFeatureOf()
	{
		return featureOf;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PhysicalEntityImpl.class, mappedBy = "notFeature")
	public Set<PhysicalEntity> getNotFeatureOf()
	{
		return notFeatureOf;
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

	protected void setEvidence(Set<Evidence> evidence)
	{
		this.evidence = evidence;
	}


	@ManyToOne(targetEntity = SequenceLocationImpl.class)//, cascade={CascadeType.ALL})
	public SequenceLocation getFeatureLocation()
	{
		return featureLocation;
	}
	public void setFeatureLocation(SequenceLocation featureLocation)
	{
		this.featureLocation = featureLocation;
	}

	@ManyToOne(targetEntity = SequenceRegionVocabularyImpl.class)
	public SequenceRegionVocabulary getFeatureLocationType()
	{
		return featureLocationType;
	}

	public void setFeatureLocationType(SequenceRegionVocabulary regionVocabulary)
	{
		this.featureLocationType= regionVocabulary;
	}

	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EntityFeatureImpl.class)
	@JoinTable(name="memberFeature")
	public Set<EntityFeature> getMemberFeature()
	{
		return memberFeature;
	}
	
	protected void setMemberFeature(Set<EntityFeature> memberFeature) {
		this.memberFeature = memberFeature;
	}

	public void addMemberFeature(EntityFeature feature)
	{
		if (feature != null) {
			memberFeature.add(feature);
			feature.getMemberFeatureOf().add(this);
		}
	}

	public void removeMemberFeature(EntityFeature feature)
	{
		if (feature != null) {
			memberFeature.remove(feature);
			feature.getMemberFeatureOf().remove(this);
		}
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EntityFeatureImpl.class, mappedBy = "memberFeature")
	public Set<EntityFeature> getMemberFeatureOf()
	{
		return this.memberFeatureOf;
	}
	
	protected void setMemberFeatureOf(Set<EntityFeature> memberFeatureOf)
	{
		this.memberFeatureOf = memberFeatureOf;
	}

    @Transient
    /**
     * This method returns true if and only if two entity features are on the same known location on a known ER.
     * Unknown location or ER on any one of the features results in a false.
     */
    public boolean atEquivalentLocation(EntityFeature that) {
        return getEntityFeatureOf() != null &&
                getEntityFeatureOf().isEquivalent(that.getEntityFeatureOf()) &&
                getFeatureLocation() != null &&
                getFeatureLocation().isEquivalent(that.getFeatureLocation());
    }

    @Transient
    protected int locationCode() {
        if (this.getEntityFeatureOf() == null || this.getFeatureLocation()==null) return hashCode();
        else {
            return
                    this.getEntityFeatureOf().equivalenceCode()+
                            13 * this.getFeatureLocation().equivalenceCode();
        }
    }

    @Override
    protected boolean semanticallyEquivalent(BioPAXElement element) {
        if (!(element instanceof EntityFeature))
            return false;

        EntityFeature that = (EntityFeature) element;
        boolean value = atEquivalentLocation(that);
        if (value) {
            SequenceRegionVocabulary myType = this.featureLocationType;
            SequenceRegionVocabulary yourType = that.getFeatureLocationType();
            value = (yourType == null) ?
                    myType == null : yourType.isEquivalent(myType);
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

	protected void setFeatureOf(Set<PhysicalEntity> featureOf)
	{
		this.featureOf = featureOf;
	}

	protected void setNotFeatureOf(Set<PhysicalEntity> notFeatureOf)
	{
		this.notFeatureOf = notFeatureOf;
	}
}
