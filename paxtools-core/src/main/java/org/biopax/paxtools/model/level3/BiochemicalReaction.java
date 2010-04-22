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
	 *
	 *
	 * @return a set of DeltaG's for this reaction.
	 */
	Set<DeltaG> getDeltaG();

	/**
	 * Standard transformed Gibbs energy change for a reaction written in terms of biochemical
	 * reactants (sums of species), delta-G'<sup>o</sup>.
	 * <p/>
	 * Since Delta-G can change based on multiple factors including ionic strength and temperature a
	 * reaction can have multiple DeltaG values.
	 *
	 *
	 * @param deltaG to be added.
	 */
	void addDeltaG(DeltaG deltaG);

	/**
	 * Standard transformed Gibbs energy change for a reaction written in terms of biochemical
	 * reactants (sums of species), delta-G'<sup>o</sup>.
	 * <p/>
	 * Since Delta-G can change based on multiple factors including ionic strength and temperature a
	 * reaction can have multiple DeltaG values.
	 *
	 *
	 * @param deltaG to be removed.
	 */
	void removeDeltaG(DeltaG deltaG);



	Set<Float> getDeltaH();

	void addDeltaH(float delta_h);

	void removeDeltaH(float delta_h);



	// Property DELTA-S

	Set<Float> getDeltaS();

	void addDeltaS(float delta_s);

	void removeDeltaS(float delta_s);



	// Property EC-NUMBER

	Set<String> getECNumber();

	void addECNumber(String ec_number);

	void removeECNumber(String ec_number);



	// Property KEQ

	Set<KPrime> getKEQ();

	void addKEQ(KPrime keq);

	void removeKEQ(KPrime keq);


}
