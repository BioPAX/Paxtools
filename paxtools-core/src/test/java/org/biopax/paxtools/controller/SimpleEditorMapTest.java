package org.biopax.paxtools.controller;
/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 25, 2008
 * Time: 1:03:41 AM
 */

import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXLevel;
import org.junit.Test;

public class SimpleEditorMapTest
{

    @Test
    public void testSimpleEditorMap() throws Exception
    {
        for (BioPAXLevel level : BioPAXLevel.values())
        {
            SimpleEditorMap simpleEditorMap = new SimpleEditorMap(level);
	        System.out.println("initialized for " + level );
	        System.out.println(simpleEditorMap);

        }


    }
}