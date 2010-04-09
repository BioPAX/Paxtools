/*
 * BioPAXElementProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 * 
 * [moved, re-factored by Igor Rodchenkov, 04/2010]
 */
package org.biopax.paxtools.proxy;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.HashSet;
import java.util.Set;

/**
 * Base Proxy for persistent BioPAX Elements
 */
public abstract class BioPAXElementProxy implements BioPAXElement {
	/**
	 * target object
	 */
	protected BioPAXElement object = null;

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
	
	public String toString() {
		return object.toString();
	}
	
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
