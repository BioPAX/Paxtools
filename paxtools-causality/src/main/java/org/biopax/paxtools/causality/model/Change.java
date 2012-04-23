package org.biopax.paxtools.causality.model;

/**
 * @author Ozgun Babur
 */
public enum Change
{
	/**
	 * The alteration has the potential to increase the activity of
	 */
	ACTIVATING(true, false),
	INHIBITING(true, false),
	STAY_INACTIVE(false, false),
	NO_CHANGE(false, false),
	NO_DATA(false, true);

	boolean absent;
	boolean altered;

	private Change(boolean altered, boolean absent)
	{
		this.absent = absent;
		this.altered = altered;
	}

	public boolean isAbsent()
	{
		return absent;
	}

	public boolean isAltered()
	{
		return altered;
	}
}
