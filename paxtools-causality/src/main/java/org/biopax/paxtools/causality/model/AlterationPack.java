package org.biopax.paxtools.causality.model;

import org.biopax.paxtools.causality.util.Summary;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class AlterationPack
{
	protected Map<Alteration, Change[]> map;

	public AlterationPack()
	{
		map = new HashMap<Alteration, Change[]>();
	}
	
	public void put(Alteration alt, Change[] changes)
	{
		for (Change[] existing : map.values())
		{
			if (existing.length != changes.length)
			{
				throw new IllegalArgumentException("Length of the change array should be equal to" +
					"the existing change arrays in the pack. Parameter length = " + changes.length +
					". In the pack = " + existing.length);
			}
		}
		map.put(alt, changes);
	}
	
	public Change[] get(Alteration alt)
	{
		return map.get(alt);
	}
	
	public boolean isAltered()
	{
		for (Alteration alt : map.keySet())
		{
			for (Change ch : map.get(alt))
			{
				if (ch.isAltered()) return true;
			}
		}
		return false;
	}
	
	public double getAlteredRatio()
	{
		double[] cases = new double[map.values().iterator().next().length];

		for (Alteration alt : map.keySet())
		{
			Change[] changes = map.get(alt);
			for (int i = 0; i < changes.length; i++)
			{
				if (changes[i].isAltered()) cases[i] = 1;
			}
		}
		
		return Summary.mean(cases);
	}
}
