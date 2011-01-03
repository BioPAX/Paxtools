package org.biopax.paxtools.model.level3;

/**
 * <b>Definition:</b> A conversion in which a pool of macromolecules are degraded into their elementary
 * units.
 * <p/>
 * Usage: This conversion always has a direction of left-to-right and is irreversible. Degraded
 * molecules are always represented on the left, degradation products on the right.
 * <p/>
 * Comments: Degradation is a complex abstraction over multiple reactions. Although it obeys law of
 * mass conservation and stoichiometric, the products are rarely specified since they are
 * ubiquitous.
 * <p/>
 * Example:  Degradation of a protein to amino acids.
 */
public interface Degradation extends Conversion
{

}
