package org.biopax.paxtools.model.level3;

/**
 * Stoichiometric coefficient of a physical entity in the context of a conversion or complex. For
 * each participating element there must be 0 or 1 stoichiometry element. A non-existing
 * stoichiometric element is treated as unknown. This is an n-ary bridge for left, right and
 * component properties.
 */
public interface Stoichiometry extends UtilityClass
{


	/**
	 * @return the physical entity to be annotated with stoichiometry.
	 */
	PhysicalEntity getPhysicalEntity();

	/**
	 * @param physicalEntity entity to be annotated with stoichiometry.
	 */
	void setPhysicalEntity(PhysicalEntity physicalEntity);


	/**
	 * Stoichiometric coefficient of the physicalEntity specified by {@link #getPhysicalEntity()} in
	 * the context of the owner interaction or complex. This value can be any rational number. Generic
	 * values such as "n" or "n+1" should not be used - polymers are currently not covered.
	 *
	 * @return Stoichiometric coefficient for one of the entities in an interaction or complex
	 */
	float getStoichiometricCoefficient();

	/**
	 * Sets the stoichiometric coefficient of the physicalEntity specified by {@link
	 * #getPhysicalEntity()} in the context of the owner interaction or complex. { can be any
	 * rational number. Generic values such as "n" or "n+1" should not be used - polymers are currently
	 * not covered.
	 *
	 * @param newStoichiometricCoefficient of the physical entity
	 */
	void setStoichiometricCoefficient(float newStoichiometricCoefficient);
}
