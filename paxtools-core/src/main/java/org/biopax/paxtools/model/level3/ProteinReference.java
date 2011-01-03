package org.biopax.paxtools.model.level3;

/**
 * <b>Description:</b> A protein reference is a grouping of several protein entities that are encoded by the same
 * genetic sequence.  Members can differ in any combination of cellular location, sequence features and bound partners.
 * <p/>
 * <b>Rationale:</b> Protein molecules, encoded by the same genetic sequence can be present in (combinatorially many)
 * different states, as a result of post translational modifications and non-covalent bonds. Each state, chemically, is
 * a different pool of molecules. They are, however, related to each other because:
 * <p/>
 * <ul> <li> They all share the same "base" genetic sequence. </li> <li> They can only be converted to each other but
 * not to any other protein</li> </ul>
 * <p/>
 * <b>Comments:</b>Most Protein databases, including UniProt  would map one to one with ProteinReferences in BioPAX.
 */
public interface ProteinReference extends SequenceEntityReference {
}
