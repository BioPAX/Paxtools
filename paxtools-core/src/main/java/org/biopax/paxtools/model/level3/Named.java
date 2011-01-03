package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * Interface for all classes that can have names in BioPAX. All named BioPAX elements can have
 * multiple names, and exactly one standardName and exactly one shortName. standardName and
 * shortName are OWL subproperties of name in BioPAX, so these are automatically considered as a
 * name if defined. Paxtools refle
 * <p/>
 * Warning: There is a potential OWL-JAVA semantic mismatch when manipulating names. If a user
 * decides to assign a different name to standardName or shortName, what happens to the old name is
 * not well defined - they can be "demoted" to a regular name or removed from the names list
 * altogether. Paxtools currently does the latter. If this is not the desired behaviour, the old
 * name should be added manually back to the names list.
 */
public interface Named extends XReferrable
{
	/**
	 * Names for this entity, including standardName and shortName if defined.
	 * <p/>
	 * The contents of this set can be modified directly but semantic consistency is not guaranteed.
	 * Using {@link #addName} and {@link #removeName} is recommended.
	 *
	 * @return Names for this entity, including standardName and shortName if defined.
	 */
	public Set<String> getName();


	/**
	 * This method adds the given value to the name set.
	 *
	 * @param name a new name to be added
	 */
	public void addName(String name);

	/**
	 * This method removes the given value from the name set.
	 *
	 * @param name a new name to be removed
	 */
	public void removeName(String name);


	/**
	 * An abbreviated name for this entity, preferably a name that is short enough to be used in a
	 * visualization application to label a graphical element that represents this entity. If no short
	 * name is available, an xref may be used for this purpose by the visualization application.
	 *
	 * @return An abbreviated name for this entity
	 */
	public String getDisplayName();


	/**
	 * An abbreviated name for this entity, preferably a name that is short enough to be used in a
	 * visualization application to label a graphical element that represents this entity. If no short
	 * name is available, an xref may be used for this purpose by the visualization application.
	 *
	 * @param displayName
	 */
	public void setDisplayName(String displayName);



	/**
	 * The preferred full name for this entity, if exists assigned by a standard nomenclature
	 * organization such as HUGO Gene Nomenclature Committee.
	 *
	 * @return standard name for this entity
	 */
	String getStandardName();


	/**
	 * The preferred full name for this entity, if exists assigned by a standard nomenclature
	 * organization such as HUGO Gene Nomenclature Committee.
	 *
	 * @param standardName standard name for this entity
	 */
	public void setStandardName(String standardName);

}
