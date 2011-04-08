package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * Definition: A conversion interaction in which one or more entities (substrates) undergo covalent
 * changes to become one or more other entities (products). The substrates of biochemical reactions
 * are defined in terms of sums of species. This is convention in biochemistry, and, in principle,
 * all of the EC reactions should be biochemical reactions. Examples: ATP + H2O = ADP + Pi Comment:
 * In the example reaction above, ATP is considered to be an equilibrium mixture of several species,
 * namely ATP4-, HATP3-, H2ATP2-, MgATP2-, MgHATP-, and Mg2ATP. Additional species may also need to
 * be considered if other ions (e.g. Ca2+) that bind ATP are present. Similar considerations apply
 * to ADP and to inorganic phosphate (Pi). When writing biochemical reactions, it is not necessary
 * to attach charges to the biochemical reactants or to include ions such as H+ and Mg2+ in the
 * equation. The reaction is written in the direction specified by the EC nomenclature system, if
 * applicable, regardless of the physiological direction(s) in which the reaction proceeds.
 * Polymerization reactions involving large polymers whose structure is not explicitly captured
 * should generally be represented as unbalanced reactions in which the monomer is consumed but the
 * polymer remains unchanged, e.g. glycogen + glucose = glycogen.
 */
public interface BiochemicalReaction extends Conversion
{
	/**
	 * Standard transformed Gibbs energy change for a reaction written in terms of biochemical
	 * reactants (sums of species), delta-G'<sup>o</sup>.
	 * <p/>
	 * Since Delta-G can change based on multiple factors including ionic strength and temperature a
	 * reaction can have multiple DeltaG values.
	 * @return a set of DeltaG's for this reaction.
	 */
	Set<DeltaG> getDeltaG();

	/**
	 * Standard transformed Gibbs energy change for a reaction written in terms of biochemical
	 * reactants (sums of species), delta-G'<sup>o</sup>.
	 * <p/>
	 * Since Delta-G can change based on multiple factors including ionic strength and temperature a
	 * reaction can have multiple DeltaG values.
	 * @param deltaG to be added.
	 */
	void addDeltaG(DeltaG deltaG);

	/**
	 * Standard transformed Gibbs energy change for a reaction written in terms of biochemical
	 * reactants (sums of species), delta-G'<sup>o</sup>.
	 * <p/>
	 * Since Delta-G can change based on multiple factors including ionic strength and temperature a
	 * reaction can have multiple DeltaG values.
	 * @param deltaG to be removed.
	 */
	void removeDeltaG(DeltaG deltaG);


	/**
	 * For biochemical reactions this property refers to the standard transformed enthalpy change for a reaction
	 * written in terms of biochemical reactants (sums of species), delta-H'<sup>o</sup>.
	 * <p/>
	 * delta-G'<sup>o</sup> = delta-H'<sup>o</sup> - T delta-S'<sup>o</sup>
	 * <p/>
	 * Units: kJ/mole
	 * <p/>
	 * @return standard transformed enthalpy change
	 */
	Set<Float> getDeltaH();

	/**
	 * For biochemical reactions this property refers to the standard transformed enthalpy change for a reaction
	 * written in terms of biochemical reactants (sums of species), delta-H'<sup>o</sup>.
	 * <p/>
	 * delta-G'<sup>o</sup> = delta-H'<sup>o</sup> - T delta-S'<sup>o</sup>
	 * <p/>
	 * Units: kJ/mole
	 * <p/>
	 * @param delta_h standard transformed enthalpy change
	 */
	void addDeltaH(float delta_h);

	/**
	 * For biochemical reactions this property refers to the standard transformed enthalpy change for a reaction
	 * written in terms of biochemical reactants (sums of species), delta-H'<sup>o</sup>.
	 * <p/>
	 * delta-G'<sup>o</sup> = delta-H'<sup>o</sup> - T delta-S'<sup>o</sup>
	 * <p/>
	 * Units: kJ/mole
	 * <p/>
	 * @param delta_h standard transformed enthalpy change
	 */
	void removeDeltaH(float delta_h);


	/**
	 * For biochemical reactions, this property refers to the standard transformed entropy change for a reaction
	 * written in terms of biochemical reactants (sums of species), delta-S'<sup>o</sup>.
	 * <p/>
	 * delta-G'<sup>o</sup> = delta-H'<sup>o</sup> - T delta-S'<sup>o</sup>
	 * @return standard transformed entropy change
	 */
	Set<Float> getDeltaS();

	/**
	 * For biochemical reactions, this property refers to the standard transformed entropy change for a reaction
	 * written in terms of biochemical reactants (sums of species), delta-S'<sup>o</sup>.
	 * <p/>
	 * delta-G'<sup>o</sup> = delta-H'<sup>o</sup> - T delta-S'<sup>o</sup>
	 * @return standard transformed entropy change
	 */
	void addDeltaS(float delta_s);

	/**
	 * For biochemical reactions, this property refers to the standard transformed entropy change for a reaction
	 * written in terms of biochemical reactants (sums of species), delta-S'<sup>o</sup>.
	 * <p/>
	 * delta-G'<sup>o</sup> = delta-H'<sup>o</sup> - T delta-S'<sup>o</sup>
	 * @return standard transformed entropy change
	 */
	void removeDeltaS(float delta_s);


	/**
	 * The unique number assigned to a reaction by the Enzyme Commission of the International Union of Biochemistry
	 * and Molecular Biology.
	 * @return The unique number assigned to a reaction by the Enzyme Commission
	 */
	 Set<String> getECNumber();

	/**
	 * The unique number assigned to a reaction by the Enzyme Commission of the International Union of Biochemistry
	 * and Molecular Biology.
	 * @param ec_number The unique number assigned to a reaction by the Enzyme Commission
	 */
	void addECNumber(String ec_number);

	/**
	 * The unique number assigned to a reaction by the Enzyme Commission of the International Union of Biochemistry
	 * and Molecular Biology.
	 * @param ec_number The unique number assigned to a reaction by the Enzyme Commission
	 */
	void removeECNumber(String ec_number);


	/**
	 * This quantity is dimensionless and is usually a single number. The measured equilibrium constant for a
	 * biochemical reaction, encoded by the slot KEQ, is actually the apparent equilibrium constant,
	 * K'.  Concentrations in the equilibrium constant equation refer to the total concentrations of  all forms of
	 * particular biochemical reactants. For example, in the equilibrium constant equation for the biochemical
	 * reaction in which ATP is hydrolyzed to ADP and inorganic phosphate:
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
	 * temperature (T), ionic strength (I), pH, and pMg (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>]). Therefore,
	 * these quantities must be specified to be precise, and values for KEQ for biochemical reactions may be
	 * represented as 5-tuples of the form (K' T I pH pMg).  This property may have multiple values,
	 * representing different measurements for K' obtained under the different experimental conditions listed in
	 * the 5-tuple.
	 * @return measured equilibrium constant for a biochemical reaction
	 */
	Set<KPrime> getKEQ();

	/**
	 * This quantity is dimensionless and is usually a single number. The measured equilibrium constant for a
	 * biochemical reaction, encoded by the slot KEQ, is actually the apparent equilibrium constant,
	 * K'.  Concentrations in the equilibrium constant equation refer to the total concentrations of  all forms of
	 * particular biochemical reactants. For example, in the equilibrium constant equation for the biochemical
	 * reaction in which ATP is hydrolyzed to ADP and inorganic phosphate:
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
	 * temperature (T), ionic strength (I), pH, and pMg (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>]). Therefore,
	 * these quantities must be specified to be precise, and values for KEQ for biochemical reactions may be
	 * represented as 5-tuples of the form (K' T I pH pMg).  This property may have multiple values,
	 * representing different measurements for K' obtained under the different experimental conditions listed in
	 * the 5-tuple.
	 * @param keq measured equilibrium constant for a biochemical reaction
	 */
	void addKEQ(KPrime keq);

	/**
	 * This quantity is dimensionless and is usually a single number. The measured equilibrium constant for a
	 * biochemical reaction, encoded by the slot KEQ, is actually the apparent equilibrium constant,
	 * K'.  Concentrations in the equilibrium constant equation refer to the total concentrations of  all forms of
	 * particular biochemical reactants. For example, in the equilibrium constant equation for the biochemical
	 * reaction in which ATP is hydrolyzed to ADP and inorganic phosphate:
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
	 * temperature (T), ionic strength (I), pH, and pMg (pMg = -log<sub>10</sub>[Mg<sup>2+</sup>]). Therefore,
	 * these quantities must be specified to be precise, and values for KEQ for biochemical reactions may be
	 * represented as 5-tuples of the form (K' T I pH pMg).  This property may have multiple values,
	 * representing different measurements for K' obtained under the different experimental conditions listed in
	 * the 5-tuple.
	 * @param keq measured equilibrium constant for a biochemical reaction
	 */
	void removeKEQ(KPrime keq);

}
