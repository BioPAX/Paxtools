package org.biopax.paxtools.model.level3;

/**
 * Definition: A region on a DNA molecule.
 * <p/>
 * Usage:  DNARegion is not a pool of independent molecules but a subregion on these molecules. As such,
 * every DNARegion has a defining DNA molecule. This is defined at the EntityReference level.
 * <p/>
 * Examples: Protein encoding region, promoter
 * @see DnaRegionReference
 */
public interface DnaRegion extends NucleicAcid
{
}
