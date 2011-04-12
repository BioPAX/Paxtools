package org.biopax.paxtools.model.level3;

import java.util.Set;


/**
 * Definition: This class represents a term from an external controlled vocabulary (CV).
 * <p/>
 * Rationale: Controlled Vocabularies mark cases where BioPAX delegates the representation of a complex biological
 * phenomena to an external controlled vocabulary development effort such as Gene Ontology. Each subclass of this
 * class represents one such case and often has an associated "Best-Practice" external resource to use. See the
 * documentation of each subclass for more specific information. Correct usage of controlled vocabularies are
 * critical to data exchange and integration.
 * <p/>
 * Usage: The individuals belonging to this class must unambiguously refer to the source controlled vocabulary.
 * This can be achieved in two manners:
 * <p/>
 * The xref property of this class is restricted to the unification xref class. It must point to the source
 * controlled vocabulary.
 * <p/>
 * Alternatively the rdf-id of the member individuals can be set to the designated MIRIAM URN.
 * <p/>
 * It is a best practice to do both whenever possible.
 * <p/>
 * Although it is possible to use multiple unification xrefs to identify semantically identical terms across
 * alternative controlled vocabularies, this is not a recommended practice as it might lead to maintenance
 * issues as the controlled vocabularies change.
 * <p/>
 * There is no recommended use-cases for directly instantiating this class. Please, use its subclasses instead.
 */
public interface ControlledVocabulary extends UtilityClass, XReferrable
{

	/**
	 * @return The external controlled vocabulary term.
	 */
	Set<String> getTerm();

	/**
	 * @param term The external controlled vocabulary term.
	 */
	void addTerm(String term);

	/**
	 * @param term The external controlled vocabulary term.
	 */
	void removeTerm(String term);


}
