package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelCoverageTest
{
    @Test
    public void creationMethods() {
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