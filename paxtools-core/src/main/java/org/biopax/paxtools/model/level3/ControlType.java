package org.biopax.paxtools.model.level3;


/**
 * Defines the nature of the control relationship between the controller and the controlled entities.
*/
public enum ControlType
{
	/**
	 * General activation. Compounds that activate the specified enzyme activity by an unknown mechanism. The
	 * mechanism is defined as unknown, because either the mechanism has yet to be elucidated in the experimental
	 * literature, or the paper(s) curated thus far do not define the mechanism, and a full literature search has
	 * yet to be performed.
	 */
	ACTIVATION,
	/**
	 * General inhibition. Compounds that inhibit the specified enzyme activity by an unknown mechanism. The
	 * mechanism is defined as unknown, because either the mechanism has yet to be elucidated in the experimental
	 * literature, or the paper(s) curated thus far do not define the mechanism, and a full literature search has
	 * yet to be performed.
	 */
	INHIBITION,

	/**
	 * @deprecated LEVEL 1 workaround
	 */
	ACTIVATION_UNKMECH,
	/**
	 * @deprecated LEVEL 1 workaround
	 */
	INHIBITION_UNKMECH,
	/**
	 * Allosteric inhibitors decrease the specified enzyme activity by binding reversibly to the enzyme and
	 * inducing a conformational change that decreases the affinity of the enzyme to its substrates without
	 * affecting its VMAX. Allosteric inhibitors can be competitive or noncompetitive inhibitors, therefore,
	 * those inhibition categories can be used in conjunction with this category.
	 */
	INHIBITION_ALLOSTERIC,
	/**
	 * Competitive inhibitors are compounds that competitively inhibit the specified enzyme activity by binding
	 * reversibly to the enzyme and preventing the substrate from binding. Binding of the inhibitor and substrate
	 * are mutually exclusive because it is assumed that the inhibitor and substrate can both bind only to the free
	 * enzyme. A competitive inhibitor can either bind to the active site of the enzyme,
	 * directly excluding the substrate from binding there, or it can bind to another site on the enzyme,
	 * altering the conformation of the enzyme such that the substrate can not bind to the active site.
	 */
	INHIBITION_COMPETITIVE,
	/**
	 * Irreversible inhibitors are compounds that irreversibly inhibit the specified enzyme activity by binding to
	 * the enzyme and dissociating so slowly that it is considered irreversible. For example, alkylating agents,
	 * such as iodoacetamide, irreversibly inhibit the catalytic activity of some enzymes by modifying cysteine
	 * side chains.
	 */
	INHIBITION_IRREVERSIBLE,
	/**
	 * Noncompetitive inhibitors are compounds that noncompetitively inhibit the specified enzyme by binding
	 * reversibly to both the free enzyme and to the enzyme-substrate complex. The inhibitor and substrate may be
	 * bound to the enzyme simultaneously and do not exclude each other. However, only the enzyme-substrate complex
	 * (not the enzyme-substrate-inhibitor complex) is catalytically active.
	 */
	INHIBITION_NONCOMPETITIVE,
	/**
	 * Compounds that inhibit the specified enzyme activity by a mechanism that has been characterized,
	 * but that cannot be clearly classified as irreversible, competitive, noncompetitive, uncompetitive,
	 * or allosteric.
	 */
	INHIBITION_OTHER,
	/**
	 * Uncompetitive inhibitors are compounds that uncompetitively inhibit the specified enzyme activity by binding
	 * reversibly to the enzyme-substrate complex but not to the enzyme alone.
	 */
	INHIBITION_UNCOMPETITIVE,
	/**
	 * Nonallosteric activators increase the specified enzyme activity by means other than allosteric.
	 */
	ACTIVATION_NONALLOSTERIC,
	/**
	 * Allosteric activators increase the specified enzyme activity by binding reversibly to the enzyme and
	 * inducing a conformational change that increases the affinity of the enzyme to its substrates without
	 * affecting its VMAX.
	 */
	ACTIVATION_ALLOSTERIC
}
