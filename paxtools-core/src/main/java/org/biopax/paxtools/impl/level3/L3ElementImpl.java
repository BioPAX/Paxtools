package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.util.SetStringBridge;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import java.util.HashSet;
import java.util.Set;

import static org.hibernate.annotations.FetchProfile.FetchOverride;

/**
 * Base BioPAX Level3 element.
 *
 */
@Entity
@Proxy(proxyClass= Level3Element.class)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//Fetch Profiles
 @FetchProfile(name = "completer", fetchOverrides = {
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
 @FetchOverride(entity = PathwayStepImpl.class, association = "stepProcess", mode = FetchMode.JOIN),
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
})

public abstract class L3ElementImpl extends BioPAXElementImpl
        implements Level3Element
{
    private Set<String> comment;

    public L3ElementImpl()
    {
        this.comment = new HashSet<String>();
    }

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ElementCollection
    @JoinTable(name="comment")
    @Fields({
    	@Field(name=FIELD_COMMENT, index=Index.TOKENIZED, bridge=@FieldBridge(impl=SetStringBridge.class)),
    	@Field(name=FIELD_KEYWORD, store=Store.YES, index=Index.TOKENIZED, bridge=@FieldBridge(impl=SetStringBridge.class))
    })
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

}
