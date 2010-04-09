/*
 * Level2ElementProxy.java
 *
 * 2008.02.26 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.biopax.paxtools.proxy.StringSetBridge;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;

import java.util.Set;

@javax.persistence.Entity(name = "l2element")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
@NamedQuery(name="org.biopax.paxtools.proxy.level2.elementByRdfId",
			query="from org.biopax.paxtools.proxy.level2.Level2ElementProxy as l2el where upper(l2el.RDFId) = upper(:rdfid)"),
@NamedQuery(name="org.biopax.paxtools.proxy.level2.elementByRdfIdEager",
			query="from org.biopax.paxtools.proxy.level2.Level2ElementProxy as l2el fetch all properties where upper(l2el.RDFId) = upper(:rdfid)")

})
public abstract class Level2ElementProxy<T extends Level2Element> extends BioPAXElementProxy<T>
	implements Level2Element 
{
    public Level2ElementProxy()
    {
        this(BioPAXLevel.L2.getDefaultFactory());
    }

    protected Level2ElementProxy(BioPAXFactory factory)
    {
        super(factory);
    }


	private Long proxyId = 0L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "proxy_id")
	public Long getProxyId() {
		return proxyId;
	}

	public void setProxyId(Long value) {
		proxyId = value;
	}

	@Column(name = "rdfid", length = 500, nullable = false, unique = true)
	public String getRDFId() {
		return object.getRDFId();
	}

	public void setRDFId(String id) {
		object.setRDFId(id);
	}

	@CollectionOfElements
	@Column(name = "comment_x", columnDefinition = "text")
	@FieldBridge(impl = StringSetBridge.class)
	@Field(name = BioPAXElementProxy.SEARCH_FIELD_COMMENT, index = Index.TOKENIZED)
	public Set<String> getCOMMENT() {
		return object.getCOMMENT();
	}

    public boolean isEquivalent(BioPAXElement element)
    {
       return object.isEquivalent(element);
    }

    public void addCOMMENT(String COMMENT)
	{
		object.addCOMMENT(COMMENT);
	}

	public void removeCOMMENT(String COMMENT)
	{
		object.removeCOMMENT(COMMENT);
	}


	public void setCOMMENT(Set<String> COMMENT)
	{
		object.setCOMMENT(COMMENT);
	}
}

