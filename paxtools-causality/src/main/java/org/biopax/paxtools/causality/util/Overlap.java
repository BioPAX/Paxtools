package org.biopax.paxtools.causality.util;

import org.biopax.paxtools.causality.model.Alteration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class Overlap
{
	public static double calcPVal(int n, int a, int b, int o)
	{
		// Make sure that all parameters are non-negative
		
		if (n < 0 || a < 0 || b < 0 || o < 0) throw new IllegalArgumentException(
			"All parameters should be non-negative. n="+n+" a="+a+" b="+b+" o="+o);
		
		// Make sure that n >= a >= b >= o

		if (o > a) throw new IllegalArgumentException("Overlap cannot be more than a");
		if (o > b) throw new IllegalArgumentException("Overlap cannot be more than b");
		if (a > n) throw new IllegalArgumentException("a cannot be greater than sample size");
		if (b > n) throw new IllegalArgumentException("b cannot be greater than sample size");

		if (b > a)
		{
			int t = b;
			b = a;
			a = t;
		}

		double e = (a * b) / (double) n;

		double pval = 0;

		if (o >= e)
		{
			for (int i = o; i <= b; i++)
			{
				pval += calcProb(n, a, b, i);
			}
			return pval;
		}
		else // (o < e)
		{
			for (int i = 0; i <= o; i++)
			{
				pval += calcProb(n, a, b, i);
			}
			return -pval;
		}
	}

	/**
	 * Calculated the probability that sets a and b have exactly x overlaps.
	 * @param n
	 * @param a
	 * @param b
	 * @param x
	 * @return
	 */
	protected static double calcProb(int n, int a, int b, int x)
	{
		FactorialSolver s = new FactorialSolver(
			new ArrayList<Integer>(Arrays.asList(a, b, (n-a), (n-b))),
			new ArrayList<Integer>(Arrays.asList(n, x, (a-x), (b-x), (n-a-b+x))));
		
		return s.solve();
	}

	public static void main(String[] args)
	{
		System.out.println("pval = " + calcPVal(100, 20, 20, 10));
	}

	public static double calcAlterationOverlapPval(
		List<Set<Alteration>> alterList1, List<Set<Alteration>> alterList2)
	{
		assert alterList1.size() == alterList2.size();

		int cnt1 = 0;
		int cnt2 = 0;
		int overlap = 0;

		for (int i = 0; i < alterList1.size(); i++)
		{
			boolean a1 = !alterList1.get(i).isEmpty();
			boolean a2 = !alterList2.get(i).isEmpty();

			if (a1)
			{
				cnt1++;

				if (a2)
				{
					cnt2++;
					overlap++;
				}
			}
			else if (a2) cnt2++;
		}

		return calcPVal(alterList1.size(), cnt1, cnt2, overlap);
	}
}
