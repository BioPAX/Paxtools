package org.biopax.paxtools.model.level3;

/**
 * This class represents the results of a cleavage or degradation event. There
 * are multiple cases: <ul>
 * <li>   A protein with a single cleavage site that
 * converts the protein into two fragments (e.g. pro-insulin converted to
 * insulin and C-peptide). TODO: CV term for sequence fragment?  PSI-MI CV term
 * for cleavage site?
 * <li> A protein with two cleavage sites that removes an
 * internal sequence e.g. an intein i.e. ABC -> AC
 * <li>Cleavage of a circular sequence e.g. a plasmid. </ul>
 */
public interface FragmentFeature extends EntityFeature
{
	
}
