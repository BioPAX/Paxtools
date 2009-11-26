package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * A physical entity whose structure is comprised of other physical entities
 * bound to each other non-covalently, at least one of which is a macromolecule
 * (e.g. protein, DNA, or RNA). Complexes must be stable enough to function as
 * a biological unit; in general, the temporary association of an enzyme with
 * its substrate(s) should not be considered or represented as a complex. A
 * complex is the physical product of an interaction (complexAssembly) and is
 * not itself considered an interaction.
 * <b>Comment:</b> In general, complexes should not be defined recursively so
 * that smaller complexes exist within larger complexes, i.e. a complex should
 * not be a COMPONENT of another complex (see comments on the COMPONENT
 * property). The boundaries on the size of complexes described by this class
 * are not defined here, although elements of the cell as large and dynamic as,
 * e.g., a mitochondrion would typically not be described using this class
 * (later versions of this ontology may include a cellularComponent class to
 * represent these). The strength of binding and the topology of the components
 * cannot be described currently, but may be included in future versions of the
 * ontology, depending on community need.
 * <p/>
 * <b>Examples:</b> Ribosome, RNA polymerase II. Other examples of this class
 * include complexes of multiple protein monomers and complexes of proteins and
 *  small molecules.
 */
public interface complex extends physicalEntity
{


    public Set<physicalEntityParticipant> getCOMPONENTS();

    public void setCOMPONENTS(Set<physicalEntityParticipant> COMPONENTS);

    public void addCOMPONENTS(physicalEntityParticipant COMPONENTS);

    public void removeCOMPONENTS(physicalEntityParticipant COMPONENTS);


    public bioSource getORGANISM();

    public void setORGANISM(bioSource ORGANISM);
}