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
	// Full-text search field names (case sensitive!)
	public static final String FIELD_AVAILABILITY = "availability";
	public static final String FIELD_COMMENT = "comment"; // biopax comments
	public static final String FIELD_KEYWORD = "keyword"; //anything, e.g., names, terms, comments, incl. - from child elements 
	public static final String FIELD_NAME = "name"; // standardName, displayName, other names
	public static final String FIELD_TERM = "term"; // CV terms
	public static final String FIELD_XREFDB = "xrefdb"; //xref.db
	public static final String FIELD_XREFID = "xrefid"; //xref.id
	public static final String FIELD_ECNUMBER = "ecnumber";
	public static final String FIELD_SEQUENCE = "sequence";
	// Full-text search / Filter field names (case sensitive!) -
	public static final String FIELD_ORGANISM = "organism";
	public static final String FIELD_DATASOURCE = "dataSource"; //(case sensitive)
	public static final String FIELD_PATHWAY = "pathway";
	
	// Filter definitionnames (case sensitive!)
	public static final String FILTER_BY_ORGANISM = "organism";
	public static final String FILTER_BY_DATASOURCE = "datasource";
	
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
    @Column(length=333)
    public String getRDFId()
    {
        return uri;
    }

    protected void setRDFId(String id)
    {
        this.uri = id;
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

