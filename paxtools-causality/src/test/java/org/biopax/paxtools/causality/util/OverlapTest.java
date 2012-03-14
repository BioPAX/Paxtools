package org.biopax.paxtools.causality.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Ozgun Babur
 */
public class OverlapTest
{
	@Test
	public void testOverlapAccuracy()
	{
		int n = 100;
		int a  = 30;
		int b = 40;

		System.out.println("overlap\tcalculated pval\tsimulated pval\tdifference");

		for (int o = 0; o <= Math.min(a, b); o++)
		{
			double e = (a * b) / (double) n;

			int trial = 100000;
			int hit = 0;

			for (int i = 0; i < trial; i++)
			{
				int ov = createRandomOverlap(n, a, b);
				if ((e <= o && ov >= o) || (e > o && ov <= o)) hit++;
			}
			if (e > o) hit = -hit;

			double sim = hit / (double) trial;
			double calc = Overlap.calcPVal(n, a, b, o);
			double dif = sim - calc;
			System.out.println(o + "\t" + sim + "\t" + calc + "\t" + dif);
			Assert.assertTrue(Math.abs(dif) < 0.01 || Math.abs(dif / calc) < 0.01);
		}
	}
	
	
	private static Random rand = new Random();

	private static int createRandomOverlap(int n, int a, int b)
	{
		List<Integer> list = new ArrayList<Integer>(n);
		for (int i = 0; i < n; i++)
		{
			list.add(i);
		}

		int overlap = 0;

		for (int i = 0; i < b; i++)
		{
			Integer x = list.remove(rand.nextInt(list.size()));
			if (x < a) overlap++;
		}

		return overlap;
	}
}
