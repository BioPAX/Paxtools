package org.biopax.paxtools.causality.util;

/**
 * @author Ozgun Babur
 */
public class Kronometre
{
	private long startedAt;
	private long pausedAt;
	private long totalPaused;
	private long totalCounted;
	private boolean paused;

	public Kronometre()
	{
		start();
	}

	public void reset()
	{
		start();
	}
	
	public void start()
	{
		startedAt = System.currentTimeMillis();
		totalPaused = 0;
		totalCounted = 0;
		pausedAt = 0;
		paused = false;
	}

	public void pause()
	{
		if (!paused)
		{
			pausedAt = System.currentTimeMillis();
			paused = true;
		}
	}

	public void resume()
	{
		if (paused)
		{
			totalPaused += System.currentTimeMillis() - pausedAt;
			paused = false;
		}
	}

	public void stop()
	{
		if (paused)
		{
			resume();
		}

		long totalTime = System.currentTimeMillis() - startedAt;
		totalCounted = totalTime - totalPaused;
	}

	public long getPassedMilisec()
	{
		return (paused ? pausedAt : System.currentTimeMillis()) - startedAt - totalPaused;
	}

	public void print()
	{
		System.out.println("Time counted: " + getPrintable(totalCounted));
		if (totalPaused > 0)
		{
			System.out.println("Time paused: " + getPrintable(totalPaused));
			System.out.println("Time total: " + getPrintable(totalCounted + totalPaused));
		}
	}

	public static String getPrintable(long time)
	{
		int div = 1000 * 60 * 60;
		int hours = (int) (time / div);
		time %= div;
		div /= 60;
		int minutes = (int) (time / div);
		time %= div;
		div /= 60;
		int seconds = (int) (time / div);

		String s = hours > 0 ? hours + "h, " : "";
		s += minutes > 0 ? minutes + "m, " : "";
		s += seconds + "s";
		return s;
	}
}
