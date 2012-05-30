package org.biopax.paxtools.causality.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class Binomial
{
	/**
	 * Exact p-value for the deviation.
	 */
	public static double getPval(int count1, int count2)
	{
		int heads = count1;
		int tosses = count1 + count2;

		if (heads * 2 >= tosses - 1 && heads * 2 <= tosses + 1) return 1;
		if (heads > (tosses / 2D)) heads = tosses - heads;

		double pval = 0;
		
		List<Integer> twos = new ArrayList<Integer>();
		for (int i = 0; i < tosses; i++)
		{
			twos.add(2);
		}

		for (int i = 0; i <= heads; i++)
		{
			List<Integer> nom = new ArrayList<Integer>();
			nom.add(tosses);

			List<Integer> denom = new ArrayList<Integer>(twos);
			denom.add(i);
			denom.add(tosses-i);
			

			pval += FactorialSolver.solve(nom, denom);
		}
		pval *= 2;

		return pval;
	}

	public static void main(String[] args)
	{
		int k1 = 0;
		int k2 = 6;

		double pval = getPval(k1, k2);
		System.out.println("pval = " + pval);
		System.out.println("simu = " + simulate(k1, k1 + k2));
	}

	public static double simulate(int heads, int tosses)
	{
		double mid = tosses / 2D;
		if (heads > mid) heads = tosses - heads;

		int count  = 0;
		int trials = 10000;

		for (int i = 0; i < trials; i++)
		{
			int h = generateRand(tosses);
			if (h > mid) h = tosses - h;
			if (h <= heads) count++;
		}

		return count / (double) trials;
	}

	public static int generateRand(int tosses)
	{
		int h = 0;
		for (int j = 0; j < tosses; j++)
		{
			if (Math.random() < .5) h++;
		}
		return h;
	}
}
