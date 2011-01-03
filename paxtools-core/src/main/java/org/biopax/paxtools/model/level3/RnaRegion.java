package org.biopax.paxtools.model.level3;

/**
 * <b>Definition:</b> A region on a RNA molecule.
 * <p/>
 * <b>Usage:</b> RNARegion is not a pool of independent molecules but a subregion on these molecules. As such, every RNARegion
 * has a defining RNA molecule.
 * <p/>
 * <b>Examples:</b> CDS, 3' UTR, Hairpin
 */
public interface RnaRegion extends NucleicAcid {
}
