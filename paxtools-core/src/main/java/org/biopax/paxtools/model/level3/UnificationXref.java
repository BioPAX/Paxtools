package org.biopax.paxtools.model.level3;


/**
 * <b>Definition:</b> A unification xref defines a reference to an entity in an external resource that has the same
 * biological identity as the referring entity Rationale: Unification xrefs are critically important for data
 * integration. In the future they may be replaced by direct miriam links and rdf:id based identity management.
 * <p/>
 * <b>Usage:</b> For example, if one wished to link from a database record, C, describing a chemical compound in a
 * BioPAX data collection to a record, C', describing the same chemical compound in an external database, one would use
 * a unification xref since records C and C' describe the same biological identity. Generally, unification xrefs should
 * be used whenever possible, although there are cases where they might not be useful, such as application to
 * application data exchange.Identity of interactions can be computed based on the  identity of its participants. An
 * xref in a protein pointing to a gene, e.g. in the LocusLink database17, would not be a unification xref since the two
 * entities do not have the same biological identity (one is a protein, the other is a gene). Instead, this link should
 * be a captured as a relationship xref. References to an external controlled vocabulary term within the
 * OpenControlledVocabulary class should use a unification xref where possible (e.g. GO:0005737).
 * <p/>
 * <b>Examples:</b> An xref in a protein instance pointing to an entry in the Swiss-Prot database, and an xref in an RNA
 * instance pointing to the corresponding RNA sequence in the RefSeq database..
 */
public interface UnificationXref extends Xref {
}
