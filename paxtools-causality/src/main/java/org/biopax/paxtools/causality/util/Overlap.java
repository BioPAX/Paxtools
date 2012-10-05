package org.biopax.paxtools.causality.util;

import org.biopax.paxtools.causality.model.Alteration;
import org.biopax.paxtools.causality.model.Change;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class Overlap
{
	/**
	 * Calculates the p-value for getting o or less overlaps by chance.
	 *
	 * @param n
	 * @param a
	 * @param b
	 * @param o
	 * @return
	 */
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
		if (o < b-(n-a)) throw new IllegalArgumentException("o cannot be lower than b-(n-a)");

		if (n == 0) return 1;
		
		if (b > a)
		{
			int t = b;
			b = a;
			a = t;
		}

		double pval = 0;

		for (int i = 0; i <= o; i++)
		{
			pval += calcProb(n, a, b, i);
		}
		return pval;
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
		System.out.println("pval = " + calcPVal(9, 6, 6, 3));
	}

	public static double calcAlterationOverlapPval(Change[] alt1, Change[] alt2)
	{
		return calcAlterationOverlapPval(alt1, alt2, null);
	}

	public static double calcAlterationOverlapPval(Change[] alt1, Change[] alt2, boolean[] use)
	{
		assert alt1.length == alt2.length;
		assert use == null || use.length == alt1.length;

		int n = 0;
		int cnt1 = 0;
		int cnt2 = 0;
		int overlap = 0;

		for (int i = 0; i < alt1.length; i++)
		{
			if (use != null && !use[i]) continue;
			if (alt1[i].isAbsent() || alt2[i].isAbsent()) continue;

			n++;
			
			boolean a1 = alt1[i].isAltered();
			boolean a2 = alt2[i].isAltered();

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

		return calcPVal(n, cnt1, cnt2, overlap);
	}
}
