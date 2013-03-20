/**
 * This package contains a simple GSEA converter.
 *
 * Converts a BioPAX model to GSEA (GMT format).
 * <p/>
 * Creates GSEA entries from the protein references's xrefs contained in the BioPAX model.
 * One entry (id-list) per pathway per organism. If there are no pathways,
 * then simply - per organism (i.e., all available protein types are considered).
 * - One identifier per protein reference (not guaranteed to be the primary one).
 * All identifiers can only be of the same type, e.g., UniProt,
 * and the converter does not do any id-mapping; so a protein without
 * the required identifier type will not be listed.
 * <p/>
 * Note, to effectively enforce cross-species violation, bio-sources must
 * be annotated (have a unification xref) with "taxonomy" database name
 * and id, and pathways's, protein references's "organism" property - not empty.
 * <p/>
 * Note, this code assumes that the model has successfully been validated
 * and normalized (e.g., using the BioPAX Validator for Level3 data).
 * L1 and L2 models are first converted to L3 (this however does not
 * fix BioPAX errors, if any present, but possibly adds new)
 */
package org.biopax.paxtools.io.gsea;