/*
 * BioPAXElementProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */
package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Proxy for BioPAXElement
 */
// MySQL
//  TABLE_PER_CLASS: syntax error on EntityManager.find()
//  JOINED: too many JOIN (over 31) error on EntityManager.find()
//  SINGLE_TABLE: ok
// PostgreSQL
//  TABLE_PER_CLASS: ok
//  JOINED: not tested
//  SINGLE_TABLE: ok
@javax.persistence.Entity(name = "l3biopaxelement")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
	@NamedQuery(name="org.biopax.paxtools.proxy.level3.elementByRdfId",
		query="from org.biopax.paxtools.proxy.level3.BioPAXElementProxy as l3element where upper(l3element.RDFId) = upper(:rdfid)")
})
public abstract class BioPAXElementProxy implements BioPAXElement {
	/**
	 * target object
	 */
	BioPAXElement object = null;

	public final static String SEARCH_FIELD_SOURCE_NAME = "source_name";
	public final static String SEARCH_INDEX_NAME = "paxtools";
	public final static String SEARCH_FIELD_KEYWORD ="keyword";
	public final static String SEARCH_FIELD_NAME = "name";
	public final static String SEARCH_FIELD_SYNONYMS = "synonyms";
	public final static String SEARCH_FIELD_TERM = "term";
	public final static String SEARCH_FIELD_EC_NUMBER = "ec_number";
	public final static String SEARCH_FIELD_SEQUENCE = "sequence";
	public final static String SEARCH_FIELD_XREF_DB = "xref_db";
	public final static String SEARCH_FIELD_XREF_ID = "xref_id";
	public final static String SEARCH_FIELD_AVAILABILITY = "availability";
	public final static String SEARCH_FIELD_COMMENT = "comment";

	protected BioPAXElementProxy() {
		this.object = 
			BioPAXLevel.L3.getDefaultFactory()
				.reflectivelyCreate(this.getModelInterface());
	}

	//2010.02.25 - having back the proxyID
	Long proxyId = 0L;
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="proxy_id")
	public Long getProxyId() {
		return proxyId;
	}

	public void setProxyId(Long value) {
		proxyId = value;
	}
	
	@Column(name="rdfid", length = 500, nullable=false, unique=true)
	public String getRDFId() {
		return object.getRDFId();
	}

	public void setRDFId(String id) {
		object.setRDFId(id);
	}
	
	// 2007.05.16
	public boolean equals(Object o) {
		return object.equals(o);
	}

	public int hashCode() {
		return object.hashCode();
	}
	
	public int equivalenceCode() {
		return object.equivalenceCode();
	}

	public boolean isEquivalent(BioPAXElement element) {
		return object.isEquivalent(element);
	}

	///////////////////////////////////
	// 2007.09.04
	///////////////////////////////////
	protected String doubleToString(Double d) {
		try {
			return d.toString();
		} catch (Exception e) {
		}
		return "NaN";
	}

	protected Double stringToDouble(String s) {
		try {
			return Double.valueOf(s);
		} catch (Exception e) {
		}
		return Double.NaN;
	}

	protected Set<String> doubleSetToStringSet(Set<Double> ds) {
		Set<String> result = new HashSet<String>();
		for (Double d : ds) {
			result.add(doubleToString(d));
		}
		return result;
	}

	protected Set<Double> stringSetToDoubleSet(Set<String> ss) {
		Set<Double> result = new HashSet<Double>();
		for (String s : ss) {
			result.add(stringToDouble(s));
		}
		return result;
	}

	protected String floatToString(Float f) {
		try {
			return f.toString();
		} catch (Exception e) {
		}
		return "NaN";
	}

	protected Float stringToFloat(String s) {
		try {
			return Float.valueOf(s);
		} catch (Exception e) {
		}
		return Float.NaN;
	}

	protected Set<String> floatSetToStringSet(Set<Float> ds) {
		Set<String> result = new HashSet<String>();
		for (Float d : ds) {
			result.add(floatToString(d));
		}
		return result;
	}

	protected Set<Float> stringSetToFloatSet(Set<String> ss) {
		Set<Float> result = new HashSet<Float>();
		for (String s : ss) {
			result.add(stringToFloat(s));
		}
		return result;
	}
	
}
