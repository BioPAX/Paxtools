package org.biopax.paxtools.model.level3;

/**
 * <b>Definition:</b> An entity feature that represent the covalently bound state of  a physical entity.
 * <p/>
 * Rationale: Most frequent covalent modifications to proteins and DNA, such as phosphorylation and
 * metylation are covered by the ModificationFeature class. In these cases, the added groups are
 * simple and stateless therefore they can be captured by a controlled vocabulary. In other cases,
 * such as ThiS-Thilacyl-disulfide, the covalently linked molecules are best represented as a
 * molecular complex. CovalentBindingFeature should be used to model such covalently linked
 * complexes.
 * <p/>
 * Usage: Using this construct, it is possible to represent small molecules as a covalent complex of
 * two other small molecules. The demarcation of small molecules is a general problem and is
 * delegated to small molecule databases.The best practice is not to model using covalent complexes
 * unless at least one of the participants is a protein, DNA or RNA.
 * <p/>
 * Examples: disulfide bond UhpC + glc-6P -> Uhpc-glc-6p acetyl-ACP -> decenoyl-ACP charged tRNA
 */
public interface CovalentBindingFeature extends BindingFeature, ModificationFeature
{

}