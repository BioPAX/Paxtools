package org.biopax.paxtools.causality.util;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class Summary
{
	public static double mean(double[] x, double[] weight)
	{
		assert x.length == weight.length;

		double totalW = sum(weight);
		double avg = 0;
		for (int i = 0; i < x.length; i++)
		{
			avg += x[i] * weight[i];
		}
		avg /= totalW;
		return avg;
	}

	public static double mean(double[] x)
	{
		if (x.length == 0) return Double.NaN;

		double total = 0;

		for (double v : x)
		{
			total += v;
		}
		return total / x.length;
	}

	public static double absoluteMean(double[] x)
	{
		if (x.length == 0) return Double.NaN;

		double total = 0;

		for (double v : x)
		{
			total += Math.abs(v);
		}
		return total / x.length;
	}

	public static double meanOrderWeighted(double[] x)
	{
		if (x.length == 0) return Double.NaN;

		double total = 0;

		for (int i = 0; i < x.length; i++)
		{
			total += x[i] * (x.length - i);
		}
		return total / ((x.length * (x.length + 1)) / 2);
	}

	public static double mean(double[] x, int[] inds)
	{
		if (x.length == 0 || inds.length == 0) return Double.NaN;
		assert x.length >= inds.length;

		double total = 0;

		for (int ind : inds)
		{
			total += x[ind];
		}

		return total / inds.length;
	}

	public static double max(double[] x)
	{
		if (x.length == 0) return Double.NaN;

		double max = -Double.MAX_VALUE;

		for (double v : x)
		{
			if (max < v) max = v;
		}
		return max;
	}

	public static int max(int[] x)
	{
		if (x.length == 0) return Integer.MIN_VALUE;

		int max = Integer.MIN_VALUE;

		for (int v : x)
		{
			if (max < v) max = v;
		}
		return max;
	}

	public static double min(double[] x)
	{
		if (x.length == 0) return Double.NaN;

		double min = Double.MAX_VALUE;

		for (double v : x)
		{
			if (min > v) min = v;
		}
		return min;
	}

	public static int min(int[] x)
	{
		if (x.length == 0) return Integer.MAX_VALUE;

		int min = Integer.MAX_VALUE;

		for (int v : x)
		{
			if (min > v) min = v;
		}
		return min;
	}

	public static double median(double[] x)
	{
		if (x.length == 0) return Double.NaN;

		double[] v = new double[x.length];
		System.arraycopy(x, 0, v, 0, x.length);
		Arrays.sort(v);
		int i = v.length / 2;

		if (v.length % 2 == 1) return v[i];
		else return (v[i] + v[i + 1]) / 2;
	}

	public static double stdev(double[] x)
	{
		return Math.sqrt(variance(x));
	}

	public static double stdev(double[] x, int[] ind)
	{
		return Math.sqrt(variance(x, ind));
	}

	public static double variance(double[] x)
	{
		double mean = Summary.mean(x);
		double var = 0;

		for (double v : x)
		{
			double term = v - mean;
			var += term * term;
		}

		var /= x.length;
		return var;
	}

	public static double variance(double[] x, int[] ind)
	{
		double mean = Summary.mean(x, ind);
		double var = 0;

		for (int i : ind)
		{
			double term = x[i] - mean;
			var += term * term;
		}

		var /= ind.length;
		return var;
	}

	public static double varLog(double[] x)
	{
		double[] loged = log(x);
		return variance(loged);
	}

	public static double[] log(double[] x)
	{
		double[] v = new double[x.length];
		for (int i = 0; i < v.length; i++)
		{
			v[i] = Math.log(x[i]);
		}
		return v;
	}

	public static int sum(int[] x)
	{
		int sum = 0;
		for (int i : x)
		{
			sum += i;
		}
		return sum;
	}

	public static double sum(double[] x)
	{
		double sum = 0;
		for (double i : x)
		{
			sum += i;
		}
		return sum;
	}

	public static int[] sum(List<int[]> singles)
	{
		int[] s = new int[singles.get(0).length];

		for (int[] cnt : singles)
		{
			for (int i = 0; i < s.length; i++)
			{
				s[i] += cnt[i];
			}
		}
		return s;
	}

	public static double calcPval(double dif, double stdev, double n)
	{
		if (dif < 0) dif = -dif;
		double z = dif / (stdev / Math.sqrt(n));
		return calcPvalForZ(z);
	}

	static double calcPvalForZ(double z)
	{
		if (z > 5) return 0;

		double p2 = (((((.000005383*z+.0000488906)*z+.0000380036)*z+
			.0032776263)*z+.0211410061)*z+.049867347)*z+1;

		p2 = Math.pow(p2, -16);

		return p2;
	}
}