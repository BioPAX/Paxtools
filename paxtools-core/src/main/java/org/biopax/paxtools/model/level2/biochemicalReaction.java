package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * A conversion interaction in which one or more entities (substrates) undergo
 * ovalent changes to become one or more other entities (products). The substrates
 * of biochemical reactions are defined in terms of sums of species.
 * This is convention in biochemistry, and, in principle, all of the EC
 * reactions should be biochemical reactions.
 *
 * <b>Examples:</b> ATP + H2O = ADP + Pi
 *
 * <b>Comment:</b> In the example reaction above, ATP is considered to be an
 * equilibrium mixture of several species, namely ATP4-, HATP3-, H2ATP2-,
 * MgATP2-, MgHATP-, and Mg2ATP. Additional species may also need to be
 * considered if other ions (e.g. Ca2+) that bind ATP are present.
 * Similar considerations apply to ADP and to inorganic phosphate (Pi).
 * When writing biochemical reactions, it is not necessary to attach charges to
 * the biochemical reactants or to include ions such as H+ and Mg2+ in the
 * equation. The reaction is written in the direction specified by the EC
 * nomenclature system, if applicable, regardless of the physiological
 * direction(s) in which the reaction proceeds. Polymerization reactions
 * involving large polymers whose structure is not explicitly captured should
 * generally be represented as unbalanced reactions in which the monomer is
 * consumed but the polymer remains unchanged, e.g. glycogen + glucose =
 * glycogen.
 */
public interface biochemicalReaction extends conversion
{
// -------------------------- OTHER METHODS --------------------------

    Set<deltaGprimeO> getDELTA_G();

    void setDELTA_G(Set<deltaGprimeO> DELTA_G);

    void addDELTA_G(deltaGprimeO DELTA_G);

    void removeDELTA_G(deltaGprimeO DELTA_G);


    Set<Double> getDELTA_H();

    void setDELTA_H(Set<Double> DELTA_H);

    void addDELTA_H(double DELTA_H);

    void removeDELTA_H(double DELTA_H);


    Set<Double> getDELTA_S();

    void setDELTA_S(Set<Double> DELTA_S);

    void addDELTA_S(double DELTA_S);

    void removeDELTA_S(double DELTA_S);


    Set<String> getEC_NUMBER();

    void setEC_NUMBER(Set<String> EC_NUMBER);

    void addEC_NUMBER(String EC_NUMBER);

    void removeEC_NUMBER(String EC_NUMBER);


    Set<kPrime> getKEQ();

    void setKEQ(Set<kPrime> KEQ);

    void addKEQ(kPrime KEQ);

    void removeKEQ(kPrime KEQ);

}