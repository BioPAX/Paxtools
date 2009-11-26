package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 12, 2008
 * Time: 11:08:01 AM
 */
abstract class BioPAXLevel2ElementImpl extends BioPAXElementImpl
        implements BioPAXElement
{
    private Set<String> COMMENT;

    BioPAXLevel2ElementImpl()
    {
        this.COMMENT = new HashSet<String>();
    }

    public Set<String> getCOMMENT()
    {
        return this.COMMENT;
    }

    public void setCOMMENT(Set<String> COMMENT)
    {
        this.COMMENT = COMMENT;
    }

    public void removeCOMMENT(String COMMENT)
    {
        this.COMMENT.remove(COMMENT);
    }

    public void addCOMMENT(String COMMENT)
    {
        this.COMMENT.add(COMMENT);
    }

}