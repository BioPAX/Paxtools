package org.biopax.paxtools.model.level3;

/**
 *
 */
public interface ChemicalConstant extends UtilityClass
{
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

}
