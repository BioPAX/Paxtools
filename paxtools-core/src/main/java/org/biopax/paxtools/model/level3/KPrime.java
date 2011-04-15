package org.biopax.paxtools.model.level3;


/**
 * The apparent equilibrium constant, K', and associated values.  Concentrations in the equilibrium constant equation
 * refer to the total concentrations of all forms of particular biochemical reactants. For example,
 * in the equilibrium constant equation for the biochemical reaction in which ATP is hydrolyzed to ADP and inorganic
 * phosphate:
 * <p/>
 * K' = [ADP][P<sub>i</sub>]/[ATP],
 * <p/>
 * The concentration of ATP refers to the total concentration of all of the following species:
 * <p/>
 * [ATP] = [ATP<sup>4-</sup>] + [HATP<sup>3-</sup>] + [H<sub>2</sub>ATP<sup>2-</sup>] + [MgATP<sup>2-</sup>] +
 * [MgHATP<sup>-</sup>] + [Mg<sub>2</sub>ATP].
 * <p/>
 * The apparent equilibrium constant is formally dimensionless, and can be kept so by inclusion of as many of the
 * terms (1 mol/dm3) in the numerator or denominator as necessary.  It is a function of temperature (T),
 * ionic strength (I), pH, and pMg (pMg = -log10[Mg2+]). Therefore, these quantities must be specified to be precise,
 * and values for KEQ for biochemical reactions may be represented as 5-tuples of the form (K' T I pH pMg).  This
 * property may have multiple values, representing different measurements for K' obtained under the different
 * experimental conditions listed in the 5-tuple.
 */
public interface KPrime extends UtilityClass
{

	/**
	 * The ionic strength is defined as half of the total sum of the concentration (ci) of every ionic species (i)
	 * in the solution times the square of its charge (zi). For example, the ionic strength of a 0.1 M solution of
	 * CaCl2 is 0.5 x (0.1 x 22 + 0.2 x 12) = 0.3 M
	 * @return The ionic strength
	 */
	float getIonicStrength();

	/**
	 * The ionic strength is defined as half of the total sum of the concentration (ci) of every ionic species (i)
	 * in the solution times the square of its charge (zi). For example, the ionic strength of a 0.1 M solution of
	 * CaCl2 is 0.5 x (0.1 x 22 + 0.2 x 12) = 0.3 M
	 * @param ionicStrength The ionic strength
	 */
	void setIonicStrength(float ionicStrength);


	/**
	 * The apparent equilibrium constant K'. Concentrations in the equilibrium constant equation refer to the total
	 * concentrations of  all forms of particular biochemical reactants. For example,
	 * in the equilibrium constant equation for the biochemical reaction in which ATP is hydrolyzed to ADP and
	 * inorganic phosphate:
	 * <p/>
	 * K' = [ADP][P<sub>i</sub>]/[ATP],
	 * <p/>
	 * The concentration of ATP refers to the total concentration of all of the following species:
	 * <p/>
	 * [ATP] = [ATP<sup>4-</sup>] + [HATP<sup>3-</sup>] + [H<sub>2</sub>ATP<sup>2-</sup>] + [MgATP<sup>2-</sup>] +
	 * [MgHATP<sup>-</sup>] + [Mg<sub>2</sub>ATP].
	 * <p/>
	 * The apparent equilibrium constant is formally dimensionless, and can be kept so by inclusion of as many of
	 * the terms (1 mol/dm<sup>3</sup>) in the numerator or denominator as necessary.  It is a function of
	 * temperature (T), ionic strength (I), pH, and pMg (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>]).
	 * @return The apparent equilibrium constant K'
	 */
	float getKPrime();

	/**
	 * The apparent equilibrium constant K'. Concentrations in the equilibrium constant equation refer to the total
	 * concentrations of  all forms of particular biochemical reactants. For example,
	 * in the equilibrium constant equation for the biochemical reaction in which ATP is hydrolyzed to ADP and
	 * inorganic phosphate:
	 * <p/>
	 * K' = [ADP][P<sub>i</sub>]/[ATP],
	 * <p/>
	 * The concentration of ATP refers to the total concentration of all of the following species:
	 * <p/>
	 * [ATP] = [ATP<sup>4-</sup>] + [HATP<sup>3-</sup>] + [H<sub>2</sub>ATP<sup>2-</sup>] + [MgATP<sup>2-</sup>] +
	 * [MgHATP<sup>-</sup>] + [Mg<sub>2</sub>ATP].
	 * <p/>
	 * The apparent equilibrium constant is formally dimensionless, and can be kept so by inclusion of as many of
	 * the terms (1 mol/dm<sup>3</sup>) in the numerator or denominator as necessary.  It is a function of
	 * temperature (T), ionic strength (I), pH, and pMg (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>]).
	 * (Definition from EcoCyc)
	 * @param kPrime The apparent equilibrium constant K'
	 */
	void setKPrime(float kPrime);

	/**
	 * A measure of acidity and alkalinity of a solution that is a number on a scale on which a value of 7
	 * represents neutrality and lower numbers indicate increasing acidity and higher numbers increasing alkalinity
	 * and on which each unit of change represents a tenfold change in acidity or alkalinity and that is the
	 * negative logarithm of the effective hydrogen-ion concentration or hydrogen-ion activity in gram equivalents
	 * per liter of the solution.
	 * @return A measure of acidity and alkalinity of a solution
	 */
	float getPh();

	/**
	 * A measure of acidity and alkalinity of a solution that is a number on a scale on which a value of 7
	 * represents neutrality and lower numbers indicate increasing acidity and higher numbers increasing alkalinity
	 * and on which each unit of change represents a tenfold change in acidity or alkalinity and that is the
	 * negative logarithm of the effective hydrogen-ion concentration or hydrogen-ion activity in gram equivalents
	 * per liter of the solution.
	 * @param ph A measure of acidity and alkalinity of a solution
	 */
	void setPh(float ph);

	/**
	 * @return A measure of the concentration of magnesium (Mg) in solution. (pMg =
	 *         -log<sub>10</sub>[Mg<sup>2+</sup>])
	 */
	float getPMg();

	/**
	 * @param pMg A measure of the concentration of magnesium (Mg) in solution. (pMg =
	 * -log<sub>10</sub>[Mg<sup>2+</sup>])
	 */
	void setPMg(float pMg);


	/**
	 * @return Temperature in Celsius
	 */
	float getTemperature();

	/**
	 * @param temperature Temperature in Celsius
	 */
	void setTemperature(float temperature);
}
