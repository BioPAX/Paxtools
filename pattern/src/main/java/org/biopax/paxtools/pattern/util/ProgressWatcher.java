package org.biopax.paxtools.pattern.util;

/**
 * This interface is to use for tracking progress of a process.
 * @author Ozgun Babur
 */
public interface ProgressWatcher
{
	/**
	 * Sets how many ticks are there in total.
	 * @param total total number of ticks
	 */
	public void setTotalTicks(int total);

	/**
	 * Ticks the progress watcher.
	 * @param times times to tick
	 */
	public void tick(int times);
}
