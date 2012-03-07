package org.biopax.paxtools.causality.util;

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
		int o = 8;
		
		double e = (a * b) / (double) n;
		
		int trial = 1000000;
		int hit = 0;

		for (int i = 0; i < trial; i++)
		{
			int ov = createRandomOverlap(n, a, b);
			if ((e < o && ov >= o) || (e > o && ov <= o)) hit++;
		}
		if (e > 0) hit = -hit;

		System.out.println("experiment ratio = " + hit / (double) trial);

		System.out.println("calculated pval  = " + Overlap.calcPVal(n, a, b, o));
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
