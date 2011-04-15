package org.biopax.paxtools.model.level3;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.Set;

/**
 * A Level 3 specific element. Instances of this class maps to an OWL Individual.
 */
public interface Level3Element extends BioPAXElement
{
	/**
	 * A textual comment about this individual. This property should be used instead of the OWL documentation
	 * elements (rdfs:comment) for instances because information in 'comment' is data to be exchanged,
	 * whereas the rdfs:comment field is used for metadata about the structure of the BioPAX ontology.
	 * Contents of this set should not be modified. Use Add/Remove instead.
	 * @return A textual comment about this individual.
	 */
	public Set<String> getComment();


	/**
	 * A textual comment about this individual. This property should be used instead of the OWL documentation
	 * elements (rdfs:comment) for instances because information in 'comment' is data to be exchanged,
	 * whereas the rdfs:comment field is used for metadata about the structure of the BioPAX ontology.
	 * Contents of this set should not be modified. Use Add/Remove instead.
	 * @param comment A textual comment about this individual.
	 */
	public void addComment(String comment);

	/**
	 * A textual comment about this individual. This property should be used instead of the OWL documentation
	 * elements (rdfs:comment) for instances because information in 'comment' is data to be exchanged,
	 * whereas the rdfs:comment field is used for metadata about the structure of the BioPAX ontology.
	 * Contents of this set should not be modified. Use Add/Remove instead.
	 * @param comment A textual comment about this individual.
	 */
	public void removeComment(String comment);
}
