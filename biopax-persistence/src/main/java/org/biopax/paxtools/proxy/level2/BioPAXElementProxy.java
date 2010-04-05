/*
 * BioPAXElementProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
//import org.biopax.paxtools.model.level2.Level2Element;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
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
@Entity(name = "l2biopaxelement")
//@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
//@Inheritance(strategy=InheritanceType.JOINED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Indexed(index = BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class BioPAXElementProxy implements BioPAXElement, Serializable
{
	/**
	 * target object
	 */
	BioPAXElement object = null;

	/**
	 * key ID
	 */
	long proxyId = 0;
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

	protected BioPAXElementProxy()
	{
		this.object = BioPAXLevel.L2.getDefaultFactory().reflectivelyCreate(
			this.getModelInterface());
	}

	
	public boolean equals(Object o)
	{
		return object.equals(o);
	}

	public int hashCode()
	{
		return object.hashCode();
	}

/*	
	public BioPAXElement implObject(BioPAXElement o) {
		try {
			if (BioPAXElementProxy.class.isInstance(o)) {
				return ((BioPAXElementProxy)o).implObject();
			}
		}
		catch (Exception e) {
		}
		return o;
	}

	public BioPAXElement implObject() {
		return object;
	}
*/

	@Id
	@DocumentId
	public long getProxyId()
	{
		return proxyId;
	}

	public void setProxyId(long value)
	{
		proxyId = value;
	}


	@Basic
	@Column(columnDefinition = "text")
	public String getRDFId()
	{
		return object.getRDFId();
	}

	@Transient
	public Class getModelInterface()
	{
		return object.getModelInterface();
	}

	public void setRDFId(String id)
	{
		object.setRDFId(id);
	}

	public int equivalenceCode()
	{
		return object.equivalenceCode();
	}

	public boolean isEquivalent(BioPAXElement element)
	{
		return object.isEquivalent(element);
	}


	protected String doubleToString(Double d)
	{
		try
		{
			return d.toString();
		}
		catch (Exception e)
		{
		}
		return "NaN";
	}

	protected Double stringToDouble(String s)
	{
		try
		{
			return Double.valueOf(s);
		}
		catch (Exception e)
		{
		}
		return Double.NaN;
	}

	protected Set<String> doubleSetToStringSet(Set<Double> ds)
	{
		Set<String> result = new HashSet<String>();
		for (Double d : ds)
		{
			result.add(doubleToString(d));
		}
		return result;
	}

	protected Set<Double> stringSetToDoubleSet(Set<String> ss)
	{
		Set<Double> result = new HashSet<Double>();
		for (String s : ss)
		{
			result.add(stringToDouble(s));
		}
		return result;
	}

	protected String floatToString(Float f)
	{
		try
		{
			return f.toString();
		}
		catch (Exception e)
		{
		}
		return "NaN";
	}

	protected Float stringToFloat(String s)
	{
		try
		{
			return Float.valueOf(s);
		}
		catch (Exception e)
		{
		}
		return Float.NaN;
	}

}

