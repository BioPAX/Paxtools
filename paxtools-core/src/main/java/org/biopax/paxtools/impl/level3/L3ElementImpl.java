package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.util.SetStringBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;

/**
 * Base BioPAX Level3 element.
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class L3ElementImpl extends BioPAXElementImpl
        implements Level3Element
{
    private Set<String> comment;

    public L3ElementImpl()
    {
        this.comment = new HashSet<String>();
    }

    @ElementCollection
	@Field(name = BioPAXElementImpl.SEARCH_FIELD_COMMENT, index = Index.TOKENIZED)
	@FieldBridge(impl=SetStringBridge.class)
	@Column(columnDefinition="LONGTEXT")
    public Set<String> getComment()
    {
        return this.comment;
    }

    public void setComment(Set<String> comment)
    {
        this.comment = comment;
    }

    public void addComment(String COMMENT)
    {
        if(COMMENT != null && COMMENT.length() > 0)
        	this.comment.add(COMMENT);
    }

    public void removeComment(String COMMENT)
    {
    	if(COMMENT != null)
    		this.comment.remove(COMMENT);
    }

}
