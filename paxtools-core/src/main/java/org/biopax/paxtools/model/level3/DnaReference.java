package org.biopax.paxtools.model.level3;

/**
 * A DNA reference is a grouping of several DNA entities that are common in
 * sequence and genomic position.  Members can differ in celular location,
 * sequence features, SNPs, mutations and bound partners. For more detailed
 * information on semantics see {@link EntityReference}.
 * <p/>
 * Comments : Note that this is not a reference gene. A gene can possibly span
 * multiple DNA molecules, sometimes even across chromosomes due to regulatory
 * regions. Similarly a gene is not necessarily made up of deoxyribonucleic acid
 * and can be present in multiple copies ( which are different DNA molecules).
 */

public interface DnaReference extends NucleicAcidReference
{

}
