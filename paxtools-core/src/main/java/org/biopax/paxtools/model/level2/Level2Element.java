package org.biopax.paxtools.model.level2;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 9, 2008
 * Time: 11:41:04 PM
 *
 */
public interface Level2Element extends BioPAXElement
{
    Set<String> getCOMMENT();

    void setCOMMENT(Set<String> COMMENT);

    void addCOMMENT(String COMMENT);

    void removeCOMMENT(String COMMENT);
}