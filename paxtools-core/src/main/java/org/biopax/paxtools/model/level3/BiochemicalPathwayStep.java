package org.biopax.paxtools.model.level3;

/**
 * Definition: Imposes ordering on a step in a biochemical pathway.
 * <p/>
 * Retionale: A biochemical reaction can be reversible by itself, but can be physiologically directed in the context
 * of a pathway, for instance due to flux of reactants and products.
 * <p/>
 * Usage: Only one conversion interaction can be ordered at a time, but multiple catalysis or modulation instances
 * can be part of one step.
 */
public interface BiochemicalPathwayStep extends PathwayStep
{


	/**
	 * This method returns the central conversion of this BiochemicalPathwayStep. The
	 * returned conversion is also stepProcess of this PathwayStep. The step direction defines the direction
	 * of this conversion. The conversion must be reversible.
	 * @return The central conversion that take place at this step of the biochemical pathway.
	 */
	@Key Conversion getStepConversion();

	/**
	 * This method sets the conversion of this BiochemicalPathwayStep to the
	 * new Step_Conversion. Old step conversion is also removed from the
	 * stepProcess list.
	 */
	void setStepConversion(Conversion stepConversion);

	/**
	 * This property can be used for annotating direction of enzymatic activity. Even if an enzyme catalyzes a
	 * reaction reversibly, the flow of matter through the pathway will force the equilibrium in a given direction
	 * for that particular pathway.
	 * @return Direction of the conversion in this particular pathway context.
	 */
	@Key StepDirection getStepDirection();

	/**
	 * This property can be used for annotating direction of enzymatic activity. Even if an enzyme catalyzes a
	 * reaction reversibly, the flow of matter through the pathway will force the equilibrium in a given direction
	 * for that particular pathway.
	 * @param stepDirection Direction of the conversion in this particular pathway context.
	 */
	void setStepDirection(StepDirection stepDirection);
}
