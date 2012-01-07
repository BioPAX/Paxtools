package org.biopax.paxtools.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.DocumentId;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
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
	// ----------------- Index Names and Search Fields --------------------
	public final static String SEARCH_FIELD_KEYWORD ="keyword";
	public final static String SEARCH_FIELD_NAME = "name";
	public final static String SEARCH_FIELD_TERM = "term";
	public final static String SEARCH_FIELD_EC_NUMBER = "ecnumber";
	public final static String SEARCH_FIELD_SEQUENCE = "sequence";
	public final static String SEARCH_FIELD_XREF_DB = "xrefdb";
	public final static String SEARCH_FIELD_XREF_ID = "xrefid";
	public final static String SEARCH_FIELD_AVAILABILITY = "availability";
	public final static String SEARCH_FIELD_COMMENT = "comment";
	
	public final static String SEARCH_INDEX_NAME = "biopax_index";
	// ------------------------------ FIELDS ------------------------------

	private String uri;
	
	// anything extra can be stored in this map (not to persist in a DB though)
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
}

