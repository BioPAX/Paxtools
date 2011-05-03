package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class ModelCoverageTest
{
    @Test 
    public void testCreationMethods() throws InvocationTargetException, IllegalAccessException
    {
        MockFactory factory = new MockFactory(BioPAXLevel.L3);

        //test reflectively create
        EditorMap map = SimpleEditorMap.L3;
        int i = 0;
        for (Class<? extends BioPAXElement> aClass : map.getKnownSubClassesOf(BioPAXElement.class))
        {
            if (factory.canInstantiate(aClass))
            {
                BioPAXElement bpe = factory.create(aClass, "mock://ModelCoverageTest/id/" + i++);
                assertNotNull(bpe);
                assertTrue(aClass.isAssignableFrom(bpe.getClass()));
            }
        }
	}
}