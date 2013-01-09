package org.biopax.paxtools.causality.model;

import org.biopax.paxtools.causality.util.Summary;

import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class AlterationPack
{
	protected Map<Alteration, Change[]> map;

	protected int size;

	protected String id;

	public static final Alteration[] priority_nonGenomic = new Alteration[]{
		Alteration.PROTEIN_LEVEL, Alteration.EXPRESSION};

	public static final Alteration[] priority_genomic = new Alteration[]{
		Alteration.MUTATION, Alteration.COPY_NUMBER, Alteration.METHYLATION};

	public static final Alteration[] priority = new Alteration[]{
		Alteration.MUTATION, Alteration.PROTEIN_LEVEL, Alteration.EXPRESSION,
		Alteration.COPY_NUMBER, Alteration.METHYLATION};

	public AlterationPack(String id)
	{
		this.id = id;
		map = new HashMap<Alteration, Change[]>();
		size = 0;
	}

	public int getSize()
	{
		return size;
	}

	public String getId()
	{
		return id;
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
	
	public Set<Alteration> getAlterationTypes()
	{
		return map.keySet();
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

	public int getAlteredCount(Alteration alt)
	{
		if (!map.containsKey(alt)) return 0;

		int i = 0;
		for (Change ch : map.get(alt))
		{
			if (ch.isAltered()) i++;
		}
		return i;
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
	
	public double getAlteredRatio(Alteration alt)
	{
		int cnt = 0;

		for (Change change : map.get(alt))
		{
			if (change.isAltered()) cnt++;
		}

		return cnt / (double) size;
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
	
	public int countAltered(Alteration key)
	{
		int cnt = 0;
		for (Change ch : get(key))
		{
			if (ch.isAltered()) cnt++;
		}
		return cnt;
	}

	public double calcAlteredRatio(Alteration key)
	{
		double cnt = countAltered(key);
		return cnt / get(key).length;
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

		if (containsAlterationType(priority_nonGenomic))
		{
			changes = new Change[changes.length];

			for (int i = 0; i < changes.length; i++)
			{
				changes[i] = getCumulativeChange(priority_nonGenomic, i);
			}

			map.put(Alteration.NON_GENOMIC, changes);
		}

		if (containsAlterationType(priority_genomic))
		{
			changes = new Change[changes.length];

			for (int i = 0; i < changes.length; i++)
			{
				changes[i] = getCumulativeChange(priority_genomic, i);
			}

			map.put(Alteration.GENOMIC, changes);
		}

		Change[] changesAct = new Change[changes.length];
		Change[] changesInh = new Change[changes.length];
		Change[] mut = get(Alteration.MUTATION);
		Change[] cnc = get(Alteration.COPY_NUMBER);
		Change[] exp = get(Alteration.EXPRESSION);

		for (int i = 0; i < changes.length; i++)
		{
			if (mut != null && mut[i].isAltered()) 
			{
				if (mut[i] != Change.INHIBITING) changesAct[i] = Change.ACTIVATING;
				changesInh[i] = Change.INHIBITING;
			}
			else if (cnc != null && cnc[i].isAltered())
			{
				if (!(exp != null && cnc[i].isOpposing(exp[i])))
				{					
					if (cnc[i] == Change.ACTIVATING) changesAct[i] = Change.ACTIVATING;
					else if (cnc[i] == Change.INHIBITING) changesInh[i] = Change.INHIBITING;
				}
			}
			else if (exp != null && exp[i].isAltered())
			{
				if (exp[i] == Change.ACTIVATING) changesAct[i] = Change.ACTIVATING;
				else if (exp[i] == Change.INHIBITING) changesInh[i] = Change.INHIBITING;
			}
			
			if (changesAct[i] == null) changesAct[i] = Change.NO_CHANGE;
			if (changesInh[i] == null) changesInh[i] = Change.NO_CHANGE;
		}
		
		map.put(Alteration.ACTIVATING, changesAct);
		map.put(Alteration.INHIBITING, changesInh);
	}
	
	protected boolean containsAlterationType(Alteration[] alts)
	{
		for (Alteration alt : alts)
		{
			if (map.containsKey(alt)) return true;
		}
		return false;
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

	public List<Integer> getParallelChangedIndexes(AlterationPack pack, boolean similar,
		boolean thisIsUp)
	{
		List<Integer> inds = new ArrayList<Integer>();

		for (int i = 0; i < size; i++)
		{
			Change ch1 = getChange(Alteration.ANY, i);

			if ((thisIsUp && ch1 == Change.INHIBITING) || (!thisIsUp && ch1 == Change.ACTIVATING))
				continue;

			Change ch2 = pack.getChange(Alteration.ANY, i);

			if (ch1.isAltered() && ch2.isAltered() &&
				((similar && ch1 == ch2) || (!similar && ch1 != ch2)))
			{
				inds.add(i);
			}
		}
		return inds;
	}
	
	public Change[] getChangesMissingRemoved(AlterationPack pack, Alteration alt)
	{
		Change[] ch1 = map.get(alt);
		Change[] ch2 = pack.get(alt);
		
		assert ch1.length == ch2.length;

		boolean[] b = new boolean[ch1.length];
		int x = 0;
		for (int i = 0; i < ch1.length; i++)
		{
			if (!ch1[i].isAbsent() && !ch2[i].isAbsent())
			{
				x++;
				b[i] = true;
			}
			else b[i] = false;
		}
		Change[] ch = new Change[x];

		int j = 0;
		for (int i = 0; i < ch1.length; i++)
		{
			if (b[i])
			{
				ch[j++] = ch1[i];
				assert !(ch1[i].isAbsent() || ch2[i].isAbsent());
			}
		}

		assert j == ch.length;
		
		return ch;
	}
	
	public String getPrint(Alteration key)
	{
		Change[] ch = get(key);
		StringBuilder buf = new StringBuilder();
		for (Change c : ch)
		{
			buf.append(c.isAltered() ? "x" : c.isAbsent() ? " " : ".");
		}
		buf.append("  ").append(id);
		return buf.toString();
	}

	public String getPrint(Alteration key, List<Integer> order)
	{
		assert new HashSet<Integer>(order).size() == order.size();

		Change[] ch = get(key);
		StringBuilder buf = new StringBuilder();
		for (Integer o : order)
		{
			buf.append(ch[o].isAltered() ? "x" : ch[o].isAbsent() ? " " : ((key == Alteration.ACTIVATING && get(Alteration.INHIBITING)[o].isAltered()) || (key == Alteration.INHIBITING && get(Alteration.ACTIVATING)[o].isAltered())) ? ":" : ".");
		}
		for (int i = 0; i < ch.length; i++)
		{
			if (!order.contains(i))
				buf.append(ch[i].isAltered() ? "x" : ch[i].isAbsent() ? " " : ((key == Alteration.ACTIVATING && get(Alteration.INHIBITING)[i].isAltered()) || (key == Alteration.INHIBITING && get(Alteration.ACTIVATING)[i].isAltered())) ? ":" : ".");
		}


		buf.append("  ").append(id);
		return buf.toString();
	}
	
	public static void writeToFile(Map<String, AlterationPack> map, String filename) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

		for (AlterationPack pack : map.values())
		{
			for (Alteration alt : pack.getAlterationTypes())
			{
				if (alt.isSummary()) continue;
				writer.write(pack.getId() + "\t" + alt);
				for (Change c : pack.get(alt))
				{
					writer.write("\t" + c.getLetter());
				}
				writer.write("\n");
			}
		}
		
		writer.close();
	}
	
	public static Map<String, AlterationPack> readFromFile(String filename) throws IOException
	{
		Map<String, AlterationPack> map = new HashMap<String, AlterationPack>();

		BufferedReader reader = new BufferedReader(new FileReader(filename));

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");
			if (token.length < 3) continue;
			if (!map.containsKey(token[0])) map.put(token[0], new AlterationPack(token[0]));
			Change[] c = new Change[token.length - 2];

			for (int i = 2; i < token.length; i++)
			{
				c[i-2] = Change.getChange(token[i]);
			}
			Alteration type = Alteration.valueOf(token[1]);
//			if (type == Alteration.MUTATION) continue;
			if (type == Alteration.EXPRESSION) continue;
//			if (type == Alteration.COPY_NUMBER) continue;
			map.get(token[0]).put(type, c);
		}

		reader.close();

		for (AlterationPack pack : map.values())
		{
			pack.complete();
		}
		return map;
	}
}
