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


	float getDeltaGPrime0();

	void setDeltaGPrime0(float deltaGPrime0);

	float getIonicStrength();

	void setIonicStrength(float ionicStrength);

	float getPh();

	void setPh(float ph);

	float getPMg();

	void setPMg(float pmg);

	float getTemperature();

	void setTemperature(float temperature);
}
