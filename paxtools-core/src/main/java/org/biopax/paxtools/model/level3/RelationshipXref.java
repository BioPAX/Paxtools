package org.biopax.paxtools.model.level3;


/**
 * <b>Definition</b>: An xref that defines a reference to an entity in an external resource that
 * does not have the same biological identity as the referring entity.
 * <p/>
 * <b>Usage</b>: There is currently no controlled vocabulary of relationship types for BioPAX,
 * although one will be created in the future if a need develops.
 * <p/>
 * <b>Examples</b>: A link between a gene G in a BioPAX data collection, and the protein product P
 * of that gene in an external database. This is not a unification xref because G and P are
 * different biological entities (one is a gene and one is a protein). Another example is a
 * relationship xref for a protein that refers to the Gene Ontology biological process, e.g. 'immune
 * response,' that the protein is involved in.
 */
public interface RelationshipXref extends Xref
{

	/**
	 * <b>Definition:</b>A controlled vocabulary term that defines the type of relationship that this xref defines.
	 * <p/>
	 * <b>Usage</b>: There is currently no controlled vocabulary of relationship types for BioPAX,
	 * although one will be created in the future as the usage of this property increases.
	 *
	 * @return The type of relationship
	 */
	RelationshipTypeVocabulary getRelationshipType();

	/**
	 * <b>Definition:</b>A controlled vocabulary term that defines the type of relationship that this xref defines.
	 * <p/>
	 * <b>Usage</b>: There is currently no controlled vocabulary of relationship types for BioPAX,
	 * although one will be created in the future as the usage of this property increases.
	 *
	 * @param relationshipType The type of relationship that this xref defines
	 */
	void setRelationshipType(RelationshipTypeVocabulary relationshipType);
}
