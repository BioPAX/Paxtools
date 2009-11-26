package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * A conversion interaction in which one or more entities (substrates) undergo
 * ovalent changes to become one or more other entities (products). The substrates
 * of biochemical reactions are defined in terms of sums of species.
 * This is convention in biochemistry, and, in principle, all of the EC
 * reactions should be biochemical reactions.
 * <p/>
 * <b>Examples:</b> ATP + H2O = ADP + Pi
 * <p/>
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

    public Set<deltaGprimeO> getDELTA_G();

    public void setDELTA_G(Set<deltaGprimeO> DELTA_G);

    public void addDELTA_G(deltaGprimeO DELTA_G);

    public void removeDELTA_G(deltaGprimeO DELTA_G);


    public Set<Double> getDELTA_H();

    public void setDELTA_H(Set<Double> DELTA_H);

    public void addDELTA_H(double DELTA_H);

    public void removeDELTA_H(double DELTA_H);


    public Set<Double> getDELTA_S();

    public void setDELTA_S(Set<Double> DELTA_S);

    public void addDELTA_S(double DELTA_S);

    public void removeDELTA_S(double DELTA_S);


    public Set<String> getEC_NUMBER();

    public void setEC_NUMBER(Set<String> EC_NUMBER);

    public void addEC_NUMBER(String EC_NUMBER);

    public void removeEC_NUMBER(String EC_NUMBER);


    public Set<kPrime> getKEQ();

    public void setKEQ(Set<kPrime> KEQ);

    public void addKEQ(kPrime KEQ);

    public void removeKEQ(kPrime KEQ);

}