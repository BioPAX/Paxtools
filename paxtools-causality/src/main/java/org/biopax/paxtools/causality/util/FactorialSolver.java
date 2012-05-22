package org.biopax.paxtools.causality.util;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class FactorialSolver
{
	private List<Integer> nom;
	private List<Integer> denom;

	/**
	 * Solves (nom[0]! x nom[1]! x nom[2]! ...) / (denom[0]! x denom[1]! x denom[2]! ...)
	 * 
	 * @param nom
	 * @param denom
	 */
	public FactorialSolver(List<Integer> nom, List<Integer> denom)
	{
		this.nom = nom;
		this.denom = denom;
		clearIneffective(nom);
		clearIneffective(denom);
		clearOverlap(nom, denom);
	}
	
	public static double solve(List<Integer> nom, List<Integer> denom)
	{
		FactorialSolver solver = new FactorialSolver(nom, denom);
		return solver.solve();
	}
	
	public double solve()
	{
		// Comparator for reverse sorting
		Comparator<Integer> comparator = new Comparator<Integer>()
		{
			@Override
			public int compare(Integer int1, Integer int2)
			{
				return int2 - int1;
			}
		};

		Collections.sort(nom, comparator);
		Collections.sort(denom, comparator);
		
		List<Integer> up = new ArrayList<Integer>();
		List<Integer> down = new ArrayList<Integer>();

		int size = Math.min(nom.size(), denom.size());
		for (int i = 0; i < size; i++)
		{
			Integer n = nom.get(i);
			Integer d = denom.get(i);
			
			if (n > d)
			{
				for (int j = n; j > d; j--)
				{
					up.add(j);
				}
			}
			else // d > n
			{
				for (int j = d; j > n; j--)
				{
					down.add(j);
				}
			}
		}
		
		if (nom.size() > size)
		{
			for (int i = size; i < nom.size(); i++)
			{
				Integer n = nom.get(i);

				for (int j = 2; j <= n; j++)
				{
					up.add(j);
				}
			}
		}
		else if (denom.size() > size)
		{
			for (int i = size; i < denom.size(); i++)
			{
				Integer n = denom.get(i);

				for (int j = 2; j <= n; j++)
				{
					down.add(j);
				}
			}
		}
		
		Collections.sort(up, comparator);
		Collections.sort(down, comparator);
		
		size = Math.min(up.size(), down.size());

		double result = 1D;
		
		for (int i = 0; i < size; i++)
		{
			Integer n = up.get(i);
			Integer d = down.get(i);

			result *= n / (double) d;
		}

		if (up.size() > size)
		{
			for (int i = size; i < up.size(); i++)
			{
				Integer n = up.get(i);
				result *= n;
			}
		}
		else if (down.size() > size)
		{
			for (int i = size; i < down.size(); i++)
			{
				Integer d = down.get(i);
				result /= d;
			}
		}
		return result;
	}
	
	private void clearOverlap(List<Integer> a, List<Integer> b)
	{
		for (Integer x : new ArrayList<Integer>(a))
		{
			if (b.contains(x))
			{
				a.remove(x);
				b.remove(x);
			}
		}
	}

	private void clearIneffective(List<Integer> list)
	{
		Iterator<Integer> iter = list.iterator();
		while (iter.hasNext())
		{
			Integer n = iter.next();
			if (n < 2) iter.remove();
		}
	}
}
