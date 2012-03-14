package org.biopax.paxtools.causality.util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic histogram implementation.
 *
 * @author Ozgun Babur
 *         Date: Apr 22, 2008
 *         Time: 7:08:18 PM
 */
public class Histogram
{
	private Map<Integer, Integer> binMap;

	private double range;
	private int total;
	private double min;
	private double max;
	private boolean bordered;

	public Histogram(double range)
	{
		this.range = range;
		binMap = new HashMap<Integer, Integer>();
		total = 0;
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
	}

	public Histogram(double range, double[] vals)
	{
		this(range);
		for (double val : vals)
		{
			count(val);
		}
	}

	public void setBordered(boolean bordered)
	{
		this.bordered = bordered;
	}

	public void countAll(double[] ns)
	{
		for (double n : ns)
		{
			count(n);
		}
	}

	public void count(double n)
	{
		if (Double.isNaN(n) || Double.isInfinite(n)) return;

		if (n > max) max = n;
		if (n < min) min = n;

		int i = getBin(n);

		if (binMap.containsKey(i))
		{
			binMap.put(i, binMap.get(i) + 1);
		}
		else
		{
			binMap.put(i, 1);
		}
		total++;
	}

	private int getBin(double n)
	{
		return (int) Math.round(n / range);
	}

	private double getX(int bin)
	{
		return range * bin;
	}

	public int getValue(double n)
	{
		int i = getBin(n);

		if (binMap.containsKey(i))
		{
			return binMap.get(i);
		}
		return 0;
	}

	public double getBinValue(int bin)
	{
		if (binMap.containsKey(bin))
		{
			return binMap.get(bin);
		}
		return 0;
	}

	public double calc(double x)
	{
		return getValue(x);
	}

	public int getTotal()
	{
		return total;
	}

	public double getRange()
	{
		return range;
	}

	public void add(Histogram h)
	{
		assert range == h.range : "Ranges not equal, r1 = " + range + " r2 = " + h.range;
		for (Integer i : h.binMap.keySet())
		{
			if (!binMap.containsKey(i))
			{
				binMap.put(i, 0);
			}

			binMap.put(i, binMap.get(i) + h.binMap.get(i));
		}
	}

	public int getPositiveTotal()
	{
		int t = 0;
		for (Integer i : binMap.keySet())
		{
			if (i >= 0) t += binMap.get(i);
		}
		return t;
	}

	public int getNegativeTotal()
	{
		int t = 0;
		for (Integer i : binMap.keySet())
		{
			if (i < 0) t += binMap.get(i);
		}
		return t;
	}

	public double getDensity(double x)
	{
		int a = getValue(x);

		return a / (total * range);
	}

	public double[] getMinMax()
	{
		if (total == 0) return new double[]{Double.NaN, Double.NaN};

		int[] bins = getMinMaxBin();

		return new double[]{getX(bins[0]), getX(bins[1])};
	}

	public int[] getMinMaxBin()
	{
		int mini = Integer.MAX_VALUE;
		int maxi = -Integer.MAX_VALUE;

		for (Integer i : binMap.keySet())
		{
			if (i < mini) mini = i;
			if (i > maxi) maxi = i;
		}

		return new int[]{mini, maxi};
	}

	public double getPeakLocation()
	{
		int max = -Integer.MAX_VALUE;
		int maxLoc = 0;
		for (Integer i : binMap.keySet())
		{
			if (binMap.containsKey(i))
			{
				Integer cnt = binMap.get(i);
				if (cnt > max)
				{
					max = cnt;
					maxLoc = i;
				}
			}
		}
		return maxLoc * range;
	}

	public void print()
	{
		int t = 0;
		for (Integer c : binMap.values())
		{
			t += c;
		}
		assert t == total : "total = " + total + " found = " + t;

		if (binMap.isEmpty()) return;

		Integer[] bins = binMap.keySet().toArray(new Integer[binMap.size()]);
		Arrays.sort(bins);

		int p = bins[0];

		for (Integer bin : bins)
		{
			for (int i = p + 1; i < bin; i++)
			{
				System.out.println(accurate(i * range) + "\t0");
			}
			System.out.println(accurate(bin * range) + "\t" + binMap.get(bin));
			p = bin;
		}
	}

	public static final int Z = 1000000;
	private double accurate(double v)
	{
		return Math.round(v * Z) / (double) Z;
	}

	public void print(double times)
	{
		if (binMap.isEmpty()) return;

		Integer[] bins = binMap.keySet().toArray(new Integer[binMap.size()]);
		Arrays.sort(bins);

		int p = bins[0];

		for (Integer bin : bins)
		{
			for (int i = p + 1; i < bin; i++)
			{
				System.out.println((i * range) + "\t0");
			}
			System.out.println((bin * range) + "\t" + (binMap.get(bin) * times));
			p = bin;
		}
	}

	public void printTogether(Histogram h)
	{
		if (binMap.isEmpty())
		{
			h.print();
			return;
		}

		Integer[] bins = binMap.keySet().toArray(new Integer[binMap.size()]);
		Arrays.sort(bins);
		Integer[] hbins = h.binMap.keySet().toArray(new Integer[h.binMap.size()]);
		Arrays.sort(hbins);

		int min = Math.min(bins[0], hbins[0]);
		int max = Math.max(bins[bins.length - 1], hbins[hbins.length - 1]);

		for (int i = min; i <= max; i++)
		{
			System.out.print((i * range) + "\t");

			if (binMap.containsKey(i))
			{
				System.out.print(binMap.get(i) + "\t");
			}
			else
			{
				System.out.print("0\t");
			}

			if (h.binMap.containsKey(i))
			{
				System.out.print(h.binMap.get(i) + "\n");
			}
			else
			{
				System.out.print("0\n");
			}
		}
	}

	public void printTogether(Histogram h, double times)
	{
		if (binMap.isEmpty())
		{
			h.print();
			return;
		}

		Integer[] bins = binMap.keySet().toArray(new Integer[binMap.size()]);
		Arrays.sort(bins);
		Integer[] hbins = h.binMap.keySet().toArray(new Integer[h.binMap.size()]);
		Arrays.sort(hbins);

		int min = Math.min(bins[0], hbins[0]);
		int max = Math.min(bins[bins.length - 1], hbins[hbins.length - 1]);

		for (int i = min; i <= max; i++)
		{
			System.out.print((i * range) + "\t");

			if (binMap.containsKey(i))
			{
				System.out.print((binMap.get(i) * times) + "\t");
			}
			else
			{
				System.out.print("0\t");
			}

			if (h.binMap.containsKey(i))
			{
				System.out.print((h.binMap.get(i) * times) + "\n");
			}
			else
			{
				System.out.print("0\n");
			}
		}
	}

	public static void printAll(Histogram... his)
	{
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;

		for (Histogram h : his)
		{
			for (Integer v : h.binMap.keySet())
			{
				if (v < min) min = v;
				if (v > max) max = v;
			}
		}

		double range = his[0].getRange();

		for (int i = min; i <= max; i++)
		{
			System.out.print((i * range));

			for (Histogram h : his)
			{
				System.out.print("\t");
				if (h.binMap.containsKey(i))
				{
					System.out.print(h.binMap.get(i));
				}
			}
			System.out.println();
		}
	}

	public void printDensity()
	{
		System.out.println("total count = " + total);

		double[] minmax = getMinMax();

		String sep = "\t";

		if (bordered)
		{
			System.out.println(minmax[0] + sep + getDensity(minmax[0]) * range / (minmax[0] + (range / 2) - min));

			for (double x = minmax[0] + range; x <= minmax[1] - (range / 2); x += range)
			{
				System.out.println(x + sep + getDensity(x));
			}

			System.out.println(minmax[1] + sep + getDensity(minmax[1]) * range / (max - minmax[1] + (range / 2)));
		}
		else
		{
			for (double x = minmax[0]; x < minmax[1] + (range / 2); x += range)
			{
				System.out.println(x + sep + getDensity(x));
			}
		}
	}

	public static void write(Histogram h, String filename)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

			writer.write(h.getRange() + "\n");
			writer.write(h.getTotal() + "\n");

			for (Integer key : h.binMap.keySet())
			{
				writer.write(key + "\t" + h.binMap.get(key) + "\n");
			}

			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static Histogram read(String filename)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			String line = reader.readLine();

			Histogram h = new Histogram(Double.parseDouble(line));
			h.total = Integer.parseInt(reader.readLine());

			for (line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (line.length() == 0) continue;

				String[] s = line.split("\\t");
				assert s.length == 2;
				h.binMap.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
			}
			reader.close();
			return h;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
