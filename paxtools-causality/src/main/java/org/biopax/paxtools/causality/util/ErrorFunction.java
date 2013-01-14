package org.biopax.paxtools.causality.util;

/**
 * @author Ozgun Babur
 */
public class ErrorFunction
{
	private static final double TWO_OVER_SQRT_PI = 2D / Math.sqrt(Math.PI);
	private static final int terms = 60;
	private static double[] divider;
	private double[] vals;
	private static final double limit = 5;
	private static final double step = 0.001;

	private static ErrorFunction instance;

	private ErrorFunction()
	{
		int n = (int) Math.round(limit / step);
		vals = new double[n];

		for (int i = 0; i < n; i++)
		{
			vals[i] = calcError(i * step);
		}
	}

	public static double getSignif(double x)
	{
		return instance.getError(x);
	}

	private double getError(double x)
	{
		if (x < 0) return -getError(-x);

		int i = (int) Math.round(x / step);

		if (i < vals.length) return vals[i];
		return 1;
	}

	private double calcError(double x)
	{
		double x2 = x * x;

		double er = x;

		for (double d : divider)
		{
			x *= x2;
			er += x / d;
		}

		er *= TWO_OVER_SQRT_PI;

		if (er > 1) er = 1 - 1E-10;
		return er;
	}

	private static double[] getDividers(int size)
	{
		double fact = 1;
		double[] div = new double[size];

		for (int i = 1; i <= size; i++)
		{
			fact *= -i;
			div[i-1] = fact * ((2 * i) + 1);
		}
		return div;
	}

	public static void main(String[] args)
	{
		ErrorFunction ef = new ErrorFunction();
		for(double x = 0; x < 10; x += 0.1)
		{
			System.out.println(x + "\t" + ef.getError(x / Math.sqrt(2)));
		}
	}

	static
	{
		divider = getDividers(terms);
		instance = new ErrorFunction();
	}
}