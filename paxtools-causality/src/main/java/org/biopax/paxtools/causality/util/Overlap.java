package org.biopax.paxtools.causality.util;

import org.biopax.paxtools.causality.model.Change;

import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class Overlap
{
//	private static Map<String, Double> mem = loadMemory();
	/**
	 * Calculates the p-value for getting o or less overlaps by chance.
	 *
	 * @param n
	 * @param a
	 * @param b
	 * @param o
	 * @return
	 */
	public static double calcMutexPVal(int n, int a, int b, int o)
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

//		String s = "" + n + "" + a + "" + b + "" + o;
//		if (mem.containsKey(s)) return mem.get(s);

		double pval = 0;

		if (o < b/2)
		{
			for (int i = 0; i <= o; i++)
			{
				pval += calcProb(n, a, b, i);
			}
		}
		else
		{
			for (int i = o+1; i <= b; i++)
			{
				pval += calcProb(n, a, b, i);
			}
			pval = 1 - pval;
		}
//		mem.put(s, pval);
		return pval;
	}

	/**
	 * Calculates the p-value for getting o or more overlaps by chance.
	 *
	 * @param n
	 * @param a
	 * @param b
	 * @param o
	 * @return
	 */
	public static double calcCoocPVal(int n, int a, int b, int o)
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

		if (o < b/2)
		{
			for (int i = 0; i < o; i++)
			{
				pval += calcProb(n, a, b, i);
			}
			pval = 1 - pval;
		}
		else
		{
			for (int i = o; i <= b; i++)
			{
				pval += calcProb(n, a, b, i);
			}
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
		if ((a + b - n) > x) return 0;

		FactorialSolver s = new FactorialSolver(
			new ArrayList<Integer>(Arrays.asList(a, b, (n-a), (n-b))),
			new ArrayList<Integer>(Arrays.asList(n, x, (a-x), (b-x), (n-a-b+x))));
		
		return s.solve();
	}

	public static void main(String[] args) throws InterruptedException
	{
		System.out.println("mutex = " + calcMutexPVal(300, 290, 50, 49));
		System.out.println("cooc  = " + calcCoocPVal(316, 113, 41, 23));

		for (int i = 0; i < 10; i++)
		{
			System.out.print("\rNUmero " + i);
			Thread.sleep(200);
		}


//		Kronometre k = new Kronometre();
//		k.start();
//		for (int i = 0; i < 100000; i++)
//		{
//			calcMutexPVal(300, 200, 100, 90);
//		}
//		k.stop();
//		k.print();
	}

	public static double calcAlterationMutexPval(Change[] alt1, Change[] alt2)
	{
		return calcAlterationMutexPval(alt1, alt2, null);
	}

	public static double calcAlterationMutexPval(Change[] alt1, Change[] alt2, boolean[] use)
	{
		int[] nabo = getCounts(alt1, alt2, use);
		return calcMutexPVal(nabo[0], nabo[1], nabo[2], nabo[3]);
	}

	public static double calcAlterationCoocPval(Change[] alt1, Change[] alt2)
	{
		return calcAlterationCoocPval(alt1, alt2, null);
	}

	public static double calcAlterationCoocPval(Change[] alt1, Change[] alt2, boolean[] use)
	{
		int[] nabo = getCounts(alt1, alt2, use);
		return calcCoocPVal(nabo[0], nabo[1], nabo[2], nabo[3]);
	}

	private static int[] getCounts(Change[] alt1, Change[] alt2, boolean[] use)
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

		return new int[]{n, cnt1, cnt2, overlap};
	}

//	private static Map<String, Double> loadMemory()
//	{
//		Map<String, Double> mem = new HashMap<String, Double>(1000);
//		try
//		{
//			File file = new File(MEMORY_FILE);
//			if (file.exists())
//			{
//				BufferedReader reader = new BufferedReader(new FileReader(file));
//
//				for (String line = reader.readLine(); line != null; line = reader.readLine())
//				{
//					String[] token = line.split("\t");
//					mem.put(token[0], Double.parseDouble(token[1]));
//				}
//
//				reader.close();
//			}
//		} catch (IOException e){e.printStackTrace();}
//		return mem;
//	}
//
//	public static void writeMemory()
//	{
//		writeMemory(mem);
//	}
//
//	private static void writeMemory(Map<String, Double> mem)
//	{
//		try
//		{
//			File file = new File(MEMORY_FILE);
//			if (file.exists())
//			{
//				Map<String, Double> map = loadMemory();
//				map.putAll(mem);
//				mem = map;
//			}
//
//			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//
//			for (String s : mem.keySet())
//			{
//				writer.write(s + "\t" + mem.get(s) + "\n");
//			}
//
//			writer.close();
//
//		} catch (IOException e){e.printStackTrace();}
//	}
//	public static final String MEMORY_FILE = "memory.txt";
}
