package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * User: demir Date: Aug 17, 2007 Time: 5:19:13 PM
 */
public interface Named
{
	/**
	 * The contents of this set can be modified but semantic consistency is not
	 * guaranteed. Using {@link #addName} and {@link #removeName} is recommended.
	 *
	 * @return a set of NAME for the name of this entity. This should include the
	 *         values of the NAME and SHORT-NAME property so that it is easy to
	 *         find all known names in one place.
	 */
	public Set<String> getName();

	/**
	 * This method overrides existing set with the new set. If you want to append
	 * to the existing set, use {@link #addName} instead.
	 *
	 * @param NAME a set of names for this entity.
	 */
	public void setName(Set<String> NAME);

	/**
	 * This method adds the given value to the NAME set.
	 *
	 * @param NAME_TEXT a new name to be added
	 */
	public void addName(String NAME_TEXT);

	/**
	 * This method removes the given value from the NAME set.
	 *
	 * @param NAME_TEXT a new name to be added
	 */
	public void removeName(String NAME_TEXT);


	public String getDisplayName();


	/**
	 * An abbreviated name for this entity, preferably a name that is short enough
	 * to be used in a visualization application to label a graphical element that
	 * represents this entity. If no short name is available, an xref may be used
	 * for this purpose by the visualization application.
	 *
	 * @param DISPLAY_NAME
	 */
	public void setDisplayName(String DISPLAY_NAME);

	// Property STANDARD-NAME

	/**
	 * This method returns the standard name for this entity.
	 *
	 * @return standard name for this entity
	 */
	String getStandardName();

	/**
	 * This method sets the standard name for this entity to the given value.
	 *
	 * @param newSTANDARD_NAME The preferred full name for this entity.
	 */
	void setStandardName(String newSTANDARD_NAME);


}
