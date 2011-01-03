package org.biopax.paxtools.model.level3;


/**
 * <b>Definition:</b> A conversion interaction in which a set of physical entities, at least one being a macromolecule (e.g.
 * protein, RNA, DNA), aggregate to from a complex physicalEntity. One of the participants of a complexAssembly must be
 * an instance of the class Complex. The modification of the physicalentities involved in the ComplexAssembly is
 * captured via BindingFeature class.
 * <p/>
 * <b>Usage:</b> This class is also used to represent complex disassembly. The assembly or disassembly of a complex is often a
 * spontaneous process, in which case the direction of the complexAssembly (toward either assembly or disassembly)
 * should be specified via the SPONTANEOUS property. Conversions in which participants obtain or lose
 * CovalentBindingFeatures ( e.g. glycolysation of proteins) should be modeled with BiochemicalReaction.
 * <p/>
 * <b>Synonyms:</b> aggregation, complex formation
 * <p/>
 * <b>Examples:</b> Assembly of the TFB2 and TFB3 proteins into the TFIIH complex, and assembly of the ribosome through
 * aggregation of its subunits.
 */
public interface ComplexAssembly extends Conversion
{
}
