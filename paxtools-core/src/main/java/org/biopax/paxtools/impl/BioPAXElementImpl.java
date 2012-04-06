package org.biopax.paxtools.impl;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@Proxy(proxyClass= BioPAXElement.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(length=40)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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

	private String _pk; // used for primary key

	
	public BioPAXElementImpl() {
		this.annotations = new HashMap<String, Object>();
	}
	
	public BioPAXElementImpl(String uri) {
		this();
		this.uri = uri;
	}


	// Primary Key
	// could not use names like: 'key' (SQL conflict), 'id', 'idx' (conflicts with the existing prop. in a child class), etc...
	// @GeneratedValue did not work (stateless session is unable to save/resolve object references and inverse props)
	@SuppressWarnings("unused") //is used by Hibernate
	@Id
	@Column(name="pk", length=32) // enough to save MD5 digest Hex.
	private String getPk() {
		return _pk;
	}

	@SuppressWarnings("unused")
	private void setPk(String pk) {
		this._pk = pk;
	}

	@Transient
    public boolean isEquivalent(BioPAXElement element)
    {
        return this.equals(element) || this.semanticallyEquivalent(element);
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

//    @Id
//    @DocumentId
//    @Column(length=333) 
	// we have had a big PROBLEM using this as PK, like 
	// (also, Mysql 5.0.x PK index is case-insensitive and only 64-chars long by default);
    // no doubt, because of using this long string PK, we also had performance issues..
    @Lob
    public String getRDFId()
    {
        return uri;
    }

    /**
     * BioPAX URI private set method. 
     * 
     * By design, URI should not normally be modified once the object is created,
     * unless you know what you're doing (then it can be done using Java reflection)!
     * 
     * @param id
     */
    @SuppressWarnings("unused")
	private void setRDFId(String id)
    {
        this.uri = id;
        this._pk = ModelUtils.md5hex(id);
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

