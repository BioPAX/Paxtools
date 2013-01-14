package org.biopax.paxtools.causality.model;

/**
 * @author Ozgun Babur
 */
public enum Change
{
	/**
	 * The alteration has the potential to increase the activity of
	 */
	ACTIVATING("a", true, false),
	INHIBITING("i", true, false),
	UNKNOWN_CHANGE("u", true, false),
	STAY_INACTIVE("s", false, false),
	NO_CHANGE(".", false, false),
	NO_DATA("-", false, true);

	boolean absent;
	boolean altered;
	String letter;

	private Change(String letter, boolean altered, boolean absent)
	{
		this.absent = absent;
		this.altered = altered;
		this.letter = letter;
	}

	public boolean isAbsent()
	{
		return absent;
	}

	public boolean isAltered()
	{
		return altered;
	}

	public String getLetter()
	{
		return letter;
	}
	
	public boolean isOpposing(Change ch)
	{
		return (this == ACTIVATING && ch == INHIBITING) || (this == INHIBITING && ch == ACTIVATING);
	}
	
	public static Change getChange(String letter)
	{
		for (Change c : values())
		{
			if (c.getLetter().equals(letter)) return c;
		}
		return null;
	}
}
