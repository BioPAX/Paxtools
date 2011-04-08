package org.biopax.paxtools.model.level3;

/**
 * This enum represents the direction of a catalysis under all physiological conditions if there is one.
 * Note that chemically a catalyst will increase the rate of the reaction in both directions. In biology,
 * however, there are cases where the enzyme is expressed only when the controlled bidirectional conversion is
 * on one side of the chemical equilibrium. olled bidirectional conversion is on one side of the chemical
 * equilibrium. For example E.Coli's lac operon ensures that lacZ gene is only synthesized when there is enough
 * lactose in the medium. If that is the case and the controller, under biological conditions,
 * is always catalyzing the conversion in one direction then this fact can be captured using this property. If
 * the enzyme is active for both directions, or the conversion is not bidirectional,
 * this property should be left empty.
 */
public enum CatalysisDirectionType
{
	LEFT_TO_RIGHT,
	RIGHT_TO_LEFT
}