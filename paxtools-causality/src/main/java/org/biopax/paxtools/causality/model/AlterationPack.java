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

	protected int size;

	public static final Alteration[] priority_nonGenomic = new Alteration[]{
		Alteration.PROTEIN_LEVEL, Alteration.EXPRESSION};

	public static final Alteration[] priority_genomic = new Alteration[]{
		Alteration.MUTATION, Alteration.COPY_NUMBER, Alteration.METHYLATION};

	public static final Alteration[] priority = new Alteration[]{
		Alteration.MUTATION, Alteration.PROTEIN_LEVEL, Alteration.EXPRESSION,
		Alteration.COPY_NUMBER, Alteration.METHYLATION};

	public AlterationPack()
	{
		map = new HashMap<Alteration, Change[]>();
		size = 0;
	}

	public int getSize()
	{
		return size;
	}

	public void put(Alteration alt, Change[] changes)
	{
		if (changes.length == 0) throw new IllegalArgumentException("Changes array is empty.");

		if (containsNull(changes))
			throw new IllegalArgumentException("Changes array contains null change.");

		if (size > 0 && size != changes.length)
		{
			throw new IllegalArgumentException("Length of the change array should be equal to" +
				"the existing change arrays in the pack. Parameter length = " + changes.length +
				". In the pack = " + map.values().iterator().next().length);
		}

		map.put(alt, changes);
		size = changes.length;
	}
	
	private boolean containsNull(Change[] changes)
	{
		for (Change ch : changes)
		{
			if (ch == null) return true;
		}
		return false;
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

	public boolean isAltered(Alteration alt)
	{
		if (!map.containsKey(alt)) return false;
		for (Change ch : map.get(alt))
		{
			if (ch.isAltered()) return true;
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
	
	public Change getChange(Alteration alt, int index)
	{
		if (!map.containsKey(alt)) return Change.NO_DATA;
		
		Change[] changes = map.get(alt);
		if (changes.length <= index)
			throw new IllegalArgumentException("Index is out of loaded cases. index = " + index +
				" case size = " + changes.length);
		
		return changes[index];
	}

	/**
	 *
	 * @param priority
	 * @param index
	 * @return
	 */
	public Change getCumulativeChange(Alteration[] priority, int index)
	{
		boolean dataExists = false;
		for (Alteration alt : priority)
		{
			Change ch = getChange(alt, index);
			if (!ch.isAbsent()) dataExists = true;
			if (ch.isAltered()) return ch;
		}
		return dataExists ? Change.NO_CHANGE : Change.NO_DATA;
	}
	
	public void complete()
	{
		Change[] changes = new Change[map.values().iterator().next().length];

		for (int i = 0; i < changes.length; i++)
		{
			changes[i] = getCumulativeChange(priority, i);
		}

		map.put(Alteration.ANY, changes);
		
		changes = new Change[changes.length];

		for (int i = 0; i < changes.length; i++)
		{
			changes[i] = getCumulativeChange(priority_nonGenomic, i);
		}

		map.put(Alteration.NON_GENOMIC, changes);
	}
	
	public double getParallelChangeRatio(AlterationPack pack, boolean similar)
	{
		int count = 0;

		for (int i = 0; i < size; i++)
		{
			Change ch1 = getChange(Alteration.ANY, i);
			Change ch2 = pack.getChange(Alteration.ANY, i);

			if (ch1.isAltered() && ch2.isAltered() &&
				((similar && ch1 == ch2) || (!similar && ch1 != ch2)))  count++;
		}

		return count / (double) size;
	}
}
