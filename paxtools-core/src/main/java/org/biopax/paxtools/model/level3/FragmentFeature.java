package org.biopax.paxtools.model.level3;

/**
 * Definition: An entity feature that represents the resulting physical entity subsequent to a cleavage or
 * degradation event.
 * <p/>
 * Usage: Fragment Feature can be used to cover multiple types of modfications to the sequence of the physical
 * entity:
 * <ul>
 * <li>    A protein with a single cleavage site that converts the protein into two fragments (e.g. pro-insulin
 * converted to insulin and C-peptide). TODO: CV term for sequence fragment?  PSI-MI CV term for cleavage site?
 * <li>    A protein with two cleavage sites that removes an internal sequence e.g. an intein i.e. ABC -> A
 * <li>    Cleavage of a circular sequence e.g. a plasmid.
 * </ul>
 * In the case of removal ( e.g. intron)  the fragment that is *removed* is specified in the feature location
 * property. In the case of a "cut" (e.g. restriction enzyme cut site) the location of the cut is specified instead.
 * Examples: Insulin Hormone
 */
public interface FragmentFeature extends EntityFeature
{

}
