package org.biopax.paxtools.trove;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.BPCollections;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class TSetBuilderTest
{
	@Test
	public void testModelMemoryLoad() throws InvocationTargetException, IllegalAccessException
	{
		MockFactory factory = new MockFactory(BioPAXLevel.L3);
		Model model = factory.createModel();
		BPCollections.setProvider(new TProvider());
		//test reflectively create
		EditorMap map = SimpleEditorMap.L3;
		int i = 0;
		for (Class<? extends BioPAXElement> aClass : map.getKnownSubClassesOf(BioPAXElement.class))
		{
			if (factory.canInstantiate(aClass))
			{
				for (int j = 0; j < 1000; j++)
				{
					model.addNew(aClass, "mock://ModelCoverageTest/id/" + i++);

				}

			}
		}

	}
}
