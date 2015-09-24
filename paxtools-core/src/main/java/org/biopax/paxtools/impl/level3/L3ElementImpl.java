package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.util.*;
import java.util.Set;


/**
 * Base BioPAX Level3 element.
 *
 */
public abstract class L3ElementImpl extends BioPAXElementImpl
        implements Level3Element
{
    private Set<String> comment;
    
    public L3ElementImpl()
    {
        this.comment = BPCollections.I.createSet();
    }

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
