package org.biopax.paxtools.model.level2;

/**
 * A conversion interaction in which a set of physical entities, at least one
 * being a macromolecule (e.g. protein, RNA, DNA), aggregate via non-covalent
 * interactions. One of the participants of a complexAssembly must be an
 * instance of the class complex (via a physicalEntityParticipant instance).
 * <p/>
 * <b>Comment:</b> This class is also used to represent complex disassembly.
 * The assembly or disassembly of a complex is often a spontaneous process, in
 * which case the direction of the complexAssembly (toward either assembly or
 * disassembly) should be specified via the SPONTANEOUS property.
 * <p/>
 * <b>Synonyms:</b> aggregation, complex formation
 * Examples: Assembly of the TFB2 and TFB3 proteins into the TFIIH complex, and
 * assembly of the ribosome through aggregation of its subunits.
 * Note: The following are not examples of complex assembly: Covalent
 * phosphorylation of a protein (this is a biochemicalReaction); the TFIIH
 * complex itself (this is an instance of the complex class, not the
 * complexAssembly class).
 */
public interface complexAssembly extends conversion
{
}