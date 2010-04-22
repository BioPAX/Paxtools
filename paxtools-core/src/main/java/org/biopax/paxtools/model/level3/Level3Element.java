package org.biopax.paxtools.model.level3;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 9, 2008
 * Time: 11:41:04 PM
 *
 */
public interface Level3Element extends BioPAXElement
{
    public Set<String> getComment();

    public void addComment(String comment);

    public void removeComment(String comment);
}
