/*
 * Level3ElementProxy.java
 *
 * 2008.02.26 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.Level3Element;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;

@javax.persistence.Entity(name = "l3level3element")
public abstract class Level3ElementProxy extends BioPAXElementProxy 
	implements Level3Element 
{
	protected Level3ElementProxy() {
		// not get object. because this object has not factory.
	}

	@CollectionOfElements
	@Column(name = "comment_x", columnDefinition = "text")
	@FieldBridge(impl = StringSetBridge.class)
	@Field(name = BioPAXElementProxy.SEARCH_FIELD_COMMENT,
		index = Index.TOKENIZED)
	public Set<String> getComment()
	{
		return ((Level3Element)object).getComment();
	}

	public void addComment(String COMMENT)
	{
		((Level3Element)object).addComment(COMMENT);
	}

	public void removeComment(String COMMENT)
	{
		((Level3Element)object).removeComment(COMMENT);
	}

	public void setComment(Set<String> COMMENT)
	{
		((Level3Element)object).setComment(COMMENT);
	}
}

