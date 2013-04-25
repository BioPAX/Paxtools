package org.biopax.paxtools.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Entity
@Proxy(proxyClass= BioPAXElement.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(length=40)
@DynamicUpdate
@DynamicInsert
@NamedQueries({
	@NamedQuery(name="org.biopax.paxtools.impl.BioPAXElementExists",
				query="select 1 from BioPAXElementImpl where pk=:md5uri")
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
	
	// Global search filter definitions in the (paxtools-core PhysicalEntityImpl, case sensitive!)
	public static final String FILTER_BY_ORGANISM = "organism";
	public static final String FILTER_BY_DATASOURCE = "datasource";
	
	private String uri;
	
	// anything extra can be stored in this map (not to persist in a DB usually)
	private Map<String, Object> annotations;

	private String _pk; // Primary Key

	
	public BioPAXElementImpl() {
		this.annotations = new HashMap<String, Object>();
	}
	

	// Primary Key for persistence
	// could not use names like: 'key' (SQL conflict), 'id', 'idx' 
	// (conflicts with the property in sub-classes).
	// @GeneratedValue - did not work (for stateless sessions)	
    /**
     * Gets Primary Key.
     * 
     * This method may be useful within a
     * persistence context (Hibernate). This is not part of
     * Paxtools standard BioPAX API, it is implementation detail.
     * 
     * Normally, one should always use {@link #getRDFId()}
     * to get BioPAX element's URI and use it in 
     * data analysis.
     * 
     * @return
     */
	@Id
	@Column(name="pk", length=32) // enough to save MD5 digest Hex.
	public String getPk() {
		return _pk;
	}

	
	/**
	 * Primary Key setter (non-public method).
	 * 
	 * This is called only from {@link #setRDFId(String)} 
	 * and Hibernate framework (optional). Primary Key is not required 
	 * (can be ignored) if this BioPAX element (model, algorithm) 
	 * is not persistent (i.e., when not using any persistence provider, 
	 * database, etc. to handle the BioPAX model)
	 * 
	 * 
	 * @param pk
	 */
	@SuppressWarnings("unused")
	private void setPk(String pk) {
		this._pk = pk;
	}

	//private simple setter/getter, for persistence only
	//(use biopax element RDFId property instead)
	@Lob
	@Column(nullable=false)
    private String getUri() {
        return uri;
    }
    @SuppressWarnings("unused")
	private void setUri(String uri) {
    	this.uri = uri;
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
    }


	// Beware PROBLEMs, do not use RDFId (URI) as primary key 
	// (e.g., Mysql 5.x PK/index is case-insensitive, only 64-chars long, by default;
    // and there're performance issues too)
    @Transient
    public String getRDFId()
    {
        return uri;
    }

    /**
     * Private setter for the biopax element RDFId 
     * (full URI). Using the URI string, this setter 
     * also sets or updates the primary key field, 
     * {@link #getPk()}
     * 
     * Normally, URI should never be modified 
     * after the object is created unless you know 
     * what you're doing (and can use Java Reflection).
     * 
     * @param uri
     */
    @SuppressWarnings("unused")
	private synchronized void setRDFId(String uri)
    {
        if(uri == null)
        	throw new IllegalArgumentException();
        
    	this.uri = uri;
        this._pk = md5hex(this.uri);
    }

    
    public String toString()
    {
        return uri;
    }

    
    @Transient
    public Map<String, Object> getAnnotations() {
		return annotations;
	}
    

    //to calculate the PK from URI
    public static final MessageDigest MD5_DIGEST; 

	static {
		try {
			MD5_DIGEST = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Cannot instantiate MD5 MessageDigest!", e);
		}
	}
    

	/**
	 * Utility method that is called once per object
	 * to generate the primary key {@link #getPk()}
	 * from URI. URIs can be long and are case sensitive,
	 * and therefore would make bad DB primary key.
	 * 
	 * @param id
	 * @return
	 */
	private static String md5hex(String id) {
		byte[] digest = MD5_DIGEST.digest(id.getBytes());
		StringBuffer sb = new StringBuffer();
		for (byte b : digest)
			sb.append(Integer.toHexString((int) (b & 0xff) | 0x100).substring(1, 3));
		String hex = sb.toString();
		return hex;
	}
	

	/**
	 * {@inheritDoc}
	 * 
	 * For the sake of BioPAX Model integrity and completeness,
	 * BioPAX objects having the same URI will have
	 * same hash code. This therefore prevents accidentally adding
	 * different BioPAX elements with the same URI to a 
	 * multiple cardinality object property, such as xref,
	 * feature, dataSource, etc (one will have to explicitly 
	 * replace instead).
	 * 
	 * @param o
	 * @return
	 */
	@Override
	public int hashCode() {
        return uri == null ? super.hashCode() : uri.hashCode();
    }

	
	/**
	 * {@inheritDoc}
	 * 
	 * For the sake of BioPAX Model integrity and completeness,
	 * BioPAX objects having the same URI are considered
	 * equal (and equivalent) always.
	 * 
	 * @param o
	 * @return
	 */
	@Override
    public boolean equals(Object o)
    {
        boolean value = false;

        if (this == o) {
            value = true;
        }
        else if (o instanceof BioPAXElement) {
            final BioPAXElement that = (BioPAXElement) o;
            value = this.uri.equals(that.getRDFId());
        }
        
        return value;
    }
    
}

