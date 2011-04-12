package org.biopax.paxtools.model.level3;


/**
 * Standard transformed Gibbs energy change for a reaction written in terms of biochemical reactants
 * (sums of species), delta-G'<sup>o</sup>.
 * <p/>
 * delta-G'<sup>o</sup> = -RT lnK' and delta-G'<sup>o</sup> = delta-H'<sup>o</sup> - T
 * delta-S'<sup>o</sup>
 * <p/>
 * delta-G'<sup>o</sup> has units of kJ/mol.  Like K', it is a function of temperature (T), ionic
 * strength (I), pH, and pMg (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>]). Therefore, these quantities
 * must be specified, and values for DELTA-G for biochemical reactions are represented as 5-tuples
 * of the form (delta-G'<sup>o</sup> T I pH pMg).
 * <p/>
 * This property may have multiple values, representing different measurements for
 * delta-G'<sup>o</sup> obtained under the different experimental conditions listed in the 5-tuple.
 * <p/>
 */
public interface DeltaG extends UtilityClass
{


	/**
	 * For biochemical reactions, this property refers to the standard transformed Gibbs energy change for a
	 * reaction written in terms of biochemical reactants (sums of species), delta-G'<sup>o</sup>.
	 * <p/>
	 * delta-G'<sup>o</sup> = -RT lnK'
	 * and
	 * delta-G'<sup>o</sup> = delta-H'<sup>o</sup> - T delta-S'<sup>o</sup>
	 * <p/>
	 * delta-G'<sup>o</sup> has units of kJ/mol.  Like K', it is a function of temperature (T),
	 * ionic strength (I), pH, and pMg (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>]). Therefore,
	 * these quantities must be specified, and values for DELTA-G for biochemical reactions are represented as
	 * 5-tuples of the form (delta-G'<sup>o</sup> T I pH pMg).
	 * @return delta-G'<sup>o</sup> for this condition
	 */
	float getDeltaGPrime0();

	/**
	 * For biochemical reactions, this property refers to the standard transformed Gibbs energy change for a
	 * reaction written in terms of biochemical reactants (sums of species), delta-G'<sup>o</sup>.
	 * <p/>
	 * delta-G'<sup>o</sup> = -RT lnK'
	 * and
	 * delta-G'<sup>o</sup> = delta-H'<sup>o</sup> - T delta-S'<sup>o</sup>
	 * <p/>
	 * delta-G'<sup>o</sup> has units of kJ/mol.  Like K', it is a function of temperature (T),
	 * ionic strength (I), pH, and pMg (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>]). Therefore,
	 * these quantities must be specified, and values for DELTA-G for biochemical reactions are represented as
	 * 5-tuples of the form (delta-G'<sup>o</sup> T I pH pMg).
	 * @param deltaGPrime0 delta-G'<sup>o</sup> for this condition
	 */
	void setDeltaGPrime0(float deltaGPrime0);

	/**
	 * The ionic strength is defined as half of the total sum of the concentration (ci) of every ionic species (i)
	 * in the solution times the square of its charge (zi). For example, the ionic strength of a 0.1 M solution of
	 * CaCl2 is 0.5 x (0.1 x 22 + 0.2 x 12) = 0.3 M
	 * @return the ionic strength for this condition
	 */
	float getIonicStrength();

	/**
	 * The ionic strength is defined as half of the total sum of the concentration (ci) of every ionic species (i)
	 * in the solution times the square of its charge (zi). For example, the ionic strength of a 0.1 M solution of
	 * CaCl2 is 0.5 x (0.1 x 22 + 0.2 x 12) = 0.3 M
	 * @param ionicStrength the ionic strength for this condition
	 */
	void setIonicStrength(float ionicStrength);

	/**
	 * A measure of acidity and alkalinity of a solution that is a number on a scale on which a value of 7
	 * represents neutrality and lower numbers indicate increasing acidity and higher numbers increasing alkalinity
	 * and on which each unit of change represents a tenfold change in acidity or alkalinity and that is the
	 * negative logarithm of the effective hydrogen-ion concentration or hydrogen-ion activity in gram equivalents
	 * per liter of the solution. (Definition from Merriam-Webster Dictionary)
	 * @return pH for this condition
	 */
	float getPh();

	/**
	 * A measure of acidity and alkalinity of a solution that is a number on a scale on which a value of 7
	 * represents neutrality and lower numbers indicate increasing acidity and higher numbers increasing alkalinity
	 * and on which each unit of change represents a tenfold change in acidity or alkalinity and that is the
	 * negative logarithm of the effective hydrogen-ion concentration or hydrogen-ion activity in gram equivalents
	 * per liter of the solution. (Definition from Merriam-Webster Dictionary)
	 * @param ph for this condition
	 */
	void setPh(float ph);

	/**
	 * A measure of the concentration of magnesium (Mg) in solution. (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>])
	 * @return pMG for this condition
	 */
	float getPMg();

	/**
	 * A measure of the concentration of magnesium (Mg) in solution. (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>])
	 * @param pmg for this condition
	 */
	void setPMg(float pmg);

	/**
	 * Temperature in Celsius
	 * @return Temperature in Celsius for this condition
	 */
	float getTemperature();

	/**
	 * Temperature in Celsius
	 * @return Temperature in Celsius for this condition
	 */
	void setTemperature(float temperature);
}
