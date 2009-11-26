package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * A control interaction in which a physical entity (a catalyst) increases the
 * rate of a conversion interaction by lowering its activation energy. Instances
 * of this class describe a pairing between a catalyzing entity and a catalyzed
 * conversion.
 * <p/>
 * <b>Comment:</b>  A separate catalysis instance should be created for each
 * different conversion that a physicalEntity may catalyze and for each
 * different physicalEntity that may catalyze a conversion. For example, a
 * bifunctional enzyme that catalyzes two different biochemical reactions would
 * be linked to each of those biochemical reactions by two separate instances of
 * the catalysis class. Also, catalysis reactions from multiple different
 * organisms could be linked to the same generic biochemical reaction (a
 * biochemical reaction is generic if it only includes small molecules).
 * Generally, the enzyme catalyzing a conversion is known and the use of this
 * class is obvious. In the cases where a catalyzed reaction is known to occur
 * but the enzyme is not known, a catalysis instance should be created without a
 * controller specified (i.e. the CONTROLLER property should remain empty).
 * <p/>
 * <b>Synonyms:</b> facilitation, acceleration.
 * <p/>
 * <b>Examples:</b> The catalysis of a biochemical reaction by an enzyme, the
 * enabling of a transport interaction by a membrane pore complex, and the
 * facilitation of a complex assembly by a scaffold protein.
 * Hexokinase -> (The "Glucose + ATP -> Glucose-6-phosphate +ADP" reaction).
 * A plasma membrane Na+/K+ ATPase is an active transporter (antiport pump)
 * using the energy of ATP to pump Na+ out of the cell and K+ in. Na+ from
 * cytoplasm to extracellular space would be described in a transport instance.
 * K+ from extracellular space to cytoplasm would be described in a transport
 * instance. The ATPase pump would be stored in a catalysis instance
 * controlling each of the above transport instances. A biochemical reaction
 * that does not occur by itself under physiological conditions, but has been
 * observed to occur in the presence of cell extract, likely via one or more
 * unknown enzymes present in the extract, would be stored in the CONTROLLED
 * property, with the CONTROLLER property empty.
 */

public interface catalysis extends control
{

    public void addCOFACTOR(physicalEntityParticipant COFACTOR);

    public void removeCOFACTOR(physicalEntityParticipant COFACTOR);

    public void setCOFACTOR(Set<physicalEntityParticipant> COFACTOR);

    public Set<physicalEntityParticipant> getCOFACTOR();


    public Direction getDIRECTION();

    public void setDIRECTION(Direction DIRECTION);
}
