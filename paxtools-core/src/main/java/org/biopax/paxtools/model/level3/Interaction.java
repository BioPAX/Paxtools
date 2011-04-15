package org.biopax.paxtools.model.level3;

import java.util.Set;


/**
 * Definition: A biological relationship between two or more entities.
 * <p/>
 * Rationale: In BioPAX, interactions are atomic from a database modeling perspective,
 * i.e. interactions can not be decomposed into sub-interactions. When representing non-atomic continuants with
 * explicit subevents the pathway class should be used instead. Interactions are not necessarily  temporally atomic,
 * for example genetic interactions cover a large span of time. Interactions as a formal concept is a continuant,
 * it retains its identitiy regardless of time, or any differences in specific states or properties.
 * <p/>
 * Usage: Interaction is a highly abstract class and in almost all cases it is more appropriate to use one of the
 * subclasses of interaction.
 * It is partially possible to define generic reactions by using generic participants. A more comprehensive method is
 * planned for BioPAX L4 for covering all generic cases like oxidization of a generic alcohol.
 * <p/>
 * Synonyms: Process, relationship, event.
 * <p/>
 * Examples: protein-protein interaction, biochemical reaction, enzyme catalysis
 */
public interface Interaction extends Process
{

	/**
	 * Controlled vocabulary term annotating the interaction type for example, "phosphorylation reaction". This
	 * annotation is meant to be human readable and may not be suitable for computing tasks, like reasoning,
	 * that require formal vocabulary systems. For instance, this information would be useful for display on a web
	 * page or for querying a database. The PSI-MI interaction type controlled vocabulary should be used. This is
	 * browsable at:
	 * <a href =
	 * "http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MI&termId=MI%3A0190&termName=interaction%20type">
	 * OLS: PSI-MI<a/>
	 * <p/>
	 * Contents of this set should not be modified. Use add/remove instead.
	 * @return Controlled vocabulary term annotating the interaction type.
	 */
	Set<InteractionVocabulary> getInteractionType();

	/**
	 * Controlled vocabulary term annotating the interaction type for example, "phosphorylation reaction". This
	 * annotation is meant to be human readable and may not be suitable for computing tasks, like reasoning,
	 * that require formal vocabulary systems. For instance, this information would be useful for display on a web
	 * page or for querying a database. The PSI-MI interaction type controlled vocabulary should be used. This is
	 * browsable at:
	 * <a href =
	 * "http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MI&termId=MI%3A0190&termName=interaction%20type">
	 * OLS: PSI-MI<a/>
	 * <p/>
	 * Contents of this set should not be modified. Use add/remove instead.
	 * @param newinteractionType Controlled vocabulary term annotating the interaction type.
	 */
	void addInteractionType(InteractionVocabulary newinteractionType);

	/**
	 * Controlled vocabulary term annotating the interaction type for example, "phosphorylation reaction". This
	 * annotation is meant to be human readable and may not be suitable for computing tasks, like reasoning,
	 * that require formal vocabulary systems. For instance, this information would be useful for display on a web
	 * page or for querying a database. The PSI-MI interaction type controlled vocabulary should be used. This is
	 * browsable at:
	 * <a href =
	 * "http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=MI&termId=MI%3A0190&termName=interaction%20type">
	 * OLS: PSI-MI<a/>
	 * <p/>
	 * Contents of this set should not be modified. Use add/remove instead.
	 * @param oldinteractionType Controlled vocabulary term annotating the interaction type.
	 */
	void removeInteractionType(InteractionVocabulary oldinteractionType);


	/**
	 * The entities that participate in this interaction. For example, in a biochemical reaction,
	 * the participants are the union of the reactants and the products of the reaction. Multiple sub-properties of
	 * participant are defined, such as left and right used in the BiochemicalReaction class and controller and
	 * controlled, used in the Control class. Any value of the sub-properties is automatically values of the
	 * participant property.
	 * Contents of this set should not be modified. Use add/remove instead.
	 * @return The entities that participate in this interaction.
	 */
	Set<Entity> getParticipant();


	/**
	 * The entities that participate in this interaction. For example, in a biochemical reaction,
	 * the participants are the union of the reactants and the products of the reaction. Multiple sub-properties of
	 * participant are defined, such as left and right used in the BiochemicalReaction class and controller and
	 * controlled, used in the Control class. Any value of the sub-properties is automatically values of the
	 * participant property.
	 * Contents of this set should not be modified. Use add/remove instead.
	 * @param participant The entities that participate in this interaction.
	 */
	void addParticipant(Entity participant);


	/**
	 * The entities that participate in this interaction. For example, in a biochemical reaction,
	 * the participants are the union of the reactants and the products of the reaction. Multiple sub-properties of
	 * participant are defined, such as left and right used in the BiochemicalReaction class and controller and
	 * controlled, used in the Control class. Any value of the sub-properties is automatically values of the
	 * participant property.
	 * Contents of this set should not be modified. Use add/remove instead.
	 * @param participant The entities that participate in this interaction.
	 */
	void removeParticipant(Entity participant);


}
