package org.biopax.paxtools.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.DocumentId;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Proxy(proxyClass= BioPAXElement.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(length=40)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@NamedQueries({
	@NamedQuery(name="org.biopax.paxtools.impl.elementByRdfId",
		query="from org.biopax.paxtools.model.BioPAXElement as el where upper(el.RDFId) = upper(:rdfid)"),
	@NamedQuery(name="org.biopax.paxtools.impl.elementByRdfIdEager",
		query="from org.biopax.paxtools.model.BioPAXElement as el fetch all properties where upper(el.RDFId) = upper(:rdfid)")

})
public abstract class BioPAXElementImpl implements BioPAXElement
{
	private String uri;
	
	// anything extra can be stored in this map (not to persist in a DB usually)
	private Map<String, Object> annotations;

   
	public BioPAXElementImpl() {
		this.annotations = new HashMap<String, Object>();
	}
	
	public BioPAXElementImpl(String uri) {
		this.uri = uri;
	}


    @Transient
    public boolean isEquivalent(BioPAXElement element)
    {
        return this.equals(element) || this.getModelInterface().isInstance(element) &&
                this.semanticallyEquivalent(element);
    }

    protected boolean semanticallyEquivalent(BioPAXElement element)
    {
        return false;
    }

    public int equivalenceCode()
    {
        return uri.hashCode();
        // return uri == null ? super.hashCode() : uri.hashCode();
    }

    @Id
    @DocumentId
    @Column(length=333)//, columnDefinition="BINARY(255)")
    //@Column(length=255, nullable=false)
    //@Column(unique=true, nullable=false)
    //@Field(name = BioPAXElementImpl.SEARCH_FIELD_ID) // full-text search: better NOT to use rdfid!
    public String getRDFId()
    {
        return uri;
    }

    protected void setRDFId(String id)
    {
        this.uri = id;
//	    Set<Model> ownerModels = this.getOwnerModels();
//	    for (Model ownerModel : ownerModels)
//	    {
//		    ownerModel.notifyIdChange();
//	    }
    }

    public String toString()
    {
        return uri;
    }

    
    @Transient
    public Map<String, Object> getAnnotations() {
		return annotations;
	}
    
    
    /* 
     * TODO A non-biopax way to get/set/persist pathway membership.
     * This requires (depends on) the annotations to be 
     * somewhere pre-calculated.
     */ 
//    @Field(name="pathways")
//    @FieldBridge(impl=ParentPathwayFieldBridge.class)
//    @ManyToMany(targetEntity=PathwayImpl.class, fetch=FetchType.LAZY)
//    protected Set<Pathway> getPathway() {
//    	return (Set<Pathway>) annotations.get(AnnotationMapKey.PARENT_PATHWAYS.toString());
//    }
//    
//    protected void setPathway(Set<Pathway> parentPathways) {
//		annotations.put(AnnotationMapKey.PARENT_PATHWAYS.toString(), parentPathways);
//	}
}

