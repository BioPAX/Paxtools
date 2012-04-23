package org.biopax.paxtools.causality;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class FeatureCollectorTest
{
	@Test
	@Ignore
	public void testCollection() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/all.owl"));

		FeatureCollector fc = new FeatureCollector();
		Map<EntityReference,Set<ModificationFeature>> all = fc.collectFeatures(model, false);
		clearEmpty(all);
		Map<EntityReference, Set<ModificationFeature>> act = fc.collectFeatures(model, true);
		clearEmpty(act);

		Map<EntityReference, Set<String>> typesAll = convertToTypes(all);
		Map<EntityReference, Set<String>> typesAct = convertToTypes(act);

		for (EntityReference er : typesAct.keySet())
		{
			Set<String> actSet = typesAct.get(er);
			Set<String> allSet = typesAll.get(er);
			assert allSet.size() >= actSet.size();
			if (allSet.size() == actSet.size()) continue;
			System.out.println("-------\n" + er.getDisplayName());
			for (String s : actSet)
			{
				System.out.print(s + "\t");
			}
			System.out.println();
			for (String s : allSet)
			{
				if (!actSet.contains(s)) System.out.print(s + "\t");
			}
			System.out.println();
		}
		
		System.out.println();
	}
	
	private void clearEmpty(Map<EntityReference,Set<ModificationFeature>> map)
	{
		for (EntityReference er : new HashSet<EntityReference> (map.keySet()))
		{
			if (map.get(er).isEmpty()) map.remove(er);
		}
	}
	
	private Map<EntityReference, Set<String>> convertToTypes(
		Map<EntityReference, Set<ModificationFeature>> map) 
	{
		Map<EntityReference, Set<String>> types = new HashMap<EntityReference, Set<String>>();

		for (EntityReference er : map.keySet())
		{
			types.put(er, new HashSet<String>());
			for (ModificationFeature mf : map.get(er))
			{
				if (mf.getModificationType() != null)
					types.get(er).add(mf.getModificationType().toString());
			}
		}
		return types;
	}
}
