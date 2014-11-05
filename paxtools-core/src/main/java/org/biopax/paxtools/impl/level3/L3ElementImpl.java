package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.util.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;

import static org.hibernate.annotations.FetchProfile.FetchOverride;

/**
 * Base BioPAX Level3 element.
 *
 */
@Entity
@Proxy(proxyClass= Level3Element.class)
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@FetchProfiles({
@FetchProfile(name = "mul_properties_join", fetchOverrides = {
 @FetchOverride(entity = EvidenceImpl.class, association = "experimentalForm", mode = FetchMode.JOIN),
 @FetchOverride(entity = EntityFeatureImpl.class, association = "memberFeature", mode = FetchMode.JOIN),
 @FetchOverride(entity = CatalysisImpl.class, association = "cofactor", mode = FetchMode.JOIN),
 @FetchOverride(entity = EntityReferenceImpl.class, association = "memberEntityReference", mode = FetchMode.JOIN),
 @FetchOverride(entity = ComplexImpl.class, association = "componentStoichiometry", mode = FetchMode.JOIN),
 @FetchOverride(entity = BiochemicalReactionImpl.class, association = "ECNumber", mode = FetchMode.JOIN),
 @FetchOverride(entity = ExperimentalFormImpl.class, association = "experimentalFormDescription", mode = FetchMode.JOIN),
 @FetchOverride(entity = ComplexImpl.class, association = "component", mode = FetchMode.JOIN),
 @FetchOverride(entity = ConversionImpl.class, association = "participantStoichiometry", mode = FetchMode.JOIN),
 @FetchOverride(entity = PhysicalEntityImpl.class, association = "feature", mode = FetchMode.JOIN),
 @FetchOverride(entity = ConversionImpl.class, association = "right", mode = FetchMode.JOIN),
 @FetchOverride(entity = PathwayStepImpl.class, association = "stepProcessX", mode = FetchMode.JOIN),
 @FetchOverride(entity = ExperimentalFormImpl.class, association = "experimentalFeature", mode = FetchMode.JOIN),
 @FetchOverride(entity = ControlImpl.class, association = "controlled", mode = FetchMode.JOIN),
 @FetchOverride(entity = PublicationXrefImpl.class, association = "url", mode = FetchMode.JOIN),
 @FetchOverride(entity = InteractionImpl.class, association = "participant", mode = FetchMode.JOIN),
 @FetchOverride(entity = PathwayImpl.class, association = "pathwayOrder", mode = FetchMode.JOIN),
 @FetchOverride(entity = ControlImpl.class, association = "pathwayController", mode = FetchMode.JOIN),
 @FetchOverride(entity = ControlImpl.class, association = "peController", mode = FetchMode.JOIN),
 @FetchOverride(entity = L3ElementImpl.class, association = "comment", mode = FetchMode.JOIN),
 @FetchOverride(entity = InteractionImpl.class, association = "interactionType", mode = FetchMode.JOIN),
 @FetchOverride(entity = PathwayStepImpl.class, association = "nextStep", mode = FetchMode.JOIN),
 @FetchOverride(entity = EntityImpl.class, association = "dataSource", mode = FetchMode.JOIN),
 @FetchOverride(entity = BioSourceImpl.class, association = "cellType", mode = FetchMode.JOIN),
 @FetchOverride(entity = EntityReferenceImpl.class, association = "entityFeature", mode = FetchMode.JOIN),
 @FetchOverride(entity = EntityImpl.class, association = "evidence", mode = FetchMode.JOIN),
 @FetchOverride(entity = EntityReferenceImpl.class, association = "evidence", mode = FetchMode.JOIN),
 @FetchOverride(entity = PathwayStepImpl.class, association = "evidence", mode = FetchMode.JOIN),
 @FetchOverride(entity = EntityFeatureImpl.class, association = "evidence", mode = FetchMode.JOIN),
 @FetchOverride(entity = PathwayImpl.class, association = "pathwayComponent", mode = FetchMode.JOIN),
 @FetchOverride(entity = EntityReferenceImpl.class, association = "entityReferenceType", mode = FetchMode.JOIN),
 @FetchOverride(entity = PublicationXrefImpl.class, association = "author", mode = FetchMode.JOIN),
 @FetchOverride(entity = NucleicAcidReferenceImpl.class, association = "subRegion", mode = FetchMode.JOIN),
 @FetchOverride(entity = NamedImpl.class, association = "name", mode = FetchMode.JOIN),
 @FetchOverride(entity = ConversionImpl.class, association = "left", mode = FetchMode.JOIN),
 @FetchOverride(entity = BiochemicalReactionImpl.class, association = "KEQ", mode = FetchMode.JOIN),
 @FetchOverride(entity = EntityImpl.class, association = "availability", mode = FetchMode.JOIN),
 @FetchOverride(entity = BiochemicalReactionImpl.class, association = "deltaG", mode = FetchMode.JOIN),
 @FetchOverride(entity = EvidenceImpl.class, association = "confidence", mode = FetchMode.JOIN),
 @FetchOverride(entity = PhysicalEntityImpl.class, association = "notFeature", mode = FetchMode.JOIN),
 @FetchOverride(entity = PhysicalEntityImpl.class, association = "memberPhysicalEntity", mode = FetchMode.JOIN),
 @FetchOverride(entity = TemplateReactionImpl.class, association = "product", mode = FetchMode.JOIN),
 @FetchOverride(entity = EvidenceImpl.class, association = "evidenceCode", mode = FetchMode.JOIN),
 @FetchOverride(entity = PublicationXrefImpl.class, association = "source", mode = FetchMode.JOIN),
 @FetchOverride(entity = BiochemicalReactionImpl.class, association = "deltaS", mode = FetchMode.JOIN),
 @FetchOverride(entity = ControlledVocabularyImpl.class, association = "term", mode = FetchMode.JOIN),
 @FetchOverride(entity = XReferrableImpl.class, association = "xref", mode = FetchMode.JOIN),
 @FetchOverride(entity = BiochemicalReactionImpl.class, association = "deltaH", mode = FetchMode.JOIN)
 }),
@FetchProfile(name = "inverse_mul_properties_join", fetchOverrides = {
 @FetchProfile.FetchOverride(entity = PhysicalEntityImpl.class, association = "memberPhysicalEntityOf", mode = FetchMode.JOIN),
 @FetchProfile.FetchOverride(entity = PhysicalEntityImpl.class, association = "controllerOf", mode = FetchMode.JOIN),
 @FetchProfile.FetchOverride(entity = PhysicalEntityImpl.class, association = "componentOf", mode = FetchMode.JOIN),
 @FetchProfile.FetchOverride(entity = EntityImpl.class, association = "participantOf", mode = FetchMode.JOIN),
 @FetchProfile.FetchOverride(entity = EntityReferenceImpl.class, association = "memberEntityReferenceOf", mode = FetchMode.JOIN),
 @FetchProfile.FetchOverride(entity = EntityReferenceImpl.class, association = "entityReferenceOf", mode = FetchMode.JOIN),
 @FetchProfile.FetchOverride(entity = ProcessImpl.class, association = "pathwayComponentOf", mode = FetchMode.JOIN),
 @FetchProfile.FetchOverride(entity = ProcessImpl.class, association = "stepProcessOf", mode = FetchMode.JOIN),
 @FetchProfile.FetchOverride(entity = ProcessImpl.class, association = "controlledOf", mode = FetchMode.JOIN),
 @FetchProfile.FetchOverride(entity = PathwayImpl.class, association = "controllerOf", mode = FetchMode.JOIN)
 })
})
public abstract class L3ElementImpl extends BioPAXElementImpl
        implements Level3Element
{
    private Set<String> comment;
    
    private final Set<Pathway> pathways;
    private final Set<Provenance> datasources;
    private final Set<BioSource> organisms;
    private final Set<String> keywords;

    public L3ElementImpl()
    {
        this.comment = BPCollections.I.createSet();
        this.pathways = BPCollections.I.createSet();
        this.datasources = BPCollections.I.createSet();
        this.organisms = BPCollections.I.createSet();
        this.keywords =  BPCollections.I.createSet();
    }

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ElementCollection
    @JoinTable(name="comment")
    @Field(name=FIELD_COMMENT, analyze=Analyze.YES, bridge=@FieldBridge(impl=SetStringBridge.class))
	@Column(columnDefinition="LONGTEXT")
    public Set<String> getComment()
    {
        return this.comment;
    }

    public void setComment(Set<String> comment)
    {
        this.comment = comment;
    }

    public void addComment(String COMMENT)
    {
        if(COMMENT != null && COMMENT.length() > 0)
        	this.comment.add(COMMENT);
    }

    public void removeComment(String COMMENT)
    {
    	if(COMMENT != null)
    		this.comment.remove(COMMENT);
    }

	
    /**
     * A non-public transient method (not stored in the db table)
	 * to create the 'keyword' full-text index field by aggregating 
	 * biopax data field values from all child elements.
	 * 
	 * This method may be called once per biopax element
	 * by the Hibernate Search framework (indexer), if it is used, 
	 * or never called.
     * 
     * @return
     */
	@Transient
	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES)
	@FieldBridge(impl=SetStringBridge.class)
	public Set<String> getKeywords() {
		return this.keywords;
	}
	
    /**
     * A transient method (not stored in the db table)
	 * to create the 'organism' full-text index field 
	 * used then both for searching and filtering (important).
	 * 
	 * This method may be called once per biopax element
	 * by the Hibernate Search framework (indexer),
	 * or never called.
     * 
     * @return
     */
	@Transient
	@Field(name=FIELD_ORGANISM, store=Store.YES, analyze=Analyze.NO)
	@FieldBridge(impl=OrganismFieldBridge.class)
	public Set<BioSource> getOrganisms() {
		return this.organisms;
	}
	
    /**
     * A transient method (not stored in the db table)
	 * to create the 'datasource' full-text index field 
	 * used then both for searching and filtering (important).
	 * 
	 * This method may be called once per biopax element
	 * by the Hibernate Search framework (indexer), if used, 
	 * or never called otherwise.
	 *   
     * @return
     */
	@Transient
	@Field(name=FIELD_DATASOURCE, store=Store.YES, analyze=Analyze.NO)
	@FieldBridge(impl=DataSourceFieldBridge.class)
	public Set<Provenance> getDatasources() {
		return this.datasources;
	}

	
    /**
     * An transient method (not stored in the db table),
     * not trivial one, to create the 'pathway' full-text index field
	 * used then both for searching and especially annotating search
	 * hits. 
	 * 
	 * Parent pathways can be inferred and updated once the BioPAX model
	 * is built and complete.
	 * 
	 * This method may be called once per biopax element
	 * by the Hibernate Search framework (indexer), 
	 * or never called.
	 *   
     * @return
     */
	@Transient
	@Field(name=FIELD_PATHWAY, store=Store.YES, analyze=Analyze.NO)
	//this bridge simply adds pathways URIs and names to the 'pathway' index field.
	@FieldBridge(impl=ParentPathwayFieldBridge.class)
	public Set<Pathway> getParentPathways() {
		return this.pathways;
	}
	
}
