/*
 * Level2ElementProxy.java
 *
 * 2008.02.26 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.proxy.StringSetBridge;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Set;

@Entity(name = "l2level2element")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class Level2ElementProxy extends BioPAXElementProxy implements Level2Element, Serializable {
	protected Level2ElementProxy() {
		// not get object. because this object has not factory.
	}

	@CollectionOfElements
	@Column(name = "comment_x", columnDefinition = "text")
	@FieldBridge(impl = StringSetBridge.class)
	@Field(name = BioPAXElementProxy.SEARCH_FIELD_COMMENT,
		index = Index.TOKENIZED)
	public Set<String> getCOMMENT()
	{
		return ((Level2Element)object).getCOMMENT();
	}

    public boolean isEquivalent(BioPAXElement element)
    {
       return object.isEquivalent(element);
    }

    public void addCOMMENT(String COMMENT)
	{
		((Level2Element)object).addCOMMENT(COMMENT);
	}

	public void removeCOMMENT(String COMMENT)
	{
		((Level2Element)object).removeCOMMENT(COMMENT);
	}


	public void setCOMMENT(Set<String> COMMENT)
	{
		((Level2Element)object).setCOMMENT(COMMENT);
	}
}

