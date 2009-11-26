package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.SetEquivalanceChecker;
import org.biopax.paxtools.util.BidirectionalLinkViolationException;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.HashSet;
import java.util.Set;

class EntityReferenceImpl extends L3ElementImpl
        implements EntityReference {

    private final NameHelper nameHelper;
    private Set<EntityFeature> entityFeature;
    private HashSet<SimplePhysicalEntity> simplePhysicalEntity;
    private Set<Evidence> evidence;
    Set<EntityReferenceTypeVocabulary> entityReferenceType;
    Set<EntityReference> memberEntity;
    private final ReferenceHelper referenceHelper;

    /**
     * Constructor.
     */
    public EntityReferenceImpl() {
        this.nameHelper = new NameHelper();
        this.entityFeature = new HashSet<EntityFeature>();
        this.simplePhysicalEntity = new HashSet<SimplePhysicalEntity>();
        this.evidence = new HashSet<Evidence>();
        this.entityReferenceType = new HashSet<EntityReferenceTypeVocabulary>();
        this.memberEntity = new HashSet<EntityReference>();
        this.referenceHelper = new ReferenceHelper(this);
    }

    //
    // BioPAXElement interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Class<? extends EntityReference> getModelInterface() {
        return EntityReference.class;
    }

    //
    // EntityReference interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The contents of this set should NOT be modified. For manipulating the
     * contents use addNew and remove instead.
     *
     * @return A set of entity features for the reference entity.
     */
    public Set<EntityFeature> getEntityFeature() {
        return entityFeature;
    }

    public void addEntityFeature(EntityFeature entityFeature) {
        if (entityFeature != null) {
            EntityReference eFof = entityFeature.getEntityFeatureOf();
            if (eFof != null) {
                if (!eFof.equals(this)) {
                    throw new BidirectionalLinkViolationException(this,
                            entityFeature);
                }
            } else {
                entityFeature.setEntityFeatureOf(this);
            }
            this.entityFeature.add(entityFeature);
        }
    //todo put a log here..
    }

    public void removeEntityFeature(EntityFeature entityFeature) {
        if (entityFeature != null) {
            assert entityFeature.getEntityFeatureOf() == this;
            this.entityFeature.remove(entityFeature);
            entityFeature.setEntityFeatureOf(null);
        }
    }

    public void setEntityFeature(Set<EntityFeature> entityFeature) {
        this.entityFeature = entityFeature;
    }

    public Set<SimplePhysicalEntity> getEntityReferenceOf() {
        return simplePhysicalEntity;
    }

    public Set<EntityReferenceTypeVocabulary> getEntityReferenceType() {
        return entityReferenceType;
    }

    public void addEntityReferenceType(
            EntityReferenceTypeVocabulary entityReferenceType) {
        this.entityReferenceType.add(entityReferenceType);
    }

    public void removeEntityReferenceType(
            EntityReferenceTypeVocabulary entityReferenceType) {
        this.entityReferenceType.remove(entityReferenceType);
    }

    public void setEntityReferenceType(
            Set<EntityReferenceTypeVocabulary> entityReferenceType) {
        // remove all elements from existing set
        for (EntityReferenceTypeVocabulary ocv : this.entityReferenceType) {
            removeEntityReferenceType(ocv);
        }
        // addNew new open controlled vocabulary
        for (EntityReferenceTypeVocabulary ocv : entityReferenceType) {
            addEntityReferenceType(ocv);
        }
    }

    public Set<EntityReference> getMemberEntityReference() {
        return memberEntity;
    }

    public void addMemberEntityReference(EntityReference memberEntity) {
        this.memberEntity.add(memberEntity);
    }

    public void removeMemberEntityReference(EntityReference memberEntity) {
        this.memberEntity.remove(memberEntity);
    }

    public void setMemberEntityReference(Set<EntityReference> memberEntity) {
        this.memberEntity = memberEntity;
    }

    //
    // observable interface implementation
    //
    /////////////////////////////////////////////////////////////////////////////
    public Set<Evidence> getEvidence() {
        return evidence;
    }

    public void addEvidence(Evidence evidence) {
        this.evidence.add(evidence);
    }

    public void removeEvidence(Evidence evidence) {
        this.evidence.remove(evidence);
    }

    public void setEvidence(Set<Evidence> evidence) {
        this.evidence = evidence;
    }

    //
    // named interface implementation
    //
    /////////////////////////////////////////////////////////////////////////////
    public Set<String> getName() {
        return nameHelper.getName();
    }

    public void setName(Set<String> name) {
        nameHelper.setName(name);
    }

    public void addName(String name) {
        nameHelper.addName(name);
    }

    public void removeName(String name) {
        nameHelper.removeName(name);
    }

    public String getDisplayName() {
        return nameHelper.getDisplayName();
    }

    public void setDisplayName(String displayName) {
        nameHelper.setDisplayName(displayName);
    }

    public String getStandardName() {
        return nameHelper.getStandardName();
    }

    public void setStandardName(String standardName) {
        nameHelper.setStandardName(standardName);
    }

    public Set<Xref> getXref() {
        return referenceHelper.getXref();
    }

    public void setXref(Set<Xref> Xref) {
        referenceHelper.setXref(Xref);
    }

    public void addXref(Xref Xref) {
        referenceHelper.addXref(Xref);
    }

    public void removeXref(Xref Xref) {
        referenceHelper.removeXref(Xref);
    }
    
    @Override
    protected boolean semanticallyEquivalent(BioPAXElement element) {
    	if(!(element instanceof EntityReference)) return false;
    	EntityReference that = (EntityReference) element;
    	
    	return SetEquivalanceChecker.isEquivalent(getEntityFeature(), that.getEntityFeature())
    		&& SetEquivalanceChecker.isEquivalentIntersection(getEvidence(), that.getEvidence())
    		&& SetEquivalanceChecker.isEquivalent(getEntityReferenceType(), that.getEntityReferenceType())
    		&& SetEquivalanceChecker.isEquivalent(getMemberEntityReference(), that.getMemberEntityReference())
    		&& SetEquivalanceChecker.isEquivalentIntersection(
    				new ClassFilterSet<UnificationXref>(getXref(), UnificationXref.class), 
    				new ClassFilterSet<UnificationXref>(that.getXref(),UnificationXref.class)
    			);
    }
}
