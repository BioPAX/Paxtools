package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Level3Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 12, 2008
 * Time: 11:08:01 AM
 *
 **/
abstract class L3ElementImpl extends BioPAXElementImpl
        implements Level3Element
{
    private Set<String> comment;

    L3ElementImpl()
    {
        this.comment = new HashSet<String>();
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
        this.comment.add(COMMENT);
    }

    public void removeComment(String COMMENT)
    {
        this.comment.remove(COMMENT);
    }

}
