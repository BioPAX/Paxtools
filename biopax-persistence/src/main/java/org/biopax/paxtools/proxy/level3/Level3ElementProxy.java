/*
 * Level3ElementProxy.java
 *
 * 2008.02.26 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.Level3Element;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;

import java.util.Set;

import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.biopax.paxtools.proxy.StringSetBridge;

//MySQL
//TABLE_PER_CLASS: syntax error on EntityManager.find()
//JOINED: too many JOIN (over 31) error on EntityManager.find()
//SINGLE_TABLE: ok
//PostgreSQL
//TABLE_PER_CLASS: ok
//JOINED: not tested
//SINGLE_TABLE: ok
@javax.persistence.Entity(name = "l3element")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
@NamedQuery(name="org.biopax.paxtools.proxy.level3.elementByRdfId",
			query="from org.biopax.paxtools.proxy.level3.Level3ElementProxy as l3el where upper(l3el.RDFId) = upper(:rdfid)"),
@NamedQuery(name="org.biopax.paxtools.proxy.level3.elementByRdfIdEager",
			query="from org.biopax.paxtools.proxy.level3.Level3ElementProxy as l3el fetch all properties where upper(l3el.RDFId) = upper(:rdfid)")

})
public abstract class Level3ElementProxy<T extends Level3Element> extends BioPAXElementProxy<T>
	implements Level3Element 
{

	private static final long serialVersionUID = 3515758244193005906L;

	public Level3ElementProxy()
	{
		this(BioPAXLevel.L3.getDefaultFactory());
	}

	protected Level3ElementProxy(BioPAXFactory factory)
	{
		super(factory);
	}

	private Long proxyId = 0L;
	
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
	
	@CollectionOfElements
	@Column(name = "comment_x", columnDefinition = "text")
	@FieldBridge(impl = StringSetBridge.class)
	@Field(name = BioPAXElementProxy.SEARCH_FIELD_COMMENT,
		index = Index.TOKENIZED)
	public Set<String> getComment()
	{
		return object.getComment();
	}

	public void addComment(String COMMENT)
	{
		object.addComment(COMMENT);
	}

	public void removeComment(String COMMENT)
	{
		object.removeComment(COMMENT);
	}

	public void setComment(Set<String> COMMENT)
	{
		object.setComment(COMMENT);
	}
}

