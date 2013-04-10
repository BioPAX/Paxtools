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

	private String _pk; // used for primary key

	
	public BioPAXElementImpl() {
		this.annotations = new HashMap<String, Object>();
	}
	

	// Primary Key
	// could not use names like: 'key' (SQL conflict), 'id', 'idx' (conflicts with the existing prop. in a child class), etc...
	// @GeneratedValue did not work (stateless session is unable to save/resolve object references and inverse props)
	@Id
	@Column(name="pk", length=32) // enough to save MD5 digest Hex.
	@Override
	public String getPk() {
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
        this._pk = md5hex(id);
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
    
}

